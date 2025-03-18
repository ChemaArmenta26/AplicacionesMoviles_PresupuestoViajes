package armenta.jose.proyectofinal_tripsplit

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Integrante
import armenta.jose.proyectofinal_tripsplit.utilities.IntegrantesAdapter

class DetalleGastoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IntegrantesAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_gasto)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

        val tvNombreGasto = findViewById<TextView>(R.id.tv_nombre_gasto)
        val tvMontoGasto = findViewById<TextView>(R.id.tv_monto_gasto)
        val tvCategoriaGasto = findViewById<TextView>(R.id.tv_categoria_gasto)

        tvNombreGasto.text = "Comida en restaurante"
        tvMontoGasto.text = "$1,500.00"
        tvCategoriaGasto.text = "Comida"

        recyclerView = findViewById(R.id.rv_integrantes)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = IntegrantesAdapter()
        recyclerView.adapter = adapter

        adapter.listaIntegrantes = crearDatosEjemplo()
        adapter.notifyDataSetChanged()

    }

    private fun crearDatosEjemplo(): ArrayList<Integrante> {
        val lista = ArrayList<Integrante>()

        lista.add(Integrante(1, "Chema", 187.5))
        lista.add(Integrante(2, "Melle", 187.5))
        lista.add(Integrante(3, "Gabriel", 187.5))
        lista.add(Integrante(4, "Diego", 187.5))
        lista.add(Integrante(5, "Beto", 0.0))
        lista.add(Integrante(6, "Victor", 187.5))
        lista.add(Integrante(7, "Angel", 187.5))
        lista.add(Integrante(8, "Chuy", 187.5))

        return lista
    }
}