package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.et_correoElectronico)
        val password = findViewById<EditText>(R.id.et_contrasena)
        val button = findViewById<Button>(R.id.btn_ingresar)

        val tvError = findViewById<TextView>(R.id.tvError)

        tvError.visibility = View.INVISIBLE

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

        val btn_registrarse = findViewById<Button>(R.id.btn_registrarse)
        btn_registrarse.setOnClickListener {
            val intent = Intent(this, CrearCuentaActivity::class.java)
            startActivity(intent)
        }

        val btn_ingresar = findViewById<Button>(R.id.btn_ingresar)
        btn_ingresar.setOnClickListener {
            login(email.text.toString(), password.text.toString())
        }

    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                showError(visible = false)
                goToHome(user!!)
            } else {
                showError("Correo o contraseña incorrectos", true)
            }
        }
    }

    fun showError(text: String = "", visible: Boolean) {
        val tvError = findViewById<TextView>(R.id.tvError)
        tvError.text = text
        tvError.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }

    fun goToHome(user: FirebaseUser) {
        val intent = Intent(this, homev2::class.java)
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    public override fun onStart() {
        super.onStart()
        auth.signOut()
    }
}