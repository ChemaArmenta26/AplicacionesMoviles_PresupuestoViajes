package armenta.jose.proyectofinal_tripsplit.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.BaseAdapter
import armenta.jose.proyectofinal_tripsplit.R
import armenta.jose.proyectofinal_tripsplit.utilities.Grupo

class GrupoAdapter(private val context: Context, private var grupos: List<Grupo>) : BaseAdapter() {

    private class ViewHolder {
        lateinit var inicialGrupo: TextView
        lateinit var nombreGrupo: TextView
        lateinit var destinoGrupo: TextView
    }

    override fun getCount(): Int = grupos.size
    override fun getItem(position: Int): Grupo = grupos[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_viaje, parent, false)
            holder = ViewHolder()
            holder.inicialGrupo = view.findViewById(R.id.tv_inicial_grupo)
            holder.nombreGrupo = view.findViewById(R.id.tv_nombre_grupo)
            holder.destinoGrupo = view.findViewById(R.id.tv_destino_grupo)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val grupo = getItem(position)

        holder.inicialGrupo.text = grupo.nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.nombreGrupo.text = grupo.nombre ?: "Nombre no disponible"
        val desdeText = grupo.desde ?: "Origen desc."
        val haciaText = grupo.hacia ?: "Destino desc."
        holder.destinoGrupo.text = "$desdeText - $haciaText"

        return view
    }

}