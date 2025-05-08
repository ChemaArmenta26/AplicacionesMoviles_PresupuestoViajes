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
    private lateinit var btnEliminar: ImageButton
    private lateinit var listView: ListView
    private lateinit var btnRegresar: Button

    private lateinit var refGrupo: DatabaseReference
    private lateinit var refUsuarios: DatabaseReference

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

        groupId = intent.getStringExtra("groupId")
            ?: throw IllegalArgumentException("Hace falta pasar groupId")

        refGrupo = FirebaseDatabase.getInstance().getReference("grupos").child(groupId)
        refUsuarios = FirebaseDatabase.getInstance().getReference("Usuarios")

        tvTitle = findViewById(R.id.tv_title)
        btnEditName = findViewById(R.id.btnEditGroupName)
        btnEliminar = findViewById(R.id.btn_eliminar)
        listView = findViewById(R.id.listViewIntegrantes)
        btnRegresar = findViewById(R.id.btn_regresar)

        adapter = IntegranteGrupoAdapter(this, integrantes, false) { pos ->
            eliminarMiembro(pos)
        }
        listView.adapter = adapter

        btnRegresar.setOnClickListener { finish() }
        btnEditName.setOnClickListener { showEditNameDialog() }
        btnEliminar.setOnClickListener { pedirConfirmacionEliminar() }

        cargaDatosGrupo()

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

                adminId = snap.child("adminId").getValue(String::class.java).orEmpty()
                val curUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                isAdmin = (curUid == adminId)

                btnEditName.visibility = if (isAdmin) View.VISIBLE else View.GONE
                btnEliminar.visibility = if (isAdmin) View.VISIBLE else View.GONE

                miembrosIds.clear()
                miembrosIds.add(adminId)
                for (c in snap.child("miembrosIds").children) {
                    c.getValue(String::class.java)?.let { uid ->
                        if (uid != adminId) miembrosIds.add(uid)
                    }
                }

                adapter.setIsAdmin(isAdmin)
                cargaDetallesMiembros()

                val nombre = snap.child("nombre").getValue(String::class.java).orEmpty()
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
                        val nom = userSnap.child("nombre").getValue(String::class.java).orEmpty()
                        val ap = userSnap.child("apellido").getValue(String::class.java).orEmpty()
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
                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun eliminarMiembro(pos: Int) {
        if (!isAdmin || pos == 0) return
        val uidAEliminar = miembrosIds[pos]
        val nuevaLista = miembrosIds.filter { it != uidAEliminar }
        refGrupo.child("miembrosIds")
            .setValue(nuevaLista)
            .addOnSuccessListener {
                miembrosIds.clear()
                miembrosIds.addAll(nuevaLista)
                cargaDetallesMiembros()
            }
            .addOnFailureListener {
                Toast.makeText(this, "No se pudo eliminar", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEditNameDialog() {
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
                        .addOnSuccessListener { tvTitle.text = nuevo }
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun pedirConfirmacionEliminar() {
        if (!isAdmin) return
        AlertDialog.Builder(this)
            .setTitle("Eliminar grupo")
            .setMessage("¿Seguro que quieres eliminar este grupo y TODOS sus gastos?")
            .setPositiveButton("Eliminar") { _, _ -> eliminarGrupoCompleto() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun eliminarGrupoCompleto() {
        refGrupo.child("gastosIds")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updates = mutableMapOf<String, Any?>()
                    updates["grupos/$groupId"] = null
                    snapshot.children.forEach { gastoSnap ->
                        val gastoId = gastoSnap.getValue(String::class.java)
                        if (!gastoId.isNullOrBlank()) {
                            updates["gastos/$gastoId"] = null
                        }
                    }
                    FirebaseDatabase.getInstance().reference
                        .updateChildren(updates)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this@EditarGrupoActivity,
                                "Grupo eliminado",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                            val intent = intent
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this@EditarGrupoActivity,
                                "Error al eliminar: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@EditarGrupoActivity,
                        "Error: ${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }
}