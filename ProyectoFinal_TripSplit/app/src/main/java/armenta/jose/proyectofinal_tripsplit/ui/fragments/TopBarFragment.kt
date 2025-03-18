package armenta.jose.proyectofinal_tripsplit.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import armenta.jose.proyectofinal_tripsplit.EditarGrupoActivity
import armenta.jose.proyectofinal_tripsplit.Home
import armenta.jose.proyectofinal_tripsplit.InicioActivity
import armenta.jose.proyectofinal_tripsplit.R
import com.google.firebase.auth.FirebaseAuth

class TopBarFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_bar, container, false)

        val logoAvion = view.findViewById<ImageView>(R.id.icon_left)
        val iconSettings = view.findViewById<ImageView>(R.id.icon_settings)

        logoAvion?.setOnClickListener {
            val intent = Intent(activity, InicioActivity::class.java)
            FirebaseAuth.getInstance().signOut()
            startActivity(intent)
        }

        iconSettings?.setOnClickListener {
            val intent = Intent(activity, EditarGrupoActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
