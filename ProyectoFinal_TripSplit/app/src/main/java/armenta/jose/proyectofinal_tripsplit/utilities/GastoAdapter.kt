package armenta.jose.proyectofinal_tripsplit.utilities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import armenta.jose.proyectofinal_tripsplit.R

class GastoAdapter(
    private val context: Context,
    private val listaGastos: List<Gasto>
) : BaseAdapter() {

    override fun getCount(): Int = listaGastos.size

    override fun getItem(position: Int): Any = listaGastos[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_gasto, parent, false)

        val tipoGasto = view.findViewById<TextView>(R.id.tv_tipo_gasto)
        val nombreGasto = view.findViewById<TextView>(R.id.tv_nombre_gasto)
        val categoriaGasto = view.findViewById<TextView>(R.id.tv_categoria_gasto)
        val montoGasto = view.findViewById<TextView>(R.id.tv_monto_gasto)

        val gasto = listaGastos[position]

        nombreGasto.text = gasto.nombre
        categoriaGasto.text = gasto.categoria
        montoGasto.text = gasto.montoTotal.toString()

        return view
    }
}
