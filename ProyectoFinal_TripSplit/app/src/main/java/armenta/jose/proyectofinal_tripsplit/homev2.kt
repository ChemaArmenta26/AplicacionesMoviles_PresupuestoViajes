package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.adapters.GrupoAdapter
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.Locale

class homev2 : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var grupoAdapter: GrupoAdapter
    private lateinit var etBuscar: EditText

    private val todosLosGruposDelUsuario = mutableListOf<Grupo>()
    private val gruposMostradosEnLista = mutableListOf<Grupo>()


    private lateinit var auth: FirebaseAuth
    private var gruposValueEventListener: ValueEventListener? = null
    private lateinit var gruposRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homev2)

        auth = FirebaseAuth.getInstance()

        listView = findViewById(R.id.listViewGrupos)
        etBuscar = findViewById(R.id.et_buscar)

        grupoAdapter = GrupoAdapter(this, gruposMostradosEnLista)
        listView.adapter = grupoAdapter

        val btnCrearViaje = findViewById<Button>(R.id.btn_crearViaje)
        btnCrearViaje.setOnClickListener {
            val intent = Intent(this, CrearGrupo::class.java)
            startActivity(intent)
        }
        val btnUnirseViaje = findViewById<Button>(R.id.btn_unirseViaje)
        btnUnirseViaje.setOnClickListener {
            val intent = Intent(this, CodigoGrupoActivity::class.java)
            startActivity(intent)
        }

        val database = FirebaseDatabase.getInstance()
        gruposRef = database.getReference("grupos")

        listView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            if (position < gruposMostradosEnLista.size) {
                val grupoSeleccionado = gruposMostradosEnLista[position]
                val intent = Intent(this, pantalla_principal::class.java).apply {
                    putExtra("groupId", grupoSeleccionado.codigo)
                }
                startActivity(intent)
            } else {
                Log.e("HomeV2", "Error: Posición de click inválida en la lista.")
            }
        }

        etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filtrarListaDeGrupos(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        cargarGruposDelUsuarioDesdeFirebase()
    }

    private fun cargarGruposDelUsuarioDesdeFirebase() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Usuario no autenticado.", Toast.LENGTH_SHORT).show()
            Log.w("HomeV2", "Intento de cargar grupos sin usuario logueado.")
            return
        }
        val currentUserId = currentUser.uid
        Log.d("HomeV2", "Cargando TODOS los grupos para el usuario: $currentUserId")

        if (gruposValueEventListener != null) {
            gruposRef.removeEventListener(gruposValueEventListener!!)
        }

        gruposValueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("HomeV2", "Datos de grupos recibidos de Firebase.")
                todosLosGruposDelUsuario.clear()

                if (snapshot.exists()) {
                    for (grupoSnapshot in snapshot.children) {
                        val grupo = grupoSnapshot.getValue(Grupo::class.java)
                        if (grupo?.miembrosIds?.contains(currentUserId) == true) {
                            todosLosGruposDelUsuario.add(grupo)
                        }
                    }
                }
                Log.d("HomeV2", "Total de grupos del usuario cargados: ${todosLosGruposDelUsuario.size}")
                filtrarListaDeGrupos(etBuscar.text.toString())

                if (todosLosGruposDelUsuario.isEmpty() && etBuscar.text.isEmpty()) {
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


    private fun filtrarListaDeGrupos(query: String?) {
        gruposMostradosEnLista.clear()

        if (query.isNullOrEmpty()) {
            gruposMostradosEnLista.addAll(todosLosGruposDelUsuario)
        } else {
            val textoBusqueda = query.lowercase(Locale.getDefault()).trim()
            for (grupo in todosLosGruposDelUsuario) {
                if (grupo.nombre?.lowercase(Locale.getDefault())?.contains(textoBusqueda) == true) {
                    gruposMostradosEnLista.add(grupo)
                }
            }
        }

        grupoAdapter.notifyDataSetChanged()
        Log.d("HomeV2", "Lista filtrada. Mostrando ${gruposMostradosEnLista.size} grupos para query: '$query'")


    }

    override fun onDestroy() {
        super.onDestroy()
        if (gruposValueEventListener != null) {
            gruposRef.removeEventListener(gruposValueEventListener!!)
            Log.d("HomeV2", "ValueEventListener de grupos removido.")
        }
    }
}
