package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import armenta.jose.proyectofinal_tripsplit.utilities.Gasto
import armenta.jose.proyectofinal_tripsplit.utilities.GastoAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersona
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersonaAdapter

class pantalla_principal : AppCompatActivity() {

    private lateinit var db: FirebaseDatabase
    private lateinit var dbRefGrupos: DatabaseReference
    private lateinit var dbRefGastos: DatabaseReference
    private lateinit var dbRefUsuarios: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private lateinit var listViewPersonas: ListView
    private lateinit var listViewGastos: ListView
    private lateinit var totalGastadoTextView: TextView
    private lateinit var btnAgregarGasto: ImageButton
    private lateinit var btnReporteGastos: Button
    private lateinit var tvCodigoGrupo: TextView
    private lateinit var tvNombreViaje: TextView

    private var gastosList: List<Gasto> = emptyList()

    private lateinit var groupId: String
    private val TAG = "PantallaPrincipal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_principal)

        db = FirebaseDatabase.getInstance()
        dbRefGrupos = db.getReference("grupos")
        dbRefGastos = db.getReference("gastos")
        dbRefUsuarios = db.getReference("Usuarios")
        auth = FirebaseAuth.getInstance()

        groupId = intent.getStringExtra("groupId") ?: intent.getStringExtra("GRUPO_ID") ?: ""

        if (groupId.isBlank()) {
            Toast.makeText(this, "Error: No se pudo cargar el grupo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        listViewPersonas = findViewById(R.id.lista_monto_pagar_persona)
        listViewGastos = findViewById(R.id.lista_gastos)
        totalGastadoTextView = findViewById(R.id.tv_total_gastado)
        btnAgregarGasto = findViewById(R.id.btn_agregar_gasto)
        btnReporteGastos = findViewById(R.id.btn_ver_reporte_gastos)
        tvCodigoGrupo = findViewById(R.id.tv_codigo_grupo)
        tvNombreViaje = findViewById(R.id.tv_nombre_viaje)

        loadGroupData()
        listenForExpenseChanges()
        listenForMembersChanges()

        btnAgregarGasto.setOnClickListener {
            val intentRegistrar = Intent(this, RegistrarGastos::class.java)
            intentRegistrar.putExtra("GRUPO_ID", groupId)
            startActivity(intentRegistrar)
        }

        btnReporteGastos.setOnClickListener {
            val intentReporte = Intent(this, reporte_gastos::class.java)
            intentReporte.putExtra("GRUPO_ID", groupId)
            startActivity(intentReporte)
        }
    }

    private fun loadGroupData() {
        dbRefGrupos.child(groupId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Viaje sin nombre"
                val codigo = snapshot.child("codigo").getValue(String::class.java) ?: "----"
                tvNombreViaje.text = nombre
                tvCodigoGrupo.text = codigo
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar datos del grupo", error.toException())
            }
        })
    }

    private fun listenForExpenseChanges() {
        dbRefGastos.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gastosList = snapshot.children.mapNotNull {
                    val gasto = it.getValue(Gasto::class.java)
                    gasto?.takeIf { g -> g.groupId == groupId }
                }

                updateExpensesUI(gastosList)
                updateTotalSpent(gastosList)
                updateUserBalances(gastosList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al escuchar gastos", error.toException())
            }
        })
    }

    private fun listenForMembersChanges() {
        dbRefGrupos.child(groupId).child("miembrosIds")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    updateUserBalances()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al escuchar cambios de miembros", error.toException())
                }
            })
    }

    private fun updateExpensesUI(gastosList: List<Gasto>) {
        val adapter = GastoAdapter(this, gastosList)
        listViewGastos.adapter = adapter
        adapter.notifyDataSetChanged()

        listViewGastos.setOnItemClickListener { _, _, position, _ ->
            val gastoSeleccionado = gastosList[position]
            val intentDetalle = Intent(this, DetalleGastoActivity::class.java)
            intentDetalle.putExtra("GASTO_ID", gastoSeleccionado.id)
            intentDetalle.putExtra("GRUPO_ID", groupId)
            startActivity(intentDetalle)
        }
    }

    private fun updateTotalSpent(gastosList: List<Gasto>) {
        val total = gastosList.sumOf { it.montoTotal ?: 0.0 }
        totalGastadoTextView.text = getString(R.string.currency_format, total)
    }

    private fun updateUserBalances(gastosList: List<Gasto>? = null) {
        dbRefGrupos.child(groupId).child("miembrosIds")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val miembrosIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    if (miembrosIds.isEmpty()) return

                    val gastos = gastosList ?: emptyList()
                    val balances = calculateBalances(miembrosIds, gastos)
                    val personas = mutableListOf<PagarPorPersona>()

                    for (uid in miembrosIds) {
                        dbRefUsuarios.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val nombre = userSnapshot.child("nombre").getValue(String::class.java) ?: "Usuario"
                                val fotoPerfil = userSnapshot.child("fotoPerfil").getValue(String::class.java)?.toIntOrNull()
                                    ?: R.mipmap.image_default_user
                                val balance = balances[uid] ?: 0.0

                                val formatted = getString(R.string.currency_format, kotlin.math.abs(balance))
                                val display = if (balance < 0) getString(R.string.debe_format, formatted)
                                else getString(R.string.recibe_format, formatted)

                                personas.add(PagarPorPersona(nombre, display, fotoPerfil))

                                if (personas.size == miembrosIds.size) {
                                    val adapter = PagarPorPersonaAdapter(this@pantalla_principal, personas)
                                    listViewPersonas.adapter = adapter
                                    adapter.notifyDataSetChanged()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                                Log.e(TAG, "Error al cargar usuario $uid", error.toException())
                            }
                        })
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "Error al obtener miembros", error.toException())
                }
            })
    }

    private fun calculateBalances(memberIds: List<String>, expenses: List<Gasto>): Map<String, Double> {
        val balances = mutableMapOf<String, Double>().apply {
            memberIds.forEach { put(it, 0.0) }
        }

        for (expense in expenses) {
            val monto = expense.montoTotal ?: 0.0
            val pagador = expense.pagadorId ?: continue
            val participantes = expense.participantesIds ?: continue

            val share = monto / participantes.size
            for (p in participantes) {
                balances[p] = (balances[p] ?: 0.0) - share
            }
            balances[pagador] = (balances[pagador] ?: 0.0) + monto
        }

        return balances
    }
}
