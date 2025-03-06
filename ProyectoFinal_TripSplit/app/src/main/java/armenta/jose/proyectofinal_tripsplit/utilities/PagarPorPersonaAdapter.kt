package armenta.jose.proyectofinal_tripsplit.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import armenta.jose.proyectofinal_tripsplit.R

class PagarPorPersonaAdapter(
    private val context: Context,
    private val listaPersonas: List<PagarPorPersona>
) : BaseAdapter() {

    override fun getCount(): Int = listaPersonas.size
    override fun getItem(position: Int): Any = listaPersonas[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_integrante_detalle, parent, false)

        val imagenPerfil = view.findViewById<ImageView>(R.id.iv_perfil_integrante)
        val nombre = view.findViewById<TextView>(R.id.tv_nombre_integrante)
        val deuda = view.findViewById<TextView>(R.id.tv_deuda_integrante)

        val persona = listaPersonas[position]

        imagenPerfil.setImageResource(persona.imagen)
        nombre.text = persona.nombre
        deuda.text = persona.cantidad

        return view
    }
}
