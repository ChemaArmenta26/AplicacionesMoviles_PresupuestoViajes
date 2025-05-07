package armenta.jose.proyectofinal_tripsplit.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import armenta.jose.proyectofinal_tripsplit.EditarGrupoActivity
import armenta.jose.proyectofinal_tripsplit.EditarPerfil
import armenta.jose.proyectofinal_tripsplit.R
import com.google.firebase.auth.FirebaseAuth

class TopBarFragment : Fragment() {

    private lateinit var auth: FirebaseAuth

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
                .orEmpty()
    }

    private val currentGastoId: String? by lazy { arguments?.getString(ARG_GASTO_ID) }
    private val showEditIcon: Boolean by lazy { arguments?.getBoolean(ARG_SHOW_EDIT_ICON, false) ?: false }

    private lateinit var layoutEditGastoOptions: LinearLayout
    private lateinit var iconDoEditGasto: ImageView
    private lateinit var iconDoDeleteGasto: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
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

        iconSettings.visibility =
            if (groupId.isBlank()) View.GONE else View.VISIBLE

        iconSettings.setOnClickListener {
            Intent(requireActivity(), EditarGrupoActivity::class.java).also {
                it.putExtra("groupId", groupId)
                startActivity(it)
            }
        }


        if (auth.currentUser != null) {
            userProfileIcon.visibility = View.VISIBLE
            userProfileIcon.setOnClickListener {
                val intent = Intent(requireActivity(), EditarPerfil::class.java)
                startActivity(intent)
            }
        } else {
            userProfileIcon.visibility = View.GONE
        }

        if (showEditIcon && currentGastoId != null && currentGastoId!!.isNotBlank()) {
            iconEditGastoPrincipal.visibility = View.VISIBLE
            iconEditGastoPrincipal.setOnClickListener {
                layoutEditGastoOptions.visibility = if (layoutEditGastoOptions.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            iconDoEditGasto.setOnClickListener {

            }

            iconDoDeleteGasto.setOnClickListener {

            }

        } else {
            iconEditGastoPrincipal.visibility = View.GONE
            layoutEditGastoOptions.visibility = View.GONE
        }

        return view
    }
}