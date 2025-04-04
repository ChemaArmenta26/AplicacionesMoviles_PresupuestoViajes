package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.adapters.GrupoAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.firebase.database.*

class homev2 : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var grupoAdapter: GrupoAdapter
    private val gruposList = mutableListOf<Grupo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homev2)

        listView = findViewById(R.id.listViewGrupos)
        grupoAdapter = GrupoAdapter(this, gruposList)
        listView.adapter = grupoAdapter

        val btn_crearViaje = findViewById<Button>(R.id.btn_crearViaje)
        btn_crearViaje.setOnClickListener {
            val intent = Intent(this, CrearGrupo::class.java)
            startActivity(intent)
        }
        val btn_unirseViaje = findViewById<Button>(R.id.btn_unirseViaje)
        btn_unirseViaje.setOnClickListener {
            val intent = Intent(this, CodigoGrupoActivity::class.java)
            startActivity(intent)
        }


        cargarGruposDesdeFirebase()

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val gastoSeleccionado = gruposList[position]
            val intent = Intent(this, pantalla_principal::class.java)
            startActivity(intent)
        }
    }

    private fun cargarGruposDesdeFirebase() {
        val database = FirebaseDatabase.getInstance()
        val gruposRef = database.getReference("grupos")

        gruposRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                gruposList.clear()
                for (grupoSnapshot in snapshot.children) {
                    val grupo = grupoSnapshot.getValue(Grupo::class.java)
                    if (grupo != null) {
                        gruposList.add(grupo)
                    }
                }
                grupoAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@homev2, "Error al cargar los grupos", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
