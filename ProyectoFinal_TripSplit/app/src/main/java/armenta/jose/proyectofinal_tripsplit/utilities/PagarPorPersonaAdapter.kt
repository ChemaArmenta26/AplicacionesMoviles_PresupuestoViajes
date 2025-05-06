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
    private val listaPagarPorPersona: List<PagarPorPersona>
) : BaseAdapter() {

    override fun getCount(): Int = listaPagarPorPersona.size

    override fun getItem(position: Int): Any = listaPagarPorPersona[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_pagar_por_persona, parent, false)

        val personaNombre = view.findViewById<TextView>(R.id.tv_nombre_persona)
        val personaCantidad = view.findViewById<TextView>(R.id.tv_cantidad_persona)
        val personaImagen = view.findViewById<ImageView>(R.id.iv_persona_imagen)

        val pagarPersona = listaPagarPorPersona[position]

        // Asignar valores a las vistas
        personaNombre.text = pagarPersona.nombre
        personaCantidad.text = pagarPersona.cantidad
        personaImagen.setImageResource(pagarPersona.imagen)

        return view
    }
}


