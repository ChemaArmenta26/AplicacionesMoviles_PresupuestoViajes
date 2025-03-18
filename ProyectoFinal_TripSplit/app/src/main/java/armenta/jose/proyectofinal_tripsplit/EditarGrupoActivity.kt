package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment

class EditarGrupoActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_editar_grupo)

        val btnRegresar : Button = findViewById(R.id.btn_regresar)

        btnRegresar.setOnClickListener {
            finish()
        }

        if (savedInstanceState == null) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.topBarFragment, TopBarFragment())
            .commit()
        }
    }
}