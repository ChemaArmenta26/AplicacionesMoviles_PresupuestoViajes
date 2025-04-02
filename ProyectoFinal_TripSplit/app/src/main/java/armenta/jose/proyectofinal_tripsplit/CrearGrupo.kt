package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.FirebaseDatabase

class CrearGrupo : AppCompatActivity() {

    private var numIntegrantes = 2


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_crear_grupo)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, TopBarFragment())
                .commit()
        }

        val btnCrearGrupo = findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnCrearGrupo)
        val btnSumar = findViewById<MaterialButton>(R.id.btn_plus)
        val btnRestar = findViewById<MaterialButton>(R.id.btn_minus)
        val tvNumIntegrantes = findViewById<TextView>(R.id.tv_numIntegrantes)


        btnCrearGrupo.setOnClickListener {
            crearGrupo()
            val intent = Intent(this, pantalla_principal::class.java)
            startActivity(intent)
        }

        btnSumar.setOnClickListener {
            numIntegrantes++
            tvNumIntegrantes.text = numIntegrantes.toString()
        }

        btnRestar.setOnClickListener {
            if (numIntegrantes > 2) {
                numIntegrantes--
                tvNumIntegrantes.text = numIntegrantes.toString()
            } else {
                Toast.makeText(this, "Debe haber al menos 2 integrantes", Toast.LENGTH_SHORT).show()
            }
        }

    }


    private fun crearGrupo() {
        val nombreGrupo = findViewById<EditText>(R.id.et_nombreGrupo).text.toString().trim()
        val desde = findViewById<EditText>(R.id.et_desde).text.toString().trim()
        val hacia = findViewById<EditText>(R.id.et_hacia).text.toString().trim()
        val salida = findViewById<EditText>(R.id.et_salida).text.toString().trim()
        val llegada = findViewById<EditText>(R.id.et_llegada).text.toString().trim()
        val numIntegrantes = findViewById<TextView>(R.id.tv_numIntegrantes).text.toString().toIntOrNull() ?: 0

        if (nombreGrupo.isEmpty() || desde.isEmpty() || hacia.isEmpty() || salida.isEmpty() || llegada.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val codigoGrupo = generarCodigoGrupo()
        val grupo = Grupo(codigoGrupo, nombreGrupo, desde, hacia, salida, llegada, numIntegrantes)

        val database = FirebaseDatabase.getInstance()
        val gruposRef = database.getReference("grupos")

        gruposRef.child(codigoGrupo).setValue(grupo)
            .addOnSuccessListener {
                Toast.makeText(this, "Grupo creado con éxito!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error al crear grupo.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun generarCodigoGrupo(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..12)
            .map { caracteres.random() }
            .joinToString("")
    }

    
}