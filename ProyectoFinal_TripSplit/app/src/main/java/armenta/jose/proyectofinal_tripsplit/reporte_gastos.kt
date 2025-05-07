package armenta.jose.proyectofinal_tripsplit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import armenta.jose.proyectofinal_tripsplit.utilities.*

class reporte_gastos : AppCompatActivity() {

    private lateinit var db: FirebaseDatabase
    private lateinit var dbRefGastos: DatabaseReference
    private lateinit var dbRefUsuarios: DatabaseReference

    private lateinit var groupId: String

    private lateinit var barHospedaje: View
    private lateinit var barComida: View
    private lateinit var barTraslado: View
    private lateinit var barEntretenimiento: View
    private lateinit var barOtros: View
    private val maxHeight = 200f

    private val categorias = mutableMapOf(
        "Hospedaje" to 0.0,
        "Comida" to 0.0,
        "Traslado" to 0.0,
        "Entretenimiento" to 0.0,
        "Otros" to 0.0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reporte_gastos)

        groupId = intent.getStringExtra("GRUPO_ID") ?: ""
        if (groupId.isBlank()) {
            finish()
            return
        }

        db = FirebaseDatabase.getInstance()
        dbRefGastos = db.getReference("gastos")
        dbRefUsuarios = db.getReference("Usuarios")

        barHospedaje = findViewById(R.id.barHospedaje)
        barComida = findViewById(R.id.barComida)
        barTraslado = findViewById(R.id.barTraslado)
        barEntretenimiento = findViewById(R.id.barEntretenimiento)
        barOtros = findViewById(R.id.barOtros)

        findViewById<ImageButton>(R.id.btn_flecha_atras).setOnClickListener {
            finish()
        }

        cargarDatosDesdeFirebase()
    }

    private fun cargarDatosDesdeFirebase() {
        dbRefGastos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gastos = snapshot.children.mapNotNull {
                    it.getValue(Gasto::class.java)
                }.filter { it.groupId == groupId }

                procesarGastos(gastos)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReporteGastos", "Error al leer gastos", error.toException())
            }
        })
    }

    private fun procesarGastos(gastos: List<Gasto>) {
        val saldos = mutableMapOf<String, Double>()
        val totales = mutableMapOf<String, Double>()

        // Reiniciar categorías
        categorias.keys.forEach { categorias[it] = 0.0 }

        for (gasto in gastos) {
            val monto = gasto.montoTotal ?: 0.0
            val pagador = gasto.pagadorId ?: continue
            val participantes = gasto.participantesIds ?: continue
            val categoria = gasto.categoria ?: "Otros"

            // Categoría
            val key = if (categoria in categorias) categoria else "Otros"
            categorias[key] = categorias[key]!! + monto

            // Repartir gasto
            val share = monto / participantes.size
            participantes.forEach { uid ->
                saldos[uid] = (saldos[uid] ?: 0.0) - share
            }
            saldos[pagador] = (saldos[pagador] ?: 0.0) + monto

            // Total pagado
            totales[pagador] = (totales[pagador] ?: 0.0) + monto
        }

        actualizarGrafica()
        cargarListasDesdeFirebase(saldos, totales)
    }

    private fun actualizarGrafica() {
        val maxValor = categorias.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

        fun ajustar(view: View, valor: Double) {
            view.layoutParams.height = ((valor / maxValor) * maxHeight).toInt()
            view.requestLayout()
        }

        ajustar(barHospedaje, categorias["Hospedaje"]!!)
        ajustar(barComida, categorias["Comida"]!!)
        ajustar(barTraslado, categorias["Traslado"]!!)
        ajustar(barEntretenimiento, categorias["Entretenimiento"]!!)
        ajustar(barOtros, categorias["Otros"]!!)
    }

    private fun cargarListasDesdeFirebase(
        saldos: Map<String, Double>,
        totales: Map<String, Double>
    ) {
        val listaPendientes = mutableListOf<GastoPendiente>()
        val listaTotales = mutableListOf<TotalGastado>()

        if (saldos.isEmpty()) return

        for ((uid, saldo) in saldos) {
            dbRefUsuarios.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Usuario"
                    val fotoPerfil = snapshot.child("fotoPerfil").getValue(String::class.java)?.toIntOrNull()
                        ?: R.mipmap.image_default_user

                    // Saldos negativos (deudas)
                    if (saldo < 0) {
                        val deudaStr = getString(R.string.currency_format, kotlin.math.abs(saldo))
                        listaPendientes.add(GastoPendiente(nombre, deudaStr, fotoPerfil))
                    }

                    // Total gastado
                    val total = totales[uid] ?: 0.0
                    val totalStr = getString(R.string.currency_format, total)
                    listaTotales.add(TotalGastado(nombre, totalStr, fotoPerfil))

                    // Cuando ya se han cargado todos
                    if (listaTotales.size == saldos.size) {
                        val adapterPendientes = GastoPendienteAdapter(this@reporte_gastos, listaPendientes)
                        findViewById<ListView>(R.id.lista_saldo_pendiente).adapter = adapterPendientes

                        val adapterTotales = TotalGastadoAdapter(this@reporte_gastos, listaTotales)
                        findViewById<ListView>(R.id.lista_reporte_integrantes).adapter = adapterTotales
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ReporteGastos", "Error al cargar usuario $uid", error.toException())
                }
            })
        }
    }
}
