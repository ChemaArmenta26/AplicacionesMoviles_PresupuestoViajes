package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditarPerfil : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var tvSaludo: TextView
    private lateinit var tvCorreo: TextView
    private lateinit var btnCerrarSesion: Button
    private lateinit var btnFlechaAtras: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editar_perfil)

        auth            = FirebaseAuth.getInstance()
        tvSaludo        = findViewById(R.id.saludo_usuario)
        tvCorreo        = findViewById(R.id.correo_usuario)
        btnCerrarSesion = findViewById(R.id.btn_cerrar_sesion)
        btnFlechaAtras  = findViewById(R.id.btn_flecha_atras)

        cargarDatosUsuario()

        btnFlechaAtras.setOnClickListener { finish() }

        btnCerrarSesion.setOnClickListener {
            auth.signOut()
            startActivity(Intent(this, Login::class.java))
            finishAffinity()
        }
    }

    private fun cargarDatosUsuario() {
        val uid = auth.currentUser?.uid ?: return

        FirebaseDatabase.getInstance()
            .getReference("Usuarios")
            .child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val nombre   = snapshot.child("nombre").getValue(String::class.java).orEmpty()
                    val apellido = snapshot.child("apellido").getValue(String::class.java).orEmpty()
                    val correo   = snapshot.child("email").getValue(String::class.java).orEmpty()

                    val nombreCompleto = listOf(nombre, apellido)
                        .filter { it.isNotBlank() }
                        .joinToString(" ")

                    tvSaludo.text = "¡Hola, ${if (nombreCompleto.isNotBlank()) nombreCompleto else "Usuario"}!"
                    tvCorreo.text = correo
                }

                override fun onCancelled(error: DatabaseError) { }
            })
    }
}