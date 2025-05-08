package armenta.jose.proyectofinal_tripsplit.ui.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import armenta.jose.proyectofinal_tripsplit.EditarGastoActivity
import armenta.jose.proyectofinal_tripsplit.EditarGrupoActivity
import armenta.jose.proyectofinal_tripsplit.EditarPerfil
import armenta.jose.proyectofinal_tripsplit.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class TopBarFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference

    companion object {
        private const val ARG_GROUP_ID = "ARG_GROUP_ID"
        private const val ARG_GASTO_ID = "ARG_GASTO_ID"
        private const val ARG_SHOW_EDIT_ICON = "ARG_SHOW_EDIT_ICON"

        fun newInstance(groupId: String? = null, gastoId: String? = null, showEditIcon: Boolean = false) = TopBarFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GROUP_ID, groupId)
                putString(ARG_GASTO_ID, gastoId)
                putBoolean(ARG_SHOW_EDIT_ICON, showEditIcon)
            }
        }
    }

    private val groupId: String by lazy {
        arguments?.getString(ARG_GROUP_ID)
            ?: requireActivity().intent.getStringExtra("groupId")
            ?: ""
    }

    private val currentGastoId: String? by lazy { arguments?.getString(ARG_GASTO_ID) }
    private val showEditIcon: Boolean by lazy { arguments?.getBoolean(ARG_SHOW_EDIT_ICON, false) ?: false }

    private lateinit var layoutEditGastoOptions: LinearLayout
    private lateinit var iconDoEditGasto: ImageView
    private lateinit var iconDoDeleteGasto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
        dbRef = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_bar, container, false)

        val iconSettings = view.findViewById<ImageView>(R.id.icon_settings)
        val userProfileIcon = view.findViewById<ImageView>(R.id.icon_user)
        val iconEditGastoPrincipal = view.findViewById<ImageView>(R.id.icon_edit)

        layoutEditGastoOptions = view.findViewById(R.id.layout_edit_gasto_options)
        iconDoEditGasto = view.findViewById(R.id.icon_do_edit_gasto)
        iconDoDeleteGasto = view.findViewById(R.id.icon_do_delete_gasto)

        iconSettings.visibility = if (groupId.isBlank()) View.GONE else View.VISIBLE

        iconSettings.setOnClickListener {
            Intent(requireActivity(), EditarGrupoActivity::class.java).also {
                it.putExtra("groupId", groupId)
                startActivity(it)
            }
        }

        if (auth.currentUser != null) {
            userProfileIcon.visibility = View.VISIBLE
            userProfileIcon.setOnClickListener {
                startActivity(Intent(requireActivity(), EditarPerfil::class.java))
            }
        } else {
            userProfileIcon.visibility = View.GONE
        }

        if (showEditIcon && !currentGastoId.isNullOrBlank()) {
            iconEditGastoPrincipal.visibility = View.VISIBLE
            iconEditGastoPrincipal.setOnClickListener {
                layoutEditGastoOptions.visibility =
                    if (layoutEditGastoOptions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            iconDoEditGasto.setOnClickListener {
                layoutEditGastoOptions.visibility = View.GONE
                if (currentGastoId.isNullOrBlank() || groupId.isBlank()) {
                    Toast.makeText(requireContext(), "Error: IDs no válidos", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                val intent = Intent(requireActivity(), EditarGastoActivity::class.java).apply {
                    putExtra("GRUPO_ID", groupId)
                    putExtra("GASTO_ID", currentGastoId)
                }
                Log.d("TopBarFragment", "Enviando - GroupID: $groupId, GastoID: $currentGastoId")
                startActivity(intent)
            }

            iconDoDeleteGasto.setOnClickListener {
                layoutEditGastoOptions.visibility = View.GONE
                AlertDialog.Builder(requireContext())
                    .setTitle("Eliminar gasto")
                    .setMessage("¿Estás seguro de que deseas eliminar este gasto?")
                    .setPositiveButton("Sí") { _, _ ->
                        eliminarGasto()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }

        } else {
            iconEditGastoPrincipal.visibility = View.GONE
            layoutEditGastoOptions.visibility = View.GONE
        }

        return view
    }

    private fun eliminarGasto() {
        if (currentGastoId.isNullOrBlank()) {
            Toast.makeText(requireContext(), "ID de gasto no válido", Toast.LENGTH_SHORT).show()
            return
        }

        dbRef.child("gastos").child(currentGastoId!!)
            .removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Gasto eliminado correctamente", Toast.LENGTH_SHORT).show()
                requireActivity().finish()
            }
            .addOnFailureListener { e ->
                Log.e("TopBarFragment", "Error al eliminar el gasto", e)
                Toast.makeText(requireContext(), "Error al eliminar el gasto", Toast.LENGTH_SHORT).show()
            }
    }
}