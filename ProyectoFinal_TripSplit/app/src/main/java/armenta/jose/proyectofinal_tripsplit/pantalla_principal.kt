package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import armenta.jose.proyectofinal_tripsplit.utilities.Gasto
import armenta.jose.proyectofinal_tripsplit.utilities.GastoAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendiente
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendienteAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersona
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersonaAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.setupBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView

class pantalla_principal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_principal)

        // Primer ListView para los pagos por persona
        val listViewPersonas = findViewById<ListView>(R.id.lista_monto_pagar_persona)
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.nav_view)
        setupBottomNavigation(bottomNavigation, R.id.navigation_home)

        // Datos de prueba para PagarPorPersona
        val listaMontoPagar = listOf(
            PagarPorPersona("Juan", "$500", R.mipmap.image_default_user),
            PagarPorPersona("María", "$200", R.mipmap.image_default_user),
            PagarPorPersona("Carlos", "$350", R.mipmap.image_default_user),
            PagarPorPersona("Ana", "$400", R.mipmap.image_default_user)
        )

        val adapterPersonas = PagarPorPersonaAdapter(this, listaMontoPagar)
        listViewPersonas.adapter = adapterPersonas

        // Segundo ListView para los gastos generales
        val listViewGastos = findViewById<ListView>(R.id.lista_gastos)

        // Datos de prueba para Gasto
        val listaGastos = listOf(
            Gasto("C", "Mochomos", "Cena en restaurante", "- $6,622.00"),
            Gasto("T", "UBER", "Viaje en uber", "- $150.00"),
            Gasto("I", "Infonavit", "Pago de renta, Vivienda", "- $8,000.00")
        )

        val adapterGastos = GastoAdapter(this, listaGastos)
        listViewGastos.adapter = adapterGastos

        // Evento de clic en un elemento de la lista de gastos
        listViewGastos.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val gastoSeleccionado = listaGastos[position]
            val intent = Intent(this, DetalleGastoActivity::class.java)
            intent.putExtra("MONTO", gastoSeleccionado.monto)
            intent.putExtra("DESCRIPCION_GASTO", gastoSeleccionado.tipo)
            intent.putExtra("MONTO_GASTO", gastoSeleccionado.categoria)
            startActivity(intent)
        }
    }
}