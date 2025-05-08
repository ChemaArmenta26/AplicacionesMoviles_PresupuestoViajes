package armenta.jose.proyectofinal_tripsplit

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ListAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import armenta.jose.proyectofinal_tripsplit.utilities.*
import java.util.concurrent.atomic.AtomicInteger


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
    private val maxHeightDp = 150
    private var maxHeightPx: Int = 0
    private lateinit var listaSaldoPendiente: ListView
    private lateinit var listaReporteIntegrantes: ListView

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

        maxHeightPx = (maxHeightDp * resources.displayMetrics.density).toInt()

        groupId = intent.getStringExtra("GRUPO_ID") ?: ""
        if (groupId.isBlank()) {
            Log.e("ReporteGastos", "No se recibió GRUPO_ID.")
            finish()
            return
        }

        db = FirebaseDatabase.getInstance()
        dbRefGastos = db.getReference("gastos")
        dbRefUsuarios = db.getReference("Usuarios")
        findViews()
        findViewById<ImageButton>(R.id.btn_flecha_atras).setOnClickListener {
            finish()
        }
        cargarDatosDesdeFirebase()
    }

    private fun findViews() {
        barHospedaje = findViewById(R.id.barHospedaje)
        barComida = findViewById(R.id.barComida)
        barTraslado = findViewById(R.id.barTraslado)
        barEntretenimiento = findViewById(R.id.barEntretenimiento)
        barOtros = findViewById(R.id.barOtros)
        listaSaldoPendiente = findViewById(R.id.lista_saldo_pendiente)
        listaReporteIntegrantes = findViewById(R.id.lista_reporte_integrantes)
    }

    private fun cargarDatosDesdeFirebase() {
        val query = dbRefGastos.orderByChild("groupId").equalTo(groupId)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val gastosDelGrupo = snapshot.children.mapNotNull {
                    it.getValue(Gasto::class.java)
                }
                Log.d("ReporteGastos", "Gastos encontrados para el grupo $groupId: ${gastosDelGrupo.size}")
                procesarGastos(gastosDelGrupo)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ReporteGastos", "Error al leer gastos para grupo $groupId", error.toException())
            }
        })
    }

    private fun procesarGastos(gastos: List<Gasto>) {
        val saldos = mutableMapOf<String, Double>()
        val totalesPagados = mutableMapOf<String, Double>()

        categorias.keys.forEach { categorias[it] = 0.0 }

        if (gastos.isEmpty()) {
            Log.w("ReporteGastos", "No hay gastos para procesar en este grupo.")
            actualizarGrafica()
            findViewById<ListView>(R.id.lista_saldo_pendiente).adapter = null
            findViewById<ListView>(R.id.lista_reporte_integrantes).adapter = null
            return
        }


        for (gasto in gastos) {
            val monto = gasto.montoTotal ?: 0.0
            if (monto <= 0) continue

            val pagadorId = gasto.pagadorId
            val participantesIds = gasto.participantesIds
            val categoria = gasto.categoria ?: "Otros"

            if (pagadorId.isNullOrBlank() || participantesIds.isNullOrEmpty()) {
                Log.w("ReporteGastos", "Gasto ${gasto.id} ignorado por falta de pagador o participantes.")
                continue
            }

            val keyCategoria = categorias.keys.find { it.equals(categoria, ignoreCase = true) } ?: "Otros"
            categorias[keyCategoria] = (categorias[keyCategoria] ?: 0.0) + monto
            val share = monto / participantesIds.size

            participantesIds.forEach { participanteUid ->
                saldos[participanteUid] = (saldos[participanteUid] ?: 0.0) - share
            }
            saldos[pagadorId] = (saldos[pagadorId] ?: 0.0) + monto

            totalesPagados[pagadorId] = (totalesPagados[pagadorId] ?: 0.0) + monto
        }

        saldos.keys.forEach { uid ->
            if (kotlin.math.abs(saldos[uid]!!) < 0.01) {
                saldos[uid] = 0.0
            }
        }


        Log.d("ReporteGastos", "Categorías calculadas: $categorias")
        Log.d("ReporteGastos", "Saldos calculados: $saldos")
        Log.d("ReporteGastos", "Totales pagados calculados: $totalesPagados")

        actualizarGrafica()
        cargarNombresYActualizarListas(saldos, totalesPagados)
    }

    private fun actualizarGrafica() {
        val maxValor = categorias.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0

        fun ajustarBarra(view: View, valor: Double) {
            val alturaCalculada = ((valor / maxValor) * maxHeightPx).toInt()
            val params = view.layoutParams
            params.height = alturaCalculada.coerceAtLeast(1)
            view.layoutParams = params
        }

        ajustarBarra(barHospedaje, categorias["Hospedaje"] ?: 0.0)
        ajustarBarra(barComida, categorias["Comida"] ?: 0.0)
        ajustarBarra(barTraslado, categorias["Traslado"] ?: 0.0)
        ajustarBarra(barEntretenimiento, categorias["Entretenimiento"] ?: 0.0)
        ajustarBarra(barOtros, categorias["Otros"] ?: 0.0)
        Log.d("ReporteGastos", "Gráfica actualizada.")
    }

    private fun cargarNombresYActualizarListas(
        saldos: Map<String, Double>,
        totalesPagados: Map<String, Double>
    ) {
        val allUserIds = (saldos.keys + totalesPagados.keys).distinct()

        if (allUserIds.isEmpty()) {
            Log.w("ReporteGastos", "No hay usuarios involucrados para mostrar en listas.")
            listaSaldoPendiente.adapter = null
            listaReporteIntegrantes.adapter = null
            return
        }

        val listaPendientesData = mutableListOf<GastoPendiente>()
        val listaTotalesData = mutableListOf<TotalGastado>()
        val userInfoMap = mutableMapOf<String, Pair<String, Int>>()
        val fetchCounter = AtomicInteger(allUserIds.size)

        Log.d("ReporteGastos", "Cargando nombres para ${allUserIds.size} usuarios...")

        for (uid in allUserIds) {
            dbRefUsuarios.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Usuario"
                    val apellido = snapshot.child("apellido").getValue(String::class.java) ?: ""
                    val nombreCompleto = "$nombre $apellido".trim().ifEmpty { "Usuario ($uid)" }
                    val fotoPerfilId = R.mipmap.image_default_user

                    userInfoMap[uid] = Pair(nombreCompleto, fotoPerfilId)

                    if (fetchCounter.decrementAndGet() == 0) {
                        Log.d("ReporteGastos", "Todos los nombres cargados. Construyendo listas finales.")
                        for (userId in allUserIds) {
                            val userData = userInfoMap[userId] ?: continue
                            val nombreUsuario = userData.first
                            val fotoId = userData.second

                            val totalPagado = totalesPagados[userId] ?: 0.0
                            val totalStr = getString(R.string.currency_format, totalPagado)
                            listaTotalesData.add(TotalGastado(nombreUsuario, totalStr, fotoId))

                            val saldo = saldos[userId] ?: 0.0
                            if (saldo < -0.01) {
                                val deudaStr = getString(R.string.currency_format, kotlin.math.abs(saldo))
                                listaPendientesData.add(GastoPendiente(nombreUsuario, deudaStr, fotoId))
                            }
                        }

                        val adapterPendientes = GastoPendienteAdapter(this@reporte_gastos, listaPendientesData)
                        listaSaldoPendiente.adapter = adapterPendientes
                        ListViewUtility.setListViewHeightBasedOnChildren(listaSaldoPendiente)

                        val adapterTotales = TotalGastadoAdapter(this@reporte_gastos, listaTotalesData)
                        listaReporteIntegrantes.adapter = adapterTotales
                        ListViewUtility.setListViewHeightBasedOnChildren(listaReporteIntegrantes)

                        Log.d("ReporteGastos", "Listas actualizadas y altura ajustada.")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ReporteGastos", "Error al cargar datos del usuario $uid", error.toException())
                    userInfoMap[uid] = Pair("Error ($uid)", R.mipmap.image_default_user)
                    if (fetchCounter.decrementAndGet() == 0) {

                    }
                }
            })
        }
    }

    object ListViewUtility {
        fun setListViewHeightBasedOnChildren(listView: ListView) {
            val listAdapter: ListAdapter = listView.adapter ?: return

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
            params.height = totalHeight
            listView.layoutParams = params
            listView.requestLayout()
        }
    }
}


