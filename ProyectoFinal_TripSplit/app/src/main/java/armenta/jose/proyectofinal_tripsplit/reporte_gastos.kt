
package armenta.jose.proyectofinal_tripsplit


import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import armenta.jose.proyectofinal_tripsplit.utilities.*


class reporte_gastos : AppCompatActivity() {


    private lateinit var db: FirebaseFirestore
    private var groupId: String? = null
    private var listaGastos = mutableListOf<Gasto>()
    private var listaMiembros = mutableListOf<MiembroInfo>()


    // Categorías actualizadas según tus especificaciones
    private val CATEGORIAS = mapOf(
        "Hospedaje" to 0.0,
        "Comida" to 0.0,
        "Traslado" to 0.0,
        "Entretenimiento" to 0.0,
        "Otros" to 0.0
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reporte_gastos)


        // Inicializar Firebase
        db = FirebaseFirestore.getInstance()


        // Obtener el ID del grupo de los extras
        groupId = intent.getStringExtra("GROUP_ID")
        Log.d("ReporteGastos", "GroupID recibido: $groupId")


        if (groupId.isNullOrEmpty()) {
            Toast.makeText(this, "ID de grupo no válido", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        // Inicializar vistas
        val btnFlechaAtras = findViewById<ImageButton>(R.id.btn_flecha_atras)
        btnFlechaAtras.setOnClickListener {
            finish()
        }


        // Verificar si el usuario está autenticado
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para ver los reportes", Toast.LENGTH_SHORT).show()
            finish()
            return
        }


        // Cargar datos del grupo
        cargarDatosGrupo()
    }


    private fun cargarDatosGrupo() {
        Log.d("ReporteGastos", "Intentando cargar datos del grupo: $groupId")


        // Obtener datos del grupo y sus gastos
        db.collection("grupos").document(groupId!!)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    Log.d("ReporteGastos", "Documento del grupo encontrado")
                    val grupo = documentSnapshot.toObject(Grupo::class.java)
                    if (grupo != null) {
                        // Mostrar nombre del grupo si tienes un TextView para ello
                        val tvNombreGrupo = findViewById<TextView>(R.id.tv_nombre_grupo)
                        if (tvNombreGrupo != null) {
                            tvNombreGrupo.text = grupo.nombre
                        }


                        // Cargar miembros del grupo
                        if (!grupo.miembrosIds.isNullOrEmpty()) {
                            cargarMiembrosGrupo(grupo.miembrosIds!!)
                        } else {
                            Log.w("ReporteGastos", "El grupo no tiene miembros")
                            mostrarReporteVacio("El grupo no tiene miembros")
                        }


                        // Cargar gastos del grupo
                        if (!grupo.gastosIds.isNullOrEmpty()) {
                            cargarGastosGrupo(grupo.gastosIds!!)
                        } else {
                            Log.w("ReporteGastos", "El grupo no tiene gastos")
                            mostrarReporteVacio("No hay gastos registrados en este grupo")
                        }
                    } else {
                        Log.e("ReporteGastos", "No se pudo convertir el documento a objeto Grupo")
                        mostrarReporteVacio("Error al procesar los datos del grupo")
                    }
                } else {
                    Log.e("ReporteGastos", "El documento del grupo no existe")
                    Toast.makeText(this, "No se encontró información del grupo", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener { e ->
                Log.e("ReporteGastos", "Error al cargar datos del grupo", e)
                Toast.makeText(this, "Error de permisos: ${e.message}", Toast.LENGTH_LONG).show()
                finish()
            }
    }


    private fun cargarMiembrosGrupo(miembrosIds: List<String>) {
        Log.d("ReporteGastos", "Cargando información de ${miembrosIds.size} miembros")
        listaMiembros.clear()


        val miembrosCompletados = arrayOfNulls<Boolean>(miembrosIds.size)


        // Obtener información de cada miembro
        for ((index, uid) in miembrosIds.withIndex()) {
            db.collection("usuarios").document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val nombre = document.getString("nombre") ?: "Usuario"
                    listaMiembros.add(MiembroInfo(uid, nombre))


                    // Marcar este miembro como completado
                    miembrosCompletados[index] = true


                    // Verificar si todos los miembros se han cargado
                    if (miembrosCompletados.all { it == true } && listaGastos.isNotEmpty()) {
                        Log.d("ReporteGastos", "Todos los miembros cargados, actualizando UI")
                        actualizarUI()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ReporteGastos", "Error al cargar datos de usuario $uid", e)
                    miembrosCompletados[index] = true


                    // A pesar del error, intentamos continuar
                    if (miembrosCompletados.all { it == true } && listaGastos.isNotEmpty()) {
                        actualizarUI()
                    }
                }
        }


        // Si no hay miembros para cargar (caso extremo), actualizamos UI si tenemos gastos
        if (miembrosIds.isEmpty() && listaGastos.isNotEmpty()) {
            actualizarUI()
        }
    }


    private fun cargarGastosGrupo(gastosIds: List<String>) {
        Log.d("ReporteGastos", "Cargando información de ${gastosIds.size} gastos")
        listaGastos.clear()


        val gastosCompletados = arrayOfNulls<Boolean>(gastosIds.size)


        // Obtener información de cada gasto
        for ((index, gastoId) in gastosIds.withIndex()) {
            db.collection("gastos").document(gastoId)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val gasto = document.toObject(Gasto::class.java)
                        if (gasto != null) {
                            gasto.id = document.id
                            listaGastos.add(gasto)
                            Log.d("ReporteGastos", "Gasto cargado: ${gasto.nombre}, monto: ${gasto.montoTotal}, categoría: ${gasto.categoria}")
                        }
                    }


                    // Marcar este gasto como completado (ya sea que exista o no)
                    gastosCompletados[index] = true


                    // Verificar si todos los gastos se han procesado y tenemos los miembros
                    if (gastosCompletados.all { it == true } && listaMiembros.isNotEmpty()) {
                        Log.d("ReporteGastos", "Todos los gastos cargados, actualizando UI")
                        actualizarUI()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("ReporteGastos", "Error al cargar gasto $gastoId", e)
                    gastosCompletados[index] = true


                    // A pesar del error, intentamos continuar
                    if (gastosCompletados.all { it == true } && listaMiembros.isNotEmpty()) {
                        actualizarUI()
                    }
                }
        }


        // Si no hay gastos para cargar, mostramos la interfaz vacía
        if (gastosIds.isEmpty()) {
            mostrarReporteVacio("No hay gastos registrados en este grupo")
        }
    }


    private fun mostrarReporteVacio(mensaje: String = "No hay gastos registrados en este grupo") {
        Log.d("ReporteGastos", "Mostrando reporte vacío: $mensaje")
        // Mostrar interfaz indicando que no hay gastos
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()


        // Configurar barras con altura 0
        val barHospedaje = findViewById<View>(R.id.barAlimentos) // Ajustar nombres según tu layout
        val barTraslado = findViewById<View>(R.id.barTransporte)
        val barEntretenimiento = findViewById<View>(R.id.barEntretenimiento)
        val barOtros = findViewById<View>(R.id.barOtros)


        barHospedaje.layoutParams.height = 0
        barTraslado.layoutParams.height = 0
        barEntretenimiento.layoutParams.height = 0
        barOtros.layoutParams.height = 0


        // Refrescar la vista
        barHospedaje.requestLayout()
        barTraslado.requestLayout()
        barEntretenimiento.requestLayout()
        barOtros.requestLayout()


        // Configurar listas vacías
        val listViewSaldoPendiente = findViewById<ListView>(R.id.lista_saldo_pendiente)
        val adapterGastos = GastoPendienteAdapter(this, emptyList())
        listViewSaldoPendiente.adapter = adapterGastos


        val listViewTotalGastado = findViewById<ListView>(R.id.lista_reporte_integrantes)
        val adapterTotal = TotalGastadoAdapter(this, emptyList())
        listViewTotalGastado.adapter = adapterTotal
    }


    private fun actualizarUI() {
        if (listaGastos.isEmpty()) {
            mostrarReporteVacio()
            return
        }


        Log.d("ReporteGastos", "Actualizando UI con ${listaGastos.size} gastos y ${listaMiembros.size} miembros")


        // Calcular totales por categoría para las barras
        val gastoPorCategoria = CATEGORIAS.toMutableMap()


        // Mapas para cálculos de saldo
        val totalGastadoPorUsuario = mutableMapOf<String, Double>()
        val deudaPorUsuario = mutableMapOf<String, Double>()


        // Inicializar los mapas
        for (miembro in listaMiembros) {
            totalGastadoPorUsuario[miembro.uid] = 0.0
            deudaPorUsuario[miembro.uid] = 0.0
        }


        // Calcular montos por categoría y por usuario
        for (gasto in listaGastos) {
            val categoria = gasto.categoria ?: "Otros"
            val montoTotal = gasto.montoTotal ?: 0.0
            val pagadorId = gasto.pagadorId
            val participantesIds = gasto.participantesIds ?: listOf()


            // Actualizar gasto por categoría (ajustando a tus categorías)
            when (categoria) {
                "Hospedaje" -> gastoPorCategoria["Hospedaje"] = gastoPorCategoria["Hospedaje"]!! + montoTotal
                "Comida" -> gastoPorCategoria["Comida"] = gastoPorCategoria["Comida"]!! + montoTotal
                "Traslado" -> gastoPorCategoria["Traslado"] = gastoPorCategoria["Traslado"]!! + montoTotal
                "Entretenimiento" -> gastoPorCategoria["Entretenimiento"] = gastoPorCategoria["Entretenimiento"]!! + montoTotal
                else -> gastoPorCategoria["Otros"] = gastoPorCategoria["Otros"]!! + montoTotal
            }


            // Actualizar total gastado por el pagador
            if (pagadorId != null) {
                totalGastadoPorUsuario[pagadorId] = totalGastadoPorUsuario[pagadorId]!! + montoTotal
            }


            // Calcular deudas (cada participante debe su parte)
            if (participantesIds.isNotEmpty() && pagadorId != null) {
                val montoPorParticipante = montoTotal / participantesIds.size


                for (participanteId in participantesIds) {
                    if (participanteId != pagadorId) {
                        // Si no es el pagador, debe el monto
                        val deudaActual = deudaPorUsuario[participanteId] ?: 0.0
                        deudaPorUsuario[participanteId] = deudaActual + montoPorParticipante
                    }
                }
            }
        }


        Log.d("ReporteGastos", "Gastos por categoría: $gastoPorCategoria")
        Log.d("ReporteGastos", "Total gastado por usuario: $totalGastadoPorUsuario")
        Log.d("ReporteGastos", "Deudas por usuario: $deudaPorUsuario")


        // Actualizar gráfico de barras
        actualizarGraficaBarras(gastoPorCategoria)


        // Actualizar lista de saldos pendientes
        actualizarListaSaldoPendiente(deudaPorUsuario)


        // Actualizar lista de total gastado
        actualizarListaTotalGastado(totalGastadoPorUsuario)
    }


    private fun actualizarGraficaBarras(gastoPorCategoria: Map<String, Double>) {
        // Referencias a las barras en el layout
        // Nota: Asegúrate de que estos IDs coincidan con tu layout
        val barHospedaje = findViewById<View>(R.id.barAlimentos)      // Ajusta estos IDs
        val barTraslado = findViewById<View>(R.id.barTransporte)      // para que coincidan
        val barEntretenimiento = findViewById<View>(R.id.barEntretenimiento)
        val barOtros = findViewById<View>(R.id.barOtros)


        // Calcular el valor máximo para escalar las barras
        val maxValue = gastoPorCategoria.values.maxOrNull() ?: 0.0
        val maxHeight = 200f // Altura máxima de la barra en dp


        // Si no hay gastos, mostrar todas las barras en 0
        if (maxValue == 0.0) {
            barHospedaje.layoutParams.height = 0
            barTraslado.layoutParams.height = 0
            barEntretenimiento.layoutParams.height = 0
            barOtros.layoutParams.height = 0
        } else {
            // Calcular la altura proporcional de cada barra
            barHospedaje.layoutParams.height = (gastoPorCategoria["Hospedaje"]!! / maxValue * maxHeight).toInt()
            barTraslado.layoutParams.height = (gastoPorCategoria["Traslado"]!! / maxValue * maxHeight).toInt()
            barEntretenimiento.layoutParams.height = (gastoPorCategoria["Entretenimiento"]!! / maxValue * maxHeight).toInt()
            barOtros.layoutParams.height = (gastoPorCategoria["Otros"]!! / maxValue * maxHeight).toInt()
        }


        // Actualizar las vistas
        barHospedaje.requestLayout()
        barTraslado.requestLayout()
        barEntretenimiento.requestLayout()
        barOtros.requestLayout()
    }


    private fun actualizarListaSaldoPendiente(deudaPorUsuario: Map<String, Double>) {
        val saldosPendientes = mutableListOf<GastoPendiente>()


        // Crear la lista de GastoPendiente solo con usuarios que tienen deuda
        for (miembro in listaMiembros) {
            val deuda = deudaPorUsuario[miembro.uid] ?: 0.0
            if (deuda > 0) {
                saldosPendientes.add(GastoPendiente(
                    miembro.nombre,
                    "$${String.format("%.2f", deuda)}",
                    R.mipmap.image_default_user
                ))
            }
        }


        Log.d("ReporteGastos", "Saldos pendientes a mostrar: ${saldosPendientes.size}")


        // Actualizar el ListView
        val listViewSaldoPendiente = findViewById<ListView>(R.id.lista_saldo_pendiente)
        val adapterGastos = GastoPendienteAdapter(this, saldosPendientes)
        listViewSaldoPendiente.adapter = adapterGastos
    }


    private fun actualizarListaTotalGastado(totalGastadoPorUsuario: Map<String, Double>) {
        val totalesGastados = mutableListOf<TotalGastado>()


        // Crear la lista de TotalGastado para todos los usuarios
        for (miembro in listaMiembros) {
            val totalGastado = totalGastadoPorUsuario[miembro.uid] ?: 0.0
            totalesGastados.add(TotalGastado(
                miembro.nombre,
                "$${String.format("%.2f", totalGastado)}",
                R.mipmap.image_default_user
            ))
        }


        // Ordenar por monto de mayor a menor
        totalesGastados.sortByDescending {
            it.total.replace("$", "").toDoubleOrNull() ?: 0.0
        }


        Log.d("ReporteGastos", "Totales gastados a mostrar: ${totalesGastados.size}")


        // Actualizar el ListView
        val listViewTotalGastado = findViewById<ListView>(R.id.lista_reporte_integrantes)
        val adapterTotal = TotalGastadoAdapter(this, totalesGastados)
        listViewTotalGastado.adapter = adapterTotal
    }
}
