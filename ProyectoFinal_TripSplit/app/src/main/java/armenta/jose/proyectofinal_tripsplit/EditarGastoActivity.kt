package armenta.jose.proyectofinal_tripsplit

import ParticipantesAdapter
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.R
import armenta.jose.proyectofinal_tripsplit.utilities.Integrante
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditarGastoActivity : AppCompatActivity() {
    private lateinit var nombreEditText: EditText
    private lateinit var montoEditText: EditText
    private lateinit var categoriaSpinner: Spinner
    private lateinit var quienPagoSpinner: Spinner
    private lateinit var rvIntegrantes: RecyclerView
    private lateinit var btnGuardar: Button
    private lateinit var btnCancelar: Button
    private lateinit var btnFlechaAtras: ImageButton

    private lateinit var dbRef: DatabaseReference
    private lateinit var gastoId: String
    private lateinit var grupoId: String

    private val participantesAdapter = ParticipantesAdapter()
    private val integrantesList = mutableListOf<Integrante>()
    private val integranteToUserIdMap = mutableMapOf<Int, String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_gasto)

        gastoId = intent.getStringExtra("GASTO_ID").also {
            Log.d("EditarGastoActivity", "GastoID recibido: $it")
        } ?: ""
        grupoId = intent.getStringExtra("GRUPO_ID").also {
            Log.d("EditarGastoActivity", "GrupoID recibido: $it")
        } ?: ""

        if (gastoId.isEmpty() || grupoId.isEmpty()) {
            Toast.makeText(this, "Error: Datos no válidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initializeViews()
        setupViews()
        setupCategoriaSpinner()
        cargarDatosGasto()
    }

    private fun initializeViews() {
        nombreEditText = findViewById(R.id.et_nombre_gasto)
        montoEditText = findViewById(R.id.et_monto_gasto)
        categoriaSpinner = findViewById(R.id.spinnerCategoria)
        quienPagoSpinner = findViewById(R.id.spinnerQuienPago)
        rvIntegrantes = findViewById(R.id.rv_integrantes)
        btnGuardar = findViewById(R.id.btn_guardar)
        btnCancelar = findViewById(R.id.btn_cancelar)
        btnFlechaAtras = findViewById(R.id.btn_flecha_atras)

        dbRef = FirebaseDatabase.getInstance().reference

        rvIntegrantes.layoutManager = LinearLayoutManager(this)
        rvIntegrantes.adapter = participantesAdapter
    }

    private fun setupViews() {
        btnFlechaAtras.setOnClickListener { finish() }
        btnCancelar.setOnClickListener { finish() }
        btnGuardar.setOnClickListener { guardarCambios() }
    }

    private fun setupCategoriaSpinner() {
        val categorias = arrayOf("Comida", "Traslado", "Hospedaje", "Entretenimiento", "Otros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriaSpinner.adapter = adapter
    }

    private fun cargarDatosGasto() {
        dbRef.child("gastos").child(gastoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(this@EditarGastoActivity, "Gasto no encontrado", Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }

                    // Cargar datos básicos del gasto
                    nombreEditText.setText(snapshot.child("nombre").getValue(String::class.java))

                    // Modificación en la obtención del monto
                    val montoTotal = snapshot.child("montoTotal").getValue()
                    when (montoTotal) {
                        is Long -> montoEditText.setText(montoTotal.toString())
                        is Double -> montoEditText.setText(montoTotal.toString())
                        is Int -> montoEditText.setText(montoTotal.toString())
                        else -> montoEditText.setText("0")
                    }

                    // Seleccionar categoría
                    val categoria = snapshot.child("categoria").getValue(String::class.java) ?: ""
                    val categorias = arrayOf("Comida", "Traslado", "Hospedaje", "Entretenimiento", "Otros")
                    val categoriaPosition = categorias.indexOf(categoria)
                    if (categoriaPosition != -1) {
                        categoriaSpinner.setSelection(categoriaPosition)
                    }

                    // Obtener pagadorId y participantes
                    val pagadorId = snapshot.child("pagadorId").getValue(String::class.java)
                    val participantesIds = mutableListOf<String>()
                    snapshot.child("participantesIds").children.forEach {
                        it.getValue(String::class.java)?.let { id -> participantesIds.add(id) }
                    }

                    // Cargar integrantes
                    cargarIntegrantes(pagadorId, participantesIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditarGastoActivity, "Error al cargar el gasto", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarIntegrantes(pagadorId: String?, participantesIds: List<String>) {
        dbRef.child("grupos").child(grupoId).child("miembrosIds")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("EditarGastoActivity", "Cargando miembros del grupo")
                    val miembrosIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }

                    miembrosIds.forEachIndexed { index, userId ->
                        dbRef.child("Usuarios").child(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val nombre = userSnapshot.child("nombre").getValue(String::class.java) ?: "Desconocido"
                                    val integranteId = index + 1

                                    val integrante = Integrante(
                                        id = integranteId,
                                        nombre = nombre,
                                        deuda = 0.0
                                    )
                                    integrantesList.add(integrante)
                                    integranteToUserIdMap[integranteId] = userId

                                    if (integrantesList.size == miembrosIds.size) {
                                        // Actualizar spinner de quien pagó
                                        val spinnerAdapter = ArrayAdapter(
                                            this@EditarGastoActivity,
                                            android.R.layout.simple_spinner_item,
                                            integrantesList.map { it.nombre }
                                        )
                                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                        quienPagoSpinner.adapter = spinnerAdapter

                                        // Encontrar y seleccionar al pagador
                                        val pagadorIndex = integrantesList.indexOfFirst {
                                            integranteToUserIdMap[it.id] == pagadorId
                                        }
                                        if (pagadorIndex != -1) {
                                            quienPagoSpinner.setSelection(pagadorIndex)
                                        }

                                        // Actualizar RecyclerView
                                        participantesAdapter.actualizarLista(
                                            integrantesList,
                                            participantesIds,
                                            integranteToUserIdMap
                                        )
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Log.e("EditarGastoActivity", "Error al cargar usuario", error.toException())
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("EditarGastoActivity", "Error al cargar miembros", error.toException())
                }
            })
    }

    private fun guardarCambios() {
        val nombre = nombreEditText.text.toString()
        val monto = montoEditText.text.toString().toDoubleOrNull()
        val categoria = categoriaSpinner.selectedItem.toString()

        if (nombre.isBlank() || monto == null) {
            Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val pagadorPosition = quienPagoSpinner.selectedItemPosition
        val pagador = integrantesList.getOrNull(pagadorPosition)

        if (pagador == null) {
            Toast.makeText(this, "Por favor selecciona quién pagó", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener el userId del pagador
        val pagadorUserId = integranteToUserIdMap[pagador.id]

        val actualizaciones = hashMapOf<String, Any>(
            "nombre" to nombre,
            "montoTotal" to monto,
            "categoria" to categoria,
            "pagadorId" to (pagadorUserId ?: ""),
            "participantesIds" to participantesAdapter.getParticipantesSeleccionados()
        )

        dbRef.child("gastos").child(gastoId)
            .updateChildren(actualizaciones)
            .addOnSuccessListener {
                Toast.makeText(this, "Gasto actualizado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al actualizar el gasto", Toast.LENGTH_SHORT).show()
            }
    }
}