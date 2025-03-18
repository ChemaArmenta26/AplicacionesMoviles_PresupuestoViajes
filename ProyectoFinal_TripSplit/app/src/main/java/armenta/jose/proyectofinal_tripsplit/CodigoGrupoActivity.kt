package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment

class CodigoGrupoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_codigo_grupo)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

        val btnBuscar = findViewById<Button>(R.id.btn_buscar)

        btnBuscar.setOnClickListener {
            val intent = Intent(this, pantalla_principal::class.java)
            startActivity(intent)
        }
    }
}