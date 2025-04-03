package armenta.jose.proyectofinal_tripsplit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.BaseAdapter
import armenta.jose.proyectofinal_tripsplit.R
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo

class GrupoAdapter(private val context: Context, private val grupos: List<Grupo>) : BaseAdapter() {

    override fun getCount(): Int = grupos.size
    override fun getItem(position: Int): Grupo = grupos[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_viaje, parent, false)
        val grupo = getItem(position)

        // Asigna valores a los TextView del item_viaje.xml
        view.findViewById<TextView>(R.id.tv_inicial_grupo).text = grupo.nombre.first().toString() // Inicial del nombre
        view.findViewById<TextView>(R.id.tv_nombre_grupo).text = grupo.nombre
        view.findViewById<TextView>(R.id.tv_destino_grupo).text = "${grupo.desde} - ${grupo.hacia}"

        return view
    }
}
