package armenta.jose.proyectofinal_tripsplit

import android.graphics.Color
import android.os.Bundle
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendienteAdapter
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendiente
import armenta.jose.proyectofinal_tripsplit.utilities.TotalGastado
import armenta.jose.proyectofinal_tripsplit.utilities.TotalGastadoAdapter

class reporte_gastos : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reporte_gastos)

        val pieChart = findViewById<PieChart>(R.id.pieChart)
        setupPieChart(pieChart)

        val listView = findViewById<ListView>(R.id.lista_saldo_pendiente)

        // Datos de prueba (mocks)
        val listaGastos = listOf(
            GastoPendiente("Juan", "$500", R.mipmap.image_default_user),
            GastoPendiente("María", "$200", R.mipmap.image_default_user),
            GastoPendiente("Carlos", "$350", R.mipmap.image_default_user),
            GastoPendiente("Ana", "$400", R.mipmap.image_default_user)
        )

        val adapter = GastoPendienteAdapter(this, listaGastos)
        listView.adapter = adapter

        val listViewTotalGastado = findViewById<ListView>(R.id.lista_reporte_integrantes)

        // Datos de prueba (mocks)
        val listaTotales = listOf(
            TotalGastado("Juan", "$1200", R.mipmap.image_default_user),
            TotalGastado("María", "$950", R.mipmap.image_default_user),
            TotalGastado("Carlos", "$1500", R.mipmap.image_default_user),
            TotalGastado("Ana", "$1100", R.mipmap.image_default_user)
        )

        val adapterTotal = TotalGastadoAdapter(this, listaTotales)
        listViewTotalGastado.adapter = adapterTotal

    }

    private fun setupPieChart(pieChart: PieChart) {
        // Datos de prueba (mock)
        val entries = listOf(
            PieEntry(40f, "Alimentos"),
            PieEntry(25f, "Transporte"),
            PieEntry(15f, "Entretenimiento"),
            PieEntry(20f, "Otros")
        )

        val dataSet = PieDataSet(entries, "Categorías de Gastos")
        dataSet.colors = ColorTemplate.COLORFUL_COLORS.toList() // Colores predeterminados
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = 14f

        val pieData = PieData(dataSet)
        pieChart.data = pieData

        // Personalización del gráfico
        pieChart.description.isEnabled = false
        pieChart.isDrawHoleEnabled = true
        pieChart.setEntryLabelColor(Color.BLACK)
        pieChart.animateY(1000)
        pieChart.invalidate() // Refresca el gráfico
    }
}