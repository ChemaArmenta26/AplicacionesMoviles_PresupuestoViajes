package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Gasto
import armenta.jose.proyectofinal_tripsplit.utilities.GastoAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendiente
import armenta.jose.proyectofinal_tripsplit.utilities.GastoPendienteAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersona
import armenta.jose.proyectofinal_tripsplit.utilities.PagarPorPersonaAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.setupBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView

class pantalla_principal : AppCompatActivity() {

    private lateinit var groupId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pantalla_principal)

        // 1) Leo el groupId que me pasaron en el Intent
        groupId = intent.getStringExtra("groupId").orEmpty()
        if (groupId.isBlank()) {
            Toast.makeText(this, "Falta información del grupo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 2) Inyecto el TopBarFragment con ese mismo groupId
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.topBarFragment,
                    TopBarFragment.newInstance(groupId)
                )
                .commit()
        }

        // Primer ListView para los pagos por persona
        val listViewPersonas = findViewById<ListView>(R.id.lista_monto_pagar_persona)
        val btnReporteGastos = findViewById<Button>(R.id.btn_ver_reporte_gastos)
        val btnAgregarGasto = findViewById<ImageButton>(R.id.btn_agregar_gasto)
        val btnFlechaAtras = findViewById<ImageButton>(R.id.btn_flecha_atras)

        btnFlechaAtras.setOnClickListener {
            // Opcional: si quieres volver al homev2
            val intent = Intent(this, homev2::class.java)
            startActivity(intent)
        }

        btnAgregarGasto.setOnClickListener {
            startActivity(Intent(this, RegistrarGastos::class.java))
        }

        btnReporteGastos.setOnClickListener {
            startActivity(Intent(this, reporte_gastos::class.java))
        }

        // Datos de prueba para PagarPorPersona
        val listaMontoPagar = listOf(
            PagarPorPersona("Juan", "$500", R.mipmap.image_default_user),
            PagarPorPersona("María", "$200", R.mipmap.image_default_user),
            PagarPorPersona("Carlos", "$350", R.mipmap.image_default_user),
            PagarPorPersona("Ana", "$400", R.mipmap.image_default_user)
        )
        listViewPersonas.adapter = PagarPorPersonaAdapter(this, listaMontoPagar)

        // Segundo ListView para los gastos generales
        val listViewGastos = findViewById<ListView>(R.id.lista_gastos)
        val listaGastos = listOf(
            Gasto("C", "Mochomos", "Cena en restaurante", "- $6,622.00"),
            Gasto("T", "UBER", "Viaje en uber", "- $150.00"),
            Gasto("I", "Infonavit", "Pago de renta, Vivienda", "- $8,000.00")
        )
        listViewGastos.adapter = GastoAdapter(this, listaGastos)

        listViewGastos.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val gasto = listaGastos[position]
                Intent(this, DetalleGastoActivity::class.java).apply {
                    putExtra("MONTO", gasto.monto)
                    putExtra("DESCRIPCION_GASTO", gasto.tipo)
                    putExtra("MONTO_GASTO", gasto.categoria)
                }.also { startActivity(it) }
            }
    }
}