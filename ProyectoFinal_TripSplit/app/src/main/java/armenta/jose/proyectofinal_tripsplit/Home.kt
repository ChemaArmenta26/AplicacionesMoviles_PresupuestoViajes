package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class Home : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }


        val btn_crearViaje = findViewById<Button>(R.id.btn_crearViaje)
        btn_crearViaje.setOnClickListener {
            val intent = Intent(this, CrearGrupo::class.java)
            startActivity(intent)
        }
        val btn_unirseViaje = findViewById<Button>(R.id.btn_unirseViaje)
        btn_unirseViaje.setOnClickListener {
            val intent = Intent(this, CodigoGrupoActivity::class.java)
            startActivity(intent)
        }
    }
}