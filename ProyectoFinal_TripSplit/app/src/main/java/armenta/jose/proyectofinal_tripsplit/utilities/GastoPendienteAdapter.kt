package armenta.jose.proyectofinal_tripsplit.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.BaseAdapter
import armenta.jose.proyectofinal_tripsplit.R

class GastoPendienteAdapter(
    private val context: Context,
    private val listaGastos: List<GastoPendiente>
) : BaseAdapter() {

    override fun getCount(): Int = listaGastos.size
    override fun getItem(position: Int): Any = listaGastos[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_gasto_pendiente, parent, false)

        val imagenPerfil = view.findViewById<ImageView>(R.id.iv_perfil_integrante)
        val nombre = view.findViewById<TextView>(R.id.tv_nombre_integrante)
        val deuda = view.findViewById<TextView>(R.id.tv_deuda_integrante)

        val gasto = listaGastos[position]

        imagenPerfil.setImageResource(gasto.imagen)
        nombre.text = gasto.nombre
        deuda.text = gasto.deuda

        return view
    }
}
