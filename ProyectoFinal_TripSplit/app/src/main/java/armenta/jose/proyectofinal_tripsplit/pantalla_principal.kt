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
            val intent = Intent(this, homev2::class.java)
            startActivity(intent)
        }


        btnAgregarGasto.setOnClickListener {
            if (groupId != null) {
                val intentRegistrar = Intent(this, RegistrarGastos::class.java)
                intentRegistrar.putExtra("GRUPO_ID", groupId)
                startActivity(intentRegistrar)
            } else {
                Toast.makeText(this, "Error: No se pudo identificar el grupo.", Toast.LENGTH_SHORT).show()
            }
        }


        btnReporteGastos.setOnClickListener {
            val intent = Intent(this, reporte_gastos::class.java)
            intent.putExtra("GROUP_ID", groupId)  // Le mando el ID del grupo apra poder ver los reportes
            startActivity(intent)
        }
        // Datos de prueba para PagarPorPersona
        val listaMontoPagar = listOf(
            PagarPorPersona("Juan", "$500", R.mipmap.image_default_user),
            PagarPorPersona("María", "$200", R.mipmap.image_default_user),
            PagarPorPersona("Carlos", "$350", R.mipmap.image_default_user),
            PagarPorPersona("Ana", "$400", R.mipmap.image_default_user)
        )
        listViewPersonas.adapter = PagarPorPersonaAdapter(this, listaMontoPagar)


        // AÑADIR ESTA FUNCIONALIDAD:
        // Configurar el onItemClickListener para la lista de personas
        listViewPersonas.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val persona = listaMontoPagar[position]
                Intent(this, DetalleGastoActivity::class.java).apply {
                    putExtra("NOMBRE_PERSONA", persona.nombre)
                    putExtra("MONTO", persona.cantidad)
                    // Puedes agregar más información si es necesario
                }.also { startActivity(it) }
            }


        // Segundo ListView para los gastos generales
        val listViewGastos = findViewById<ListView>(R.id.lista_gastos)
        val listaGastos = listOf(
            Gasto("C", "Cena en restaurante", 6622.0, "Cena"),
            Gasto("T", "Viaje en uber", 150.0, "Transporte"),
            Gasto("I", "Pago de renta", 8000.0, "Vivienda")
        )
        listViewGastos.adapter = GastoAdapter(this, listaGastos)


        listViewGastos.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                val gasto = listaGastos[position]
                Intent(this, DetalleGastoActivity::class.java).apply {
                    putExtra("MONTO", gasto.montoTotal)
                    putExtra("NOMBRE_GASTO", gasto.nombre)
                    putExtra("CATEGORIA_GASTO", gasto.categoria)
                }.also { startActivity(it) }
            }
    }
}
