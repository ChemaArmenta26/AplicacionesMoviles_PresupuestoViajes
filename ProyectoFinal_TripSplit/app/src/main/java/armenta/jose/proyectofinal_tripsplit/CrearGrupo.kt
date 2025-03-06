package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment

class CrearGrupo : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_grupo)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

        val btnIngresar = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btn_ingresar)
        btnIngresar.setOnClickListener {
            val intent = Intent(this, pantalla_principal::class.java)
            startActivity(intent)
        }
    }
}