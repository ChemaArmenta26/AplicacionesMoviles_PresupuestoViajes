package armenta.jose.proyectofinal_tripsplit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment

class DetalleGastoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detalle_gasto)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

    }
}