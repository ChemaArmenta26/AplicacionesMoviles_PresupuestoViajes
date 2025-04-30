package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Integrante
import armenta.jose.proyectofinal_tripsplit.utilities.IntegranteGrupoAdapter

class EditarGrupoActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var btnEditName: ImageButton
    private lateinit var listView: ListView
    private lateinit var btnRegresar: Button

    // Firebase
    private lateinit var refGrupo: DatabaseReference
    private lateinit var refUsuarios: DatabaseReference

    // Estado
    private var adminId = ""
    private var isAdmin = false
    private val miembrosIds = mutableListOf<String>()
    private val integrantes = mutableListOf<Integrante>()
    private lateinit var adapter: IntegranteGrupoAdapter
    private lateinit var groupId: String

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_grupo)

        // 1) Leer el groupId
        groupId = intent.getStringExtra("groupId")
            ?: throw IllegalArgumentException("Hace falta pasar groupId")

        // 2) Referencias Firebase
        refGrupo = FirebaseDatabase.getInstance()
            .getReference("grupos").child(groupId)
        refUsuarios = FirebaseDatabase.getInstance()
            .getReference("Usuarios")

        // 3) Vistas
        tvTitle     = findViewById(R.id.tv_title)
        btnEditName = findViewById(R.id.btnEditGroupName)
        listView    = findViewById(R.id.listViewIntegrantes)
        btnRegresar = findViewById(R.id.btn_regresar)

        // 4) Adapter (inicialmente sin permisos de admin)
        adapter = IntegranteGrupoAdapter(this, integrantes, /*isAdmin=*/false) { pos ->
            eliminarMiembro(pos)
        }
        listView.adapter = adapter

        // 5) Listeners
        btnRegresar.setOnClickListener { finish() }
        btnEditName.setOnClickListener { showEditNameDialog() }

        // 6) Carga datos
        cargaDatosGrupo()

        // 7) TopBarFragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }
    }

    private fun cargaDatosGrupo() {
        refGrupo.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                if (!snap.exists()) return

                // 1) Leer quién es admin
                adminId = snap.child("adminId")
                    .getValue(String::class.java).orEmpty()

                // 2) Saber si el usuario actual es admin
                val curUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                isAdmin = (curUid == adminId)

                // 3) Mostrar u ocultar botón editar nombre
                btnEditName.visibility = if (isAdmin) View.VISIBLE else View.GONE

                // 4) Reconstruir lista de miembrosIds: admin primero
                miembrosIds.clear()
                miembrosIds.add(adminId)
                for (c in snap.child("miembrosIds").children) {
                    c.getValue(String::class.java)?.let { uid ->
                        if (uid != adminId) miembrosIds.add(uid)
                    }
                }

                // 5) Notificar al adapter el cambio de permiso
                adapter.setIsAdmin(isAdmin)

                // 6) Cargar nombres y refrescar vista
                cargaDetallesMiembros()

                // 7) Poner título
                val nombre = snap.child("nombre")
                    .getValue(String::class.java)
                    .orEmpty()
                tvTitle.text = nombre
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(
                    this@EditarGrupoActivity,
                    "Error cargando grupo: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun cargaDetallesMiembros() {
        integrantes.clear()
        if (miembrosIds.isEmpty()) {
            adapter.notifyDataSetChanged()
            return
        }
        var cargados = 0
        miembrosIds.forEachIndexed { idx, uid ->
            refUsuarios.child(uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(userSnap: DataSnapshot) {
                        val nom = userSnap.child("nombre")
                            .getValue(String::class.java).orEmpty()
                        val ap  = userSnap.child("apellido")
                            .getValue(String::class.java).orEmpty()
                        integrantes.add(
                            Integrante(
                                id = idx,
                                nombre = "$nom $ap".trim(),
                                deuda = null
                            )
                        )
                        cargados++
                        if (cargados == miembrosIds.size) {
                            adapter.notifyDataSetChanged()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) { /*no-op*/ }
                })
        }
    }

    private fun eliminarMiembro(pos: Int) {
        // Seguridad: solo el admin puede
        if (!isAdmin || pos == 0) return

        // 1) UID a eliminar
        val uidAEliminar = miembrosIds[pos]

        // 2) Filtramos la lista quitando ese UID
        val nuevaLista = miembrosIds.filter { it != uidAEliminar }

        // 3) Guardamos en Firebase
        refGrupo.child("miembrosIds")
            .setValue(nuevaLista)
            .addOnSuccessListener {
                // 4) Refrescamos la lista local y la UI
                miembrosIds.clear()
                miembrosIds.addAll(nuevaLista)
                cargaDetallesMiembros()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditNameDialog() {
        // Sólo admin puede
        if (!isAdmin) return

        val et = EditText(this).apply { setText(tvTitle.text) }
        AlertDialog.Builder(this)
            .setTitle("Editar nombre de grupo")
            .setView(et)
            .setPositiveButton("Guardar") { _, _ ->
                val nuevo = et.text.toString().trim()
                if (nuevo.isNotEmpty()) {
                    refGrupo.child("nombre")
                        .setValue(nuevo)
                        .addOnSuccessListener {
                            tvTitle.text = nuevo
                        }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
}