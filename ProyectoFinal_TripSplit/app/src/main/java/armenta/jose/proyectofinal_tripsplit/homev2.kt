package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.adapters.GrupoAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class homev2 : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var grupoAdapter: GrupoAdapter
    private val gruposFiltradosList = mutableListOf<Grupo>()


    private lateinit var auth: FirebaseAuth
    private var gruposValueEventListener: ValueEventListener? = null
    private lateinit var gruposRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homev2)

        auth = FirebaseAuth.getInstance()

        listView = findViewById(R.id.listViewGrupos)
        grupoAdapter = GrupoAdapter(this, gruposFiltradosList)
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

        val database = FirebaseDatabase.getInstance()
        gruposRef = database.getReference("grupos")

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < gruposFiltradosList.size) {
                val grupoSeleccionado = gruposFiltradosList[position]
                val intent = Intent(this, pantalla_principal::class.java).apply {
                    putExtra("groupId", grupoSeleccionado.codigo)   // <- aquí el ID real
                }
                startActivity(intent)
            } else {
                Log.e("HomeV2", "Error: Posición de click inválida en la lista.")
            }
        }
        cargarYFiltrarGrupos()
    }

    private fun cargarYFiltrarGrupos() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            Log.w("HomeV2", "Intento de cargar grupos sin usuario logueado.")
            return
        }
        val currentUserId = currentUser.uid
        Log.d("HomeV2", "Cargando grupos para el usuario: $currentUserId")

        if (gruposValueEventListener != null) {
            gruposRef.removeEventListener(gruposValueEventListener!!)
        }

        gruposValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeV2", "Datos de grupos recibidos de Firebase.")
                gruposFiltradosList.clear()

                for (grupoSnapshot in snapshot.children) {
                    val grupo = grupoSnapshot.getValue(Grupo::class.java)

                    if (grupo != null && grupo.miembrosIds != null && grupo.miembrosIds!!.contains(currentUserId)) {
                        gruposFiltradosList.add(grupo)
                        Log.d("HomeV2", "Grupo añadido a la lista: ${grupo.nombre} (Código: ${grupo.codigo})")
                    } else {
                        Log.w("HomeV2", "Snapshot de grupo con key ${grupoSnapshot.key} no pudo ser convertido a objeto Grupo.")
                    }
                }
                grupoAdapter.notifyDataSetChanged()
                Log.d("HomeV2", "Adapter notificado. Mostrando ${gruposFiltradosList.size} grupos.")

                if (gruposFiltradosList.isEmpty()) {
                    Toast.makeText(this@homev2, "No perteneces a ningún grupo aún.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("HomeV2", "Error al cargar grupos desde Firebase.", error.toException())
                Toast.makeText(this@homev2, "Error al cargar grupos: ${error.message}", Toast.LENGTH_LONG).show()
            }
        }

        gruposRef.addValueEventListener(gruposValueEventListener!!)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (gruposValueEventListener != null) {
            gruposRef.removeEventListener(gruposValueEventListener!!)
            Log.d("HomeV2", "ValueEventListener de grupos removido.")
        }
    }
}