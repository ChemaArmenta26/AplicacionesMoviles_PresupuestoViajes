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
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRefGrupos: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        dbRefGrupos = FirebaseDatabase.getInstance().getReference("grupos")

        val email = findViewById<EditText>(R.id.et_correoElectronico)
        val password = findViewById<EditText>(R.id.et_contrasena)


        val tvError = findViewById<TextView>(R.id.tvError)
        tvError.visibility = View.INVISIBLE

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment.newInstance(showEditIcon = false))
                .commit()
        }

        val btn_registrarse = findViewById<Button>(R.id.btn_registrarse)
        btn_registrarse.setOnClickListener {
            val intent = Intent(this, CrearCuentaActivity::class.java)
            startActivity(intent)
        }

        val btn_ingresar = findViewById<Button>(R.id.btn_ingresar)
        btn_ingresar.setOnClickListener {
            val emailText = email.text.toString().trim()
            val passwordText = password.text.toString().trim()
            if (emailText.isNotEmpty() && passwordText.isNotEmpty()) {
                login(emailText, passwordText)
            } else {
                showError("Por favor, ingresa correo y contraseña.", true)
            }
        }
    }

    private fun login(email: String, password: String) {
        findViewById<Button>(R.id.btn_ingresar).isEnabled = false
        showError(visible = false)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                findViewById<Button>(R.id.btn_ingresar).isEnabled = true
                if (task.isSuccessful) {
                    Log.d("Login", "signInWithEmail:success")
                    val user = auth.currentUser
                    if (user != null) {
                        checkUserGroupStatusAndNavigate(user)
                    } else {
                        showError("Error al obtener datos del usuario.", true)
                    }
                } else {
                    Log.w("Login", "signInWithEmail:failure", task.exception)
                    showError("Correo o contraseña incorrectos.", true)
                }
            }
    }

    private fun checkUserGroupStatusAndNavigate(user: FirebaseUser) {
        val userId = user.uid
        Log.d("Login", "Verificando grupos para el usuario: $userId")



        dbRefGrupos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var usuarioTieneGrupos = false
                if (snapshot.exists()) {
                    for (grupoSnapshot in snapshot.children) {
                        val grupo = grupoSnapshot.getValue(Grupo::class.java)
                        if (grupo?.miembrosIds?.contains(userId) == true) {
                            usuarioTieneGrupos = true
                            Log.d("Login", "Usuario $userId encontrado en grupo: ${grupo.nombre}")
                            break
                        }
                    }
                }

                if (usuarioTieneGrupos) {
                    Log.d("Login", "Usuario tiene grupos. Navegando a homev2.")
                    goToScreen(homev2::class.java)
                } else {
                    Log.d("Login", "Usuario NO tiene grupos. Navegando a home.")
                    goToScreen(Home::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Login", "Error al verificar grupos: ${error.message}", error.toException())
                showError("Error al verificar grupos. Intenta de nuevo.", true)
            }
        })
    }

    private fun showError(text: String = "", visible: Boolean) {
        val tvError = findViewById<TextView>(R.id.tvError)
        tvError.text = text
        tvError.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    private fun <T : AppCompatActivity> goToScreen(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    public override fun onStart() {
        super.onStart()
    }
}
