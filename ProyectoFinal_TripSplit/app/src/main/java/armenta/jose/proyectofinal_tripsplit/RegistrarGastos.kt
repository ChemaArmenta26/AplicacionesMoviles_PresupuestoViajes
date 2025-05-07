package armenta.jose.proyectofinal_tripsplit


import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Gasto
import armenta.jose.proyectofinal_tripsplit.utilities.IntegranteRegistroAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.MiembroInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.concurrent.atomic.AtomicInteger




class RegistrarGastos : AppCompatActivity() {


    private lateinit var currentGroupId: String


    private lateinit var auth: FirebaseAuth
    private lateinit var dbRefGrupos: DatabaseReference
    private lateinit var dbRefUsuarios: DatabaseReference
    private lateinit var dbRefGastos: DatabaseReference
    private lateinit var etNombreGasto: EditText
    private lateinit var etMontoGasto: EditText
    private lateinit var spinnerCategoria: Spinner
    private lateinit var spinnerQuienPago: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnGuardar: AppCompatButton
    private lateinit var btnCancelar: AppCompatButton
    private lateinit var btnFlechaAtras: ImageButton
    private lateinit var adapter: IntegranteRegistroAdapter
    private val listaMiembrosInfo = mutableListOf<MiembroInfo>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_gastos)


        currentGroupId = intent.getStringExtra("GRUPO_ID") ?: ""
        if (currentGroupId.isEmpty()) {
            handleError("Error: No se pudo identificar el grupo.")
            return
        }
        Log.d("RegistrarGastos", "Registrando gasto para el grupo: $currentGroupId")


        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        dbRefGrupos = database.getReference("grupos")
        dbRefUsuarios = database.getReference("Usuarios")
        dbRefGastos = database.getReference("gastos")


        btnFlechaAtras = findViewById(R.id.btn_flecha_atras)
        btnCancelar = findViewById(R.id.btn_cancelar)
        btnGuardar = findViewById(R.id.btn_guardar)
        etNombreGasto = findViewById(R.id.et_nombre_gasto)
        etMontoGasto = findViewById(R.id.et_monto_gasto)
        spinnerCategoria = findViewById(R.id.spinnerCategoria)
        spinnerQuienPago = findViewById(R.id.spinnerQuienPago)
        recyclerView = findViewById(R.id.rv_integrantes)


        if (supportFragmentManager.findFragmentById(R.id.topBarFragment) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }


        setupUI()
        setupRecyclerView()
        cargarMiembrosDelGrupo(currentGroupId)


    }


    private fun setupUI() {
        btnFlechaAtras.setOnClickListener {
            finish()
        }


        btnCancelar.setOnClickListener {
            finish()
        }
        btnGuardar.setOnClickListener {
            it.isEnabled = false
            guardarGasto()
        }


        val categorias = listOf(
            "Selecciona una categoría",
            "Hospedaje",
            "Comida",
            "Traslado",
            "Entretenimiento",
            "Otros"
        )
        val adapterCategoria = createHintAdapter(categorias)
        spinnerCategoria.adapter = adapterCategoria
        spinnerCategoria.setSelection(0, false)
    }


    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = IntegranteRegistroAdapter()
        recyclerView.adapter = adapter
    }


    private fun cargarMiembrosDelGrupo(groupId: String) {
        Log.d("RegistrarGastos", "Cargando miembros para el grupo: $groupId")


        dbRefGrupos.child(groupId).child("miembrosIds")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshotMiembros: DataSnapshot) {
                    val miembrosIds =
                        snapshotMiembros.children.mapNotNull { it.getValue(String::class.java) }
                    Log.d("RegistrarGastos", "Miembros IDs encontrados: $miembrosIds")


                    if (miembrosIds.isEmpty()) {
                        handleError("No se encontraron miembros en este grupo.")
                        return
                    }


                    fetchMemberDetails(miembrosIds)
                }

                override fun onCancelled(error: DatabaseError) {
                    handleError("Error al cargar miembros: ${error.message}", error.toException())
                }
            })
    }


    private fun fetchMemberDetails(miembrosIds: List<String>) {
        listaMiembrosInfo.clear()
        val fetchCounter = AtomicInteger(miembrosIds.size)


        if (miembrosIds.isEmpty()) {
            actualizarUIConMiembros()
            return
        }


        for (userId in miembrosIds) {
            dbRefUsuarios.child(userId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshotUsuario: DataSnapshot) {
                        val nombre =
                            snapshotUsuario.child("nombre").getValue(String::class.java) ?: ""
                        val apellido =
                            snapshotUsuario.child("apellido").getValue(String::class.java) ?: ""
                        val nombreCompleto =
                            "$nombre $apellido".trim().ifEmpty { "Usuario Desconocido ($userId)" }


                        listaMiembrosInfo.add(MiembroInfo(userId, nombreCompleto))
                        Log.d("RegistrarGastos", "Miembro cargado: $nombreCompleto (UID: $userId)")


                        if (fetchCounter.decrementAndGet() == 0) {
                            Log.d(
                                "RegistrarGastos",
                                "Todos los miembros cargados. Actualizando UI."
                            )
                            actualizarUIConMiembros()
                        }
                    }


                    override fun onCancelled(error: DatabaseError) {
                        Log.e(
                            "RegistrarGastos",
                            "Error al cargar datos para usuario $userId",
                            error.toException()
                        )
                        listaMiembrosInfo.add(MiembroInfo(userId, "Error al cargar ($userId)"))
                        if (fetchCounter.decrementAndGet() == 0) {
                            actualizarUIConMiembros()
                        }
                    }
                })
        }
    }


    private fun actualizarUIConMiembros() {
        listaMiembrosInfo.sortBy { it.nombre }
        adapter.actualizarLista(listaMiembrosInfo)


        val adapterQuienPago = createHintAdapter(
            listOf("Selecciona quién pagó") + listaMiembrosInfo.map { it.nombre }
        )
        spinnerQuienPago.adapter = adapterQuienPago
        spinnerQuienPago.setSelection(0, false)
        Log.d("RegistrarGastos", "RecyclerView y Spinner 'Quién Pagó' actualizados.")
    }


    private fun guardarGasto() {
        val nombreGasto = etNombreGasto.text.toString().trim()
        val montoStr = etMontoGasto.text.toString().trim()
        val categoriaPos = spinnerCategoria.selectedItemPosition
        val pagadorPos = spinnerQuienPago.selectedItemPosition


        if (nombreGasto.isEmpty()) {
            handleError("Ingresa el nombre del gasto."); return
        }
        val montoTotal = montoStr.toDoubleOrNull()
        if (montoTotal == null || montoTotal <= 0) {
            handleError("Ingresa un monto válido y mayor a cero."); return
        }
        if (categoriaPos == 0) {
            handleError("Selecciona una categoría."); return
        }
        if (pagadorPos == 0) {
            handleError("Selecciona quién pagó."); return
        }


        val categoriaSeleccionada = spinnerCategoria.selectedItem.toString()
        if (pagadorPos - 1 >= listaMiembrosInfo.size) {
            handleError("Error al seleccionar quién pagó."); return
        }
        val pagadorId = listaMiembrosInfo[pagadorPos - 1].uid


        val participantesIds = adapter.obtenerParticipantesSeleccionados()
        if (participantesIds.isEmpty()) {
            handleError("Selecciona al menos un participante."); return
        }


        val creadorId = auth.currentUser?.uid
        if (creadorId == null) {
            handleError("Error de autenticación al guardar."); return
        }


        val gasto = Gasto(
            currentGroupId,
            nombreGasto,
            montoTotal,
            categoriaSeleccionada,
            participantesIds,
            pagadorId,
            creadorId,
        )


        Log.d("RegistrarGastos", "Intentando guardar gasto: ${gasto.nombre}")
        val nuevoGastoPushRef = dbRefGastos.push()
        val nuevoGastoId = nuevoGastoPushRef.key


        if (nuevoGastoId == null) {
            handleError("No se pudo generar ID para el gasto.")
            return
        }
        gasto.id = nuevoGastoId


        nuevoGastoPushRef.setValue(gasto)
            .addOnSuccessListener {
                Log.i("RegistrarGastos", "Gasto $nuevoGastoId guardado exitosamente.")
                agregarGastoIdAlGrupo(currentGroupId, nuevoGastoId)
            }
            .addOnFailureListener { error ->
                handleError("Error al guardar el gasto: ${error.message}", error)
            }
    }


    private fun agregarGastoIdAlGrupo(groupId: String, gastoId: String) {
        val refListaGastos = dbRefGrupos.child(groupId).child("gastosIds")


        refListaGastos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val listaActual =
                    snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
                if (!listaActual.contains(gastoId)) {
                    listaActual.add(gastoId)
                    refListaGastos.setValue(listaActual)
                        .addOnSuccessListener {
                            Log.i(
                                "RegistrarGastos",
                                "ID $gastoId añadido a gastos del grupo $groupId"
                            )
                            Toast.makeText(
                                this@RegistrarGastos,
                                "Gasto registrado con éxito",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                        .addOnFailureListener { error ->
                            handleError(
                                "Gasto guardado, pero error al actualizar grupo: ${error.message}",
                                error
                            )
                            finish()
                        }
                } else {
                    Log.w(
                        "RegistrarGastos",
                        "El ID $gastoId ya estaba en la lista del grupo $groupId."
                    )
                    Toast.makeText(
                        this@RegistrarGastos,
                        "Gasto registrado (ya estaba en lista grupo)",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                handleError(
                    "Gasto guardado, pero error al leer gastos del grupo: ${error.message}",
                    error.toException()
                )
                finish()
            }
        })
    }


    private fun createHintAdapter(items: List<String>): ArrayAdapter<String> {
        return object :
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items) {
            override fun isEnabled(position: Int): Boolean = position != 0
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as? TextView)?.setTextColor(if (position == 0) Color.GRAY else Color.BLACK)
                return view
            }
        }
    }


    private fun handleError(message: String, error: Exception? = null) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        if (error != null) {
            Log.e("RegistrarGastos", message, error)
        } else {
            Log.e("RegistrarGastos", message)
        }
        btnGuardar.isEnabled = true
    }
}