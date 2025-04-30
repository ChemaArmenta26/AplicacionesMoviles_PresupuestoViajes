package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class CrearGrupo : AppCompatActivity() {


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
        val btnFlechaAtras = findViewById<ImageButton>(R.id.btn_flecha_atras)
        val etNombreGrupo = findViewById<EditText>(R.id.et_nombreGrupo)
        val etDesde = findViewById<EditText>(R.id.et_desde)
        val etHacia = findViewById<EditText>(R.id.et_hacia)
        val etSalida = findViewById<EditText>(R.id.et_salida)
        val etLlegada = findViewById<EditText>(R.id.et_llegada)

        btnFlechaAtras.setOnClickListener {
            finish()
        }


        btnCrearGrupo.setOnClickListener {
            crearNuevoGrupo(etNombreGrupo,etDesde,etHacia,etSalida,etLlegada)
        }


    }


    private fun crearNuevoGrupo(
        etNombreGrupo: EditText,
        etDesde: EditText,
        etHacia: EditText,
        etSalida: EditText,
        etLlegada: EditText
    ) {
        val nombreGrupo = etNombreGrupo.text.toString().trim()
        val desde = etDesde.text.toString().trim()
        val hacia = etHacia.text.toString().trim()
        val salida = etSalida.text.toString().trim()
        val llegada = etLlegada.text.toString().trim()

        if (nombreGrupo.isEmpty() || desde.isEmpty() || hacia.isEmpty() || salida.isEmpty() || llegada.isEmpty()) {
            Toast.makeText(this, "Por favor, llena todos los campos.", Toast.LENGTH_SHORT).show()
            return
        }

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Error: Inicia sesión para crear un grupo.", Toast.LENGTH_LONG).show()
            Log.e("CrearGrupo", "Usuario no autenticado al intentar crear grupo.")
            return
        }
        val currentUserId = currentUser.uid
        val codigoGrupo = generarCodigoGrupo()

        val nuevoGrupo = Grupo(
            codigoGrupo,
            nombreGrupo,
            desde,
            hacia,
            salida,
            llegada,
            currentUserId,
            listOf(currentUserId),
            listOf()
        )

        Log.i("CrearGrupo", "Creando grupo con código: $codigoGrupo por usuario $currentUserId")
        val database = FirebaseDatabase.getInstance()
        val grupoRef = database.getReference("grupos").child(codigoGrupo)

        grupoRef.setValue(nuevoGrupo)
            .addOnSuccessListener {
                Log.i("CrearGrupo", "Grupo $codigoGrupo guardado exitosamente en RTDB.")
                Toast.makeText(this, "Grupo '${nuevoGrupo.nombre}' creado", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, homev2::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { error ->
                Log.e("CrearGrupo", "Error al guardar grupo $codigoGrupo en RTDB.", error)
                Toast.makeText(this, "Error al crear grupo: ${error.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun generarCodigoGrupo(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..12)
            .map { caracteres.random() }
            .joinToString("")
    }

    
}