package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import armenta.jose.proyectofinal_tripsplit.utilities.Gasto
import armenta.jose.proyectofinal_tripsplit.utilities.GastoAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersona
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersonaAdapter
import java.util.concurrent.atomic.AtomicInteger

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
    private lateinit var tvSaldoDisponible: TextView
    private lateinit var btnFlechaAtras: ImageButton
    private var gastosList: List<Gasto> = emptyList()
    private lateinit var groupId: String
    private var currentUserId: String? = null
    private val TAG = "PantallaPrincipal"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_principal)

        db = FirebaseDatabase.getInstance()
        dbRefGrupos = db.getReference("grupos")
        dbRefGastos = db.getReference("gastos")
        dbRefUsuarios = db.getReference("Usuarios")
        auth = FirebaseAuth.getInstance()
        currentUserId = auth.currentUser?.uid

        groupId = intent.getStringExtra("groupId") ?: intent.getStringExtra("GRUPO_ID") ?: ""
        if (groupId.isBlank()) {
            handleError("Error: No se pudo cargar el grupo")
            finish()
            return
        }

        findViews()
        setupListeners()
        loadGroupData()
        listenForExpenseChanges()

    }

    private fun findViews() {
        listViewPersonas = findViewById(R.id.lista_monto_pagar_persona)
        listViewGastos = findViewById(R.id.lista_gastos)
        totalGastadoTextView = findViewById(R.id.tv_total_gastado)
        btnAgregarGasto = findViewById(R.id.btn_agregar_gasto)
        btnReporteGastos = findViewById(R.id.btn_ver_reporte_gastos)
        tvCodigoGrupo = findViewById(R.id.tv_codigo_grupo)
        tvNombreViaje = findViewById(R.id.tv_nombre_viaje)
        tvSaldoDisponible = findViewById(R.id.tvSaldoDisponbile)
        btnFlechaAtras = findViewById(R.id.btn_flecha_atras)
    }

    private fun setupListeners() {
        btnFlechaAtras.setOnClickListener {
            finish()
        }

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

        listViewGastos.setOnItemClickListener { _, _, position, _ ->
            if (position < gastosList.size) {
                val gastoSeleccionado = gastosList[position]
                val intentDetalle = Intent(this, DetalleGastoActivity::class.java)
                intentDetalle.putExtra("GASTO_ID", gastoSeleccionado.id)
                intentDetalle.putExtra("GRUPO_ID", groupId)
                startActivity(intentDetalle)
            }
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
                handleError("Error al cargar datos del grupo", error)
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
                Log.d(TAG, "Gastos actualizados para grupo $groupId: ${gastosList.size}")

                updateExpensesUI(gastosList)
                updateTotalSpent(gastosList)
                updateUserBalancesAndPersonalSaldo(gastosList)
            }
            override fun onCancelled(error: DatabaseError) {
                handleError("Error al escuchar gastos", error)
            }
        })
    }


    private fun updateExpensesUI(gastos: List<Gasto>) {
        val adapter = GastoAdapter(this, gastos)
        listViewGastos.adapter = adapter
        ListViewUtility.setListViewHeightBasedOnChildren(listViewGastos)
    }

    private fun updateTotalSpent(gastos: List<Gasto>) {
        val total = gastos.sumOf { it.montoTotal ?: 0.0 }
        totalGastadoTextView.text = getString(R.string.currency_format, total)
    }

    private fun updateUserBalancesAndPersonalSaldo(gastos: List<Gasto>) {
        if (currentUserId == null) {
            Log.w(TAG, "No se puede calcular saldo personal, usuario no logueado.")
            tvSaldoDisponible.text = "N/A"
            return
        }

        dbRefGrupos.child(groupId).child("miembrosIds")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val miembrosIds = snapshot.children.mapNotNull { it.getValue(String::class.java) }
                    if (miembrosIds.isEmpty()) {
                        Log.w(TAG, "No hay miembros en el grupo $groupId para calcular balances.")
                        listViewPersonas.adapter = null
                        tvSaldoDisponible.text = getString(R.string.currency_format, 0.0)
                        return
                    }

                    val balances = calculateBalances(miembrosIds, gastos)
                    Log.d(TAG, "Balances calculados: $balances")

                    val personalBalance = balances[currentUserId] ?: 0.0
                    tvSaldoDisponible.text = getString(R.string.currency_format, personalBalance)
                    Log.d(TAG, "Saldo personal para $currentUserId: $personalBalance")


                    val personasList = mutableListOf<PagarPorPersona>()
                    val userFetchCounter = AtomicInteger(miembrosIds.size)

                    for (uid in miembrosIds) {
                        dbRefUsuarios.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(userSnapshot: DataSnapshot) {
                                val nombre = userSnapshot.child("nombre").getValue(String::class.java) ?: "Usuario"
                                val apellido = userSnapshot.child("apellido").getValue(String::class.java) ?: ""
                                val nombreCompleto = "$nombre $apellido".trim().ifEmpty { "Usuario ($uid)" }
                                val fotoPerfilId = R.mipmap.image_default_user

                                val balance = balances[uid] ?: 0.0
                                val formattedAmount = getString(R.string.currency_format, kotlin.math.abs(balance))

                                val displayStatus = when {
                                    balance < -0.01 -> getString(R.string.debe_format, formattedAmount)
                                    balance > 0.01 -> getString(R.string.recibe_format, formattedAmount)
                                    else -> getString(R.string.a_la_par)
                                }

                                personasList.add(PagarPorPersona(nombreCompleto, displayStatus, fotoPerfilId))
                                if (userFetchCounter.decrementAndGet() == 0) {
                                    personasList.sortBy { it.nombre }
                                    val adapter = PagarPorPersonaAdapter(this@pantalla_principal, personasList)
                                    listViewPersonas.adapter = adapter
                                    ListViewUtility.setListViewHeightBasedOnChildren(listViewPersonas)
                                    Log.d(TAG, "Lista PagarPorPersona actualizada.")
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {
                                handleError("Error al cargar usuario $uid", error)
                                if (userFetchCounter.decrementAndGet() == 0) {
                                    val adapter = PagarPorPersonaAdapter(this@pantalla_principal, personasList)
                                    listViewPersonas.adapter = adapter
                                    ListViewUtility.setListViewHeightBasedOnChildren(listViewPersonas)
                                }
                            }
                        })
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    handleError("Error al obtener miembros para calcular balances", error)
                }
            })
    }


    private fun calculateBalances(memberIds: List<String>, expenses: List<Gasto>): Map<String, Double> {
        val balances = mutableMapOf<String, Double>().apply {
            memberIds.forEach { put(it, 0.0) }
        }

        for (expense in expenses) {
            val monto = expense.montoTotal ?: 0.0
            if (monto <= 0) continue
            val pagador = expense.pagadorId
            val participantes = expense.participantesIds

            if (pagador.isNullOrBlank() || participantes.isNullOrEmpty()) continue

            val share = monto / participantes.size
            for (p in participantes) {
                balances[p] = (balances[p] ?: 0.0) - share
            }
            balances[pagador] = (balances[pagador] ?: 0.0) + monto
        }

        balances.keys.forEach { uid ->
            if (kotlin.math.abs(balances[uid]!!) < 0.01) {
                balances[uid] = 0.0
            }
        }
        return balances
    }

    object ListViewUtility {
        fun setListViewHeightBasedOnChildren(listView: ListView) {
            val listAdapter: ListAdapter = listView.adapter ?: run {
                val params = listView.layoutParams
                params.height = 0
                listView.layoutParams = params
                listView.requestLayout()
                return
            }

            var totalHeight = listView.paddingTop + listView.paddingBottom
            val desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.width, View.MeasureSpec.AT_MOST)

            if (listAdapter.count == 0) {
                totalHeight = 0
            } else {
                for (i in 0 until listAdapter.count) {
                    val listItem: View = listAdapter.getView(i, null, listView)
                    if (listItem.layoutParams == null) {
                        listItem.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                    }
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED)
                    totalHeight += listItem.measuredHeight
                }
                totalHeight += (listView.dividerHeight * (listAdapter.count - 1))
            }

            val params: ViewGroup.LayoutParams = listView.layoutParams
            params.height = totalHeight.coerceAtLeast(0)
            listView.layoutParams = params
            listView.requestLayout()
        }
    }

    private fun handleError(message: String, error: DatabaseError? = null) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        if (error != null) {
            Log.e(TAG, message, error.toException())
        } else {
            Log.e(TAG, message)
        }
    }

}
