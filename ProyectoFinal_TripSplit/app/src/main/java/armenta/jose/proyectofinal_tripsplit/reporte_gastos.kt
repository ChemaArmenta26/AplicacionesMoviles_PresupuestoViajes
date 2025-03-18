package armenta.jose.proyectofinal_tripsplit

import android.os.Bundle
import android.view.View
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendienteAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendiente
import armenta.jose.proyectofinal_tripsplit.utilities.TotalGastado
import armenta.jose.proyectofinal_tripsplit.utilities.TotalGastadoAdapter

class reporte_gastos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reporte_gastos)

        //para lo de la grafica de barras
        val barAlimentos = findViewById<View>(R.id.barAlimentos)
        val barTransporte = findViewById<View>(R.id.barTransporte)
        val barEntretenimiento = findViewById<View>(R.id.barEntretenimiento)
        val barOtros = findViewById<View>(R.id.barOtros)

        
        val valoresCategorias = mapOf(
            "Alimentos" to 100f,        // 100% de la altura máxima (100dp)
            "Transporte" to 150f,       // 150dp de altura
            "Entretenimiento" to 80f,   // 80dp de altura
            "Otros" to 120f             // 120dp de altura
        )


        val maxHeight = 200f // Altura máxima de la barra

        // Ajuste de altura de las barras
        barAlimentos.layoutParams.height = (valoresCategorias["Alimentos"]!! / maxHeight * 200).toInt()
        barTransporte.layoutParams.height = (valoresCategorias["Transporte"]!! / maxHeight * 200).toInt()
        barEntretenimiento.layoutParams.height = (valoresCategorias["Entretenimiento"]!! / maxHeight * 200).toInt()
        barOtros.layoutParams.height = (valoresCategorias["Otros"]!! / maxHeight * 200).toInt()

        // Configurar ListView de saldo pendiente
        val listViewSaldoPendiente = findViewById<ListView>(R.id.lista_saldo_pendiente)
        val listaGastos = listOf(
            GastoPendiente("Juan", "$500", R.mipmap.image_default_user),
            GastoPendiente("María", "$200", R.mipmap.image_default_user),
            GastoPendiente("Carlos", "$350", R.mipmap.image_default_user),
            GastoPendiente("Ana", "$400", R.mipmap.image_default_user)
        )
        val adapterGastos = GastoPendienteAdapter(this, listaGastos)
        listViewSaldoPendiente.adapter = adapterGastos

        // Configurar ListView de total gastado
        val listViewTotalGastado = findViewById<ListView>(R.id.lista_reporte_integrantes)
        val listaTotales = listOf(
            TotalGastado("Juan", "$1200", R.mipmap.image_default_user),
            TotalGastado("María", "$950", R.mipmap.image_default_user),
            TotalGastado("Carlos", "$1500", R.mipmap.image_default_user),
            TotalGastado("Ana", "$1100", R.mipmap.image_default_user)
        )
        val adapterTotal = TotalGastadoAdapter(this, listaTotales)
        listViewTotalGastado.adapter = adapterTotal
    }
}