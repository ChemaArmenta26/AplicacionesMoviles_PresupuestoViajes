package armenta.jose.proyectofinal_tripsplit.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import armenta.jose.proyectofinal_tripsplit.EditarGrupoActivity
import armenta.jose.proyectofinal_tripsplit.R

class TopBarFragment : Fragment() {

    companion object {
        private const val ARG_GROUP_ID = "ARG_GROUP_ID"


        fun newInstance(groupId: String) = TopBarFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_GROUP_ID, groupId)
            }
        }
    }

    // Aquí guardamos el groupId que nos pase el padre o el Intent
    private val groupId: String by lazy {
        arguments?.getString(ARG_GROUP_ID)
            ?: requireActivity().intent.getStringExtra("groupId")
                .orEmpty()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_bar, container, false)
        val iconSettings = view.findViewById<ImageView>(R.id.icon_settings)

        // Intent de la Activity contenedora
        val groupId = requireActivity()
            .intent
            .getStringExtra("groupId")
            .orEmpty()

        // Si no hay groupId disponible => oculto el icono
        iconSettings.visibility =
            if (groupId.isBlank()) View.GONE else View.VISIBLE

        iconSettings.setOnClickListener {
            // Si llega aquí, sabe que groupId NO es blank
            Intent(requireActivity(), EditarGrupoActivity::class.java).also {
                it.putExtra("groupId", groupId)
                startActivity(it)
            }
        }

        // resto de iconos …
        return view
    }
}