package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Integrante
import armenta.jose.proyectofinal_tripsplit.utilities.IntegranteRegistroAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.IntegrantesAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.setupBottomNavigation
import com.google.android.material.bottomnavigation.BottomNavigationView

class RegistrarGastos : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IntegranteRegistroAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registrar_gastos)
        val btnFlechaAtras = findViewById<ImageButton>(R.id.btn_flecha_atras)

        btnFlechaAtras.setOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }


        val btnCancelar = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_cancelar)
        btnCancelar.setOnClickListener {
            val intent = Intent(this, pantalla_principal::class.java)
            startActivity(intent)
        }
        val btnGuardar = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_guardar)
        btnGuardar.setOnClickListener {
            val intent = Intent(this, pantalla_principal::class.java)
            startActivity(intent)
        }

        // Obtener referencias de los Spinners
        val spinnerCategoria = findViewById<Spinner>(R.id.spinnerCategoria)
        val spinnerQuienPago = findViewById<Spinner>(R.id.spinnerQuienPago)

// Lista de categorías con hint
        val categorias = listOf("Selecciona una opción", "Hospedaje", "Comida", "Traslado")

// Lista de quién pagó con hint
        val quienPago = listOf(
            "Selecciona quien pagó",
            "Chema",
            "Melle",
            "Gabriel",
            "Diego",
            "Beto",
            "Victor",
            "Angel",
            "Chuy"
        )

// Configurar adaptadores con layout personalizado para deshabilitar la primera opción
        val adapterCategoria = object :
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categorias) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0 // Deshabilitar el primer elemento (hint)
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).setTextColor(Color.GRAY) // Cambia el color para indicar que no es seleccionable
                }
                return view
            }
        }

        val adapterQuienPago = object :
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, quienPago) {
            override fun isEnabled(position: Int): Boolean {
                return position != 0
            }

            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val view = super.getDropDownView(position, convertView, parent)
                if (position == 0) {
                    (view as TextView).setTextColor(Color.GRAY)
                }
                return view
            }
        }

// Asignar adaptadores a los Spinners
        spinnerCategoria.adapter = adapterCategoria
        spinnerQuienPago.adapter = adapterQuienPago

// Establecer la selección en la primera opción (hint) al iniciar
        spinnerCategoria.setSelection(0, false)
        spinnerQuienPago.setSelection(0, false)


        recyclerView = findViewById(R.id.rv_integrantes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = IntegranteRegistroAdapter()
        recyclerView.adapter = adapter

        adapter.listaIntegrantes = obtenerIntegrantesMock()
        adapter.notifyDataSetChanged()

    }

    private fun obtenerIntegrantesMock(): ArrayList<Integrante> {
        return arrayListOf(
            Integrante(id = 1, nombre = "Chema", null),
            Integrante(id = 2, nombre = "Melle", null),
            Integrante(id = 3, nombre = "Gabriel", null),
            Integrante(id = 4, nombre = "Diego", null),
            Integrante(id = 5, nombre = "Beto", null),
            Integrante(id = 6, nombre = "Victor", null),
            Integrante(id = 7, nombre = "Angel", null),
            Integrante(id = 8, nombre = "Chuy", null)
        )
    }
}