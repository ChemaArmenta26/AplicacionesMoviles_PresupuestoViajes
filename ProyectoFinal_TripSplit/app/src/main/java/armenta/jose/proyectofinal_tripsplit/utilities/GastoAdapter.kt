package armenta.jose.proyectofinal_tripsplit.utilities

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import armenta.jose.proyectofinal_tripsplit.R
import java.text.NumberFormat
import java.util.Locale

class GastoAdapter(
    private val context: Context,
    private val listaGastos: List<Gasto>
) : BaseAdapter() {

    private val numberFormat = NumberFormat.getCurrencyInstance(Locale.getDefault())

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
        tipoGasto.text = gasto.categoria?.firstOrNull()?.toString() ?: "?"

        nombreGasto.text = gasto.nombre ?: context.getString(R.string.default_nombre_gasto)
        categoriaGasto.text = gasto.categoria ?: context.getString(R.string.default_categoria_gasto)

        montoGasto.text = NumberFormat.getCurrencyInstance().format(gasto.montoTotal ?: 0.0)

        return view
    }
}