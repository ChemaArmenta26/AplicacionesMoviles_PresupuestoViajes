package armenta.jose.proyectofinal_tripsplit.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import armenta.jose.proyectofinal_tripsplit.R

class TotalGastadoAdapter(
    private val context: Context,
    private val listaGastos: List<TotalGastado>
) : BaseAdapter() {

    override fun getCount(): Int = listaGastos.size
    override fun getItem(position: Int): Any = listaGastos[position]
    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_total_gastado, parent, false)

        val imagenPerfil = view.findViewById<ImageView>(R.id.iv_perfil_integrante)
        val nombre = view.findViewById<TextView>(R.id.tv_nombre_integrante)
        val totalGastado = view.findViewById<TextView>(R.id.tv_deuda_integrante)

        val gasto = listaGastos[position]

        imagenPerfil.setImageResource(gasto.imagen)
        nombre.text = gasto.nombre
        totalGastado.text = gasto.total

        return view
    }
}
