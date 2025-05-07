package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class CrearCuentaActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRefGrupos: DatabaseReference
    private lateinit var dbRefUsuarios: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_cuenta)

        auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        dbRefGrupos = database.getReference("grupos")
        dbRefUsuarios = database.getReference("Usuarios")

        val emailEt: EditText = findViewById(R.id.et_correo)
        val passwordEt: EditText = findViewById(R.id.et_contrasena)
        val confirmPasswordEt: EditText = findViewById(R.id.et_conf_contrasena)
        val nombreEt: EditText = findViewById(R.id.et_nombre)
        val apellidoEt: EditText = findViewById(R.id.et_apellido)
        val btnCrear: Button = findViewById(R.id.btn_crear)
        val tvError: TextView = findViewById(R.id.tvError)

        tvError.visibility = View.INVISIBLE

        btnCrear.setOnClickListener {
            val email = emailEt.text.toString().trim()
            val password = passwordEt.text.toString().trim()
            val confirmPassword = confirmPasswordEt.text.toString().trim()
            val nombre = nombreEt.text.toString().trim()
            val apellido = apellidoEt.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
                showError("Por favor, complete todos los campos.")
            } else if (password != confirmPassword) {
                showError("Las contraseñas no coinciden.")
            } else if (password.length < 6) {
                showError("La contraseña debe tener al menos 6 caracteres.")
            } else {
                tvError.visibility = View.INVISIBLE
                btnCrear.isEnabled = false
                signIn(email, password, nombre, apellido)
            }
        }

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment.newInstance(showEditIcon = false))
                .commit()
        }

        val btnIniciarSesion = findViewById<Button>(R.id.btn_iniciar_sesion)
        btnIniciarSesion.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }
    }

    private fun signIn(email: String, password: String, nombre: String, apellido: String) {
        Log.d("CrearCuenta", "Intentando crear cuenta para: $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                findViewById<Button>(R.id.btn_crear).isEnabled = true
                if (task.isSuccessful) {
                    Log.d("CrearCuenta", "createUserWithEmail:success")
                    val user = auth.currentUser
                    val uid = user?.uid

                    if (uid != null) {
                        val userData = mapOf(
                            "nombre" to nombre,
                            "apellido" to apellido,
                            "email" to email
                        )
                        dbRefUsuarios.child(uid).setValue(userData)
                            .addOnSuccessListener {
                                Log.d("CrearCuenta", "Datos adicionales del usuario guardados en RTDB.")
                                if (user != null) {
                                    checkUserGroupStatusAndNavigate(user)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.w("CrearCuenta", "Error al guardar datos adicionales en RTDB", e)
                                if (user != null) {
                                    checkUserGroupStatusAndNavigate(user)
                                }
                            }
                    } else {
                        Log.e("CrearCuenta", "Error: UID es null después de crear usuario.")
                        showError("Error al procesar registro. Intenta de nuevo.")
                    }
                } else {
                    Log.w("CrearCuenta", "Crear usuario con email fallo", task.exception)
                    val exceptionMessage = task.exception?.message ?: "Error desconocido."
                    if (exceptionMessage.contains("Correo electrónico en uso")) {
                        showError("Este correo electrónico ya está registrado.")
                    } else {
                        showError("Registro fallido: $exceptionMessage")
                    }
                }
            }
    }

    private fun checkUserGroupStatusAndNavigate(user: FirebaseUser) {
        val userId = user.uid
        Log.d("CrearCuenta", "Verificando grupos para el nuevo usuario: $userId")

        Log.d("CrearCuenta", "Usuario nuevo NO tiene grupos. Navegando a home.")
        goToScreen(Home::class.java)
    }

    private fun showError(text: String) {
        val tvError = findViewById<TextView>(R.id.tvError)
        tvError.text = text
        tvError.visibility = View.VISIBLE
    }

    private fun <T : AppCompatActivity> goToScreen(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
