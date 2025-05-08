import android.os.Bundle
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

class EditarGasto : AppCompatActivity() {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_gasto)

        // Obtener IDs del intent
        gastoId = intent.getStringExtra("GASTO_ID") ?: ""
        grupoId = intent.getStringExtra("GRUPO_ID") ?: ""

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

        // Configurar RecyclerView
        rvIntegrantes.layoutManager = LinearLayoutManager(this)
        rvIntegrantes.adapter = participantesAdapter
    }

    private fun setupViews() {
        btnFlechaAtras.setOnClickListener { finish() }
        btnCancelar.setOnClickListener { finish() }
        btnGuardar.setOnClickListener { guardarCambios() }
    }

    private fun setupCategoriaSpinner() {
        val categorias = arrayOf("Comida", "Transporte", "Alojamiento", "Actividades", "Otros")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categorias)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categoriaSpinner.adapter = adapter
    }

    private fun cargarDatosGasto() {
        // Primero cargar el gasto
        dbRef.child("gastos").child(gastoId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        Toast.makeText(this@EditarGasto, "Gasto no encontrado", Toast.LENGTH_SHORT).show()
                        finish()
                        return
                    }

                    // Cargar datos básicos del gasto
                    nombreEditText.setText(snapshot.child("nombre").getValue(String::class.java))
                    montoEditText.setText(snapshot.child("montoTotal").getValue(Double::class.java).toString())

                    // Seleccionar categoría
                    val categoria = snapshot.child("categoria").getValue(String::class.java) ?: ""
                    val categoriaPosition = (categoriaSpinner.adapter as ArrayAdapter<String>).getPosition(categoria)
                    if (categoriaPosition != -1) {
                        categoriaSpinner.setSelection(categoriaPosition)
                    }

                    // Guardar IDs de participantes y pagador para usarlos después
                    val pagadorId = snapshot.child("pagadorId").getValue(String::class.java)
                    val participantesIds = snapshot.child("participantesIds").children.mapNotNull {
                        it.getValue(String::class.java)
                    }

                    // Cargar integrantes del grupo
                    cargarIntegrantes(pagadorId, participantesIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditarGasto, "Error al cargar el gasto", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun cargarIntegrantes(pagadorId: String?, participantesIds: List<String>) {
        dbRef.child("grupos").child(grupoId).child("integrantes")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    integrantesList.clear()
                    val nombresParaSpinner = mutableListOf<String>()
                    var pagadorPosition = 0

                    snapshot.children.forEachIndexed { index, integranteSnap ->
                        val userId = integranteSnap.key ?: return@forEachIndexed

                        dbRef.child("Usuarios").child(userId)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(userSnapshot: DataSnapshot) {
                                    val nombre = userSnapshot.child("nombre").getValue(String::class.java) ?: "Desconocido"

                                    val integrante = Integrante(
                                        id = index + 1,
                                        nombre = nombre,
                                        deuda = 0.0
                                    )

                                    integrantesList.add(integrante)
                                    nombresParaSpinner.add(nombre)

                                    if (userId == pagadorId) {
                                        pagadorPosition = index
                                    }

                                    // Si hemos cargado todos los integrantes
                                    if (integrantesList.size == snapshot.childrenCount.toInt()) {
                                        // Configurar spinner de quien pagó
                                        val spinnerAdapter = ArrayAdapter(this@EditarGasto,
                                            android.R.layout.simple_spinner_item, nombresParaSpinner)
                                        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                                        quienPagoSpinner.adapter = spinnerAdapter
                                        quienPagoSpinner.setSelection(pagadorPosition)

                                        // Actualizar RecyclerView
                                        participantesAdapter.actualizarLista(integrantesList, participantesIds)
                                    }
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@EditarGasto,
                                        "Error al cargar usuario", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditarGasto,
                        "Error al cargar integrantes", Toast.LENGTH_SHORT).show()
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

        val actualizaciones = hashMapOf<String, Any>(
            "nombre" to nombre,
            "montoTotal" to monto,
            "categoria" to categoria,
            "pagadorId" to (pagador?.id?.toString() ?: ""),
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