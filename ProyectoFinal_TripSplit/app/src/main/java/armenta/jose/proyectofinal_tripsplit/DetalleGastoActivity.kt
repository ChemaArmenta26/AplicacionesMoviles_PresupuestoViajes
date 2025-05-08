package armenta.jose.proyectofinal_tripsplit

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.ui.fragments.TopBarFragment
import armenta.jose.proyectofinal_tripsplit.utilities.Integrante
import armenta.jose.proyectofinal_tripsplit.utilities.IntegrantesAdapter
import com.google.firebase.database.*

class DetalleGastoActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: IntegrantesAdapter
    private lateinit var dbRef: DatabaseReference

    private lateinit var tvNombreGasto: TextView
    private lateinit var tvMontoGasto: TextView
    private lateinit var tvCategoriaGasto: TextView

    private lateinit var gastoId: String
    private lateinit var grupoId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detalle_gasto)

        tvNombreGasto = findViewById(R.id.tv_nombre_gasto)
        tvMontoGasto = findViewById(R.id.tv_monto_gasto)
        tvCategoriaGasto = findViewById(R.id.tv_categoria_gasto)

        val btnFlechaAtras = findViewById<ImageButton>(R.id.btn_flecha_atras)
        btnFlechaAtras.setOnClickListener { finish() }

        if (savedInstanceState == null) {
            gastoId = intent.getStringExtra("GASTO_ID") ?: return
            grupoId = intent.getStringExtra("GRUPO_ID") ?: return

            val topBarFragment = TopBarFragment.newInstance(
                groupId = grupoId,
                gastoId = gastoId,
                showEditIcon = true
            )

            supportFragmentManager.beginTransaction()
                .replace(R.id.topBarFragment, topBarFragment)
                .commit()
        }


        recyclerView = findViewById(R.id.rv_integrantes)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = IntegrantesAdapter()
        recyclerView.adapter = adapter

        gastoId = intent.getStringExtra("GASTO_ID") ?: return
        grupoId = intent.getStringExtra("GRUPO_ID") ?: return

        dbRef = FirebaseDatabase.getInstance().reference

        cargarDatosDelGasto()
    }

    private fun cargarDatosDelGasto() {
        val gastoRef = dbRef.child("gastos").child(gastoId)

        gastoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val nombre = snapshot.child("nombre").getValue(String::class.java) ?: ""
                val categoria = snapshot.child("categoria").getValue(String::class.java) ?: ""
                val montoTotal = snapshot.child("montoTotal").getValue(Double::class.java) ?: 0.0
                val participantesSnapshot = snapshot.child("participantesIds")

                val participantesIds = participantesSnapshot.children.mapNotNull { it.getValue(String::class.java) }

                tvNombreGasto.text = nombre
                tvCategoriaGasto.text = categoria
                tvMontoGasto.text = "$%.2f".format(montoTotal)

                val deudaIndividual = if (participantesIds.isNotEmpty())
                    montoTotal / participantesIds.size
                else 0.0

                obtenerNombresUsuarios(participantesIds, deudaIndividual)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al cargar datos del gasto", error.toException())
            }
        })
    }

    private fun obtenerNombresUsuarios(participantesIds: List<String>, deuda: Double) {
        val usuariosRef = dbRef.child("Usuarios")

        usuariosRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = ArrayList<Integrante>()

                for ((index, id) in participantesIds.withIndex()) {
                    val usuario = snapshot.child(id)
                    val nombre = usuario.child("nombre").getValue(String::class.java) ?: "Desconocido"
                    val montoFormateado = String.format("%.2f", deuda).replace(",", ".").toDouble()
                    lista.add(Integrante(index + 1, nombre, montoFormateado))
                }

                adapter.listaIntegrantes = lista
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Error al obtener nombres de usuarios", error.toException())
            }
        })
    }
}
