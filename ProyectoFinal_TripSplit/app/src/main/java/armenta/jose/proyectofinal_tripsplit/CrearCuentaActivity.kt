package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CrearCuentaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_cuenta)

        auth = FirebaseAuth.getInstance()

        val email: EditText = findViewById(R.id.et_correo)
        val password: EditText = findViewById(R.id.et_contrasena)
        val confirmPassword: EditText = findViewById(R.id.et_conf_contrasena)
        val nombre: EditText = findViewById<EditText>(R.id.et_nombre)
        val apellido: EditText = findViewById<EditText>(R.id.et_apellido)
        val button: Button = findViewById(R.id.btn_crear)

        val tvError: TextView = findViewById(R.id.tvError)

        tvError.visibility = View.INVISIBLE

        button.setOnClickListener({
            if (email.text.toString().isEmpty() || password.text.toString()
                    .isEmpty() || confirmPassword.text.toString()
                    .isEmpty() || nombre.text.toString()
                    .isEmpty() || apellido.text.toString().isEmpty()
            ) {
                tvError.text = "Por favor, complete todos los campos."
                tvError.visibility = View.VISIBLE
            } else if (password.text.toString() != confirmPassword.text.toString()) {
                tvError.text = "Las contraseñas no coinciden."
                tvError.visibility = View.VISIBLE
            } else if (password.text.toString().length < 6) {
                tvError.text = "La contraseña debe tener al menos 6 caracteres."
                tvError.visibility = View.VISIBLE
            } else {
                tvError.visibility = View.INVISIBLE
                signIn(
                    email.text.toString(),
                    password.text.toString(),
                    nombre.text.toString(),
                    apellido.text.toString()
                )
            }
        })

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

        val btnIniciarSesion = findViewById<Button>(R.id.btn_iniciar_sesion)
        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

    }

    fun signIn(email: String, password: String, nombre: String, apellido: String) {
        Log.d("INFO", "email: $email, password: $password")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("INFO", "createUserWithEmail:success")
                    val user = auth.currentUser
                    val uid = user?.uid

                    // Guarda los datos adicionales en Firebase Realtime Database
                    if (uid != null) {
                        val database = FirebaseDatabase.getInstance().getReference("Usuarios")
                        val userData = mapOf(
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "email" to email
                        )

                        database.child(uid).setValue(userData)
                            .addOnSuccessListener {
                                Log.d("INFO", "Datos adicionales guardados en Realtime Database")
                            }
                            .addOnFailureListener { e ->
                                Log.w("INFO", "Error al guardar los datos en Realtime Database", e)
                            }
                    }

                    val intent = Intent(this, homev2::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                } else {
                    Log.w("INFO", "createUserWithEmail:failure", task.exception)
                    Toast.makeText(
                        baseContext, "Registro fallido.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


}