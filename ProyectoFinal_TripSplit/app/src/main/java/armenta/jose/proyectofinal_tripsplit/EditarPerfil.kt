package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class EditarPerfil : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_perfil)

        val btnCerrarSesion = findViewById<Button>(R.id.btn_cerrar_sesion)
        val btnFlechaAtras = findViewById<ImageButton>(R.id.btn_flecha_atras)

        btnFlechaAtras.setOnClickListener {
            finish()
        }

        btnCerrarSesion.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            FirebaseAuth.getInstance().signOut()
            startActivity(intent)
        }
    }
}