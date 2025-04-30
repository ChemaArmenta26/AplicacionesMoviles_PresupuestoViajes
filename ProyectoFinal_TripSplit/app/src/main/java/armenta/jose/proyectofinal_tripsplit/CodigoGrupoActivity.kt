package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CodigoGrupoActivity : AppCompatActivity() {

    private lateinit var etCodigoGrupo: EditText
    private lateinit var btnBuscar: Button
    private lateinit var btnFlechaAtras: ImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var dbRefGrupos: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_codigo_grupo)

        auth = FirebaseAuth.getInstance()
        dbRefGrupos = FirebaseDatabase.getInstance().getReference("grupos")
        btnFlechaAtras = findViewById(R.id.btn_flecha_atras)
        btnBuscar = findViewById(R.id.btn_buscar)
        etCodigoGrupo = findViewById(R.id.input_codigo)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

        btnFlechaAtras.setOnClickListener {
            finish()
        }

        btnBuscar.setOnClickListener {
            val codigoIngresado = etCodigoGrupo.text.toString().trim()
            buscarYUnirseAGrupo(codigoIngresado)
        }
    }

    private fun buscarYUnirseAGrupo(codigoIngresado: String) {
        if (codigoIngresado.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un código de grupo.", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Debes iniciar sesión para unirte a un grupo.", Toast.LENGTH_LONG).show()
            Log.e("JoinGroup", "Usuario no autenticado intentando unirse.")
            return
        }
        val currentUserId = currentUser.uid

        Log.d("JoinGroup", "Buscando grupo con código: $codigoIngresado para usuario: $currentUserId")

        val query = dbRefGrupos.orderByChild("codigo").equalTo(codigoIngresado).limitToFirst(1)

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.hasChildren()) {
                    val grupoSnapshot = snapshot.children.first()
                    val grupoKey = grupoSnapshot.key
                    val grupo = grupoSnapshot.getValue(Grupo::class.java)

                    if (grupo != null && grupoKey != null) {
                        Log.d("JoinGroup", "Grupo encontrado: ${grupo.nombre} (Key: $grupoKey)")

                        val miembrosActuales = grupo.miembrosIds ?: listOf()

                        if (miembrosActuales.contains(currentUserId)) {
                            Log.i("JoinGroup", "Usuario $currentUserId ya es miembro del grupo ${grupo.nombre}")
                            Toast.makeText(this@CodigoGrupoActivity, "Ya perteneces a este grupo.", Toast.LENGTH_SHORT).show()
                        } else {
                            Log.i("JoinGroup", "Usuario $currentUserId no es miembro. Intentando añadir")

                            val nuevaListaMiembros = miembrosActuales.toMutableList()
                            nuevaListaMiembros.add(currentUserId)

                            dbRefGrupos.child(grupoKey).child("miembrosIds")
                                .setValue(nuevaListaMiembros)
                                .addOnSuccessListener {
                                    Log.i("JoinGroup", "Usuario $currentUserId añadido exitosamente al grupo $grupoKey")
                                    Toast.makeText(this@CodigoGrupoActivity, "Te has unido al grupo '${grupo.nombre}'", Toast.LENGTH_SHORT).show()
                                    irAPantallaPrincipal()
                                }
                                .addOnFailureListener { error ->
                                    Log.e("JoinGroup", "Error al actualizar la lista de miembros para grupo $grupoKey", error)
                                    Toast.makeText(this@CodigoGrupoActivity, "Error al unirse al grupo: ${error.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Log.w("JoinGroup", "Grupo encontrado con código $codigoIngresado pero no se pudo parsear el objeto Grupo.")
                        Toast.makeText(this@CodigoGrupoActivity, "Error al procesar datos del grupo.", Toast.LENGTH_SHORT).show()
                    }

                } else {
                    Log.w("JoinGroup", "No se encontró grupo con el código: $codigoIngresado")
                    Toast.makeText(this@CodigoGrupoActivity, "Código de grupo no válido o inexistente.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("JoinGroup", "Error al buscar grupo por código.", error.toException())
                Toast.makeText(this@CodigoGrupoActivity, "Error de base de datos: ${error.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun irAPantallaPrincipal() {
        val intent = Intent(this, homev2::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}