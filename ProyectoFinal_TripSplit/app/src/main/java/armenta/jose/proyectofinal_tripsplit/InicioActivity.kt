package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class InicioActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_inicio)


        val btnUnete = findViewById<Button>(R.id.btn_ingresar)

        btnUnete.setOnClickListener {
            val intent = Intent(this, CrearCuentaActivity::class.java)

            startActivity(intent)
        }
    }
}