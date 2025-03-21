package armenta.jose.proyectofinal_tripsplit

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Starting Home activity
        val intent = Intent(this, Home::class.java)
        startActivity(intent)

        // Finish MainActivity so the user can't go back to it
        finish()
    }
    }
