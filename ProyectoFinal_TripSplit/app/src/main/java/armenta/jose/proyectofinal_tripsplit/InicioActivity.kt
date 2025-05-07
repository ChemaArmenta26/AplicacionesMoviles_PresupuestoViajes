package armenta.jose.proyectofinal_tripsplit

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*

class InicioActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRefGrupos: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        auth = FirebaseAuth.getInstance()
        dbRefGrupos = FirebaseDatabase.getInstance().getReference("grupos")
    }

    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Log.d("InicioActivity", "Usuario logueado detectado: ${currentUser.uid}. Verificando grupos...")
            checkUserGroupStatusAndNavigate(currentUser)
        } else {
            Log.d("InicioActivity", "No hay usuario logueado. Mostrando pantalla de inicio.")
            setContentView(R.layout.activity_inicio)

            val btnUnete = findViewById<Button>(R.id.btn_ingresar)

            if (btnUnete == null) {
                Log.e("InicioActivity", "Error no se encontró el botón con ID R.id.btn_ingresar en R.layout.activity_inicio.")
                Toast.makeText(this, "Error al cargar la pantalla de inicio.", Toast.LENGTH_LONG).show()
                return
            }

            btnUnete.setOnClickListener {
                val intent = Intent(this, CrearCuentaActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun checkUserGroupStatusAndNavigate(user: FirebaseUser) {
        val userId = user.uid
        Log.d("InicioActivity", "Verificando grupos para el usuario: $userId")


        dbRefGrupos.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var usuarioTieneGrupos = false
                if (snapshot.exists()) {
                    for (grupoSnapshot in snapshot.children) {
                        val grupo = grupoSnapshot.getValue(Grupo::class.java)
                        if (grupo?.miembrosIds?.contains(userId) == true) {
                            usuarioTieneGrupos = true
                            Log.d("InicioActivity", "Usuario $userId encontrado en grupo: ${grupo.nombre}")
                            break
                        }
                    }
                }

                if (usuarioTieneGrupos) {
                    Log.d("InicioActivity", "Usuario tiene grupos. Navegando a homev2.")
                    goToScreen(homev2::class.java)
                } else {
                    Log.d("InicioActivity", "Usuario NO tiene grupos. Navegando a home.")
                    goToScreen(Home::class.java)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("InicioActivity", "Error al verificar grupos: ${error.message}", error.toException())

                if (findViewById<View>(android.R.id.content).findViewById<View>(R.id.btn_ingresar) == null) {
                    Log.d("InicioActivity", "Error en onCancelled, inflando layout de inicio.")
                    setContentView(R.layout.activity_inicio)
                    val btnUnete = findViewById<Button>(R.id.btn_ingresar)
                    if (btnUnete != null) {
                        btnUnete.setOnClickListener {
                            val intent = Intent(this@InicioActivity, CrearCuentaActivity::class.java)
                            startActivity(intent)
                        }
                    } else {
                        Log.e("InicioActivity", "Error crítico en onCancelled: No se encontró R.id.btn_ingresar después de setContentView.")
                    }
                }
                Toast.makeText(this@InicioActivity, "Error al verificar datos. Intenta de nuevo.", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun <T : AppCompatActivity> goToScreen(activityClass: Class<T>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
