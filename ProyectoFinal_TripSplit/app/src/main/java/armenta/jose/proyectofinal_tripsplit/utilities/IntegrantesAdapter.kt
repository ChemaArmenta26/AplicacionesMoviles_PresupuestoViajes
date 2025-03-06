package armenta.jose.proyectofinal_tripsplit.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.R

class IntegrantesAdapter : RecyclerView.Adapter<IntegrantesAdapter.IntegranteViewHolder>() {

    var listaIntegrantes = ArrayList<Integrante>()

    class IntegranteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenPerfil: ImageView = itemView.findViewById(R.id.iv_perfil_integrante)
        var nombreIntegrante: TextView = itemView.findViewById(R.id.tv_nombre_integrante)
        var deudaIntegrante: TextView = itemView.findViewById(R.id.tv_deuda_integrante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntegranteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_integrante_detalle, parent, false)
        return IntegranteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: IntegranteViewHolder, position: Int) {
        val integrante = listaIntegrantes[position]

        holder.imagenPerfil.setImageResource(integrante.imagenPerfilId)
        holder.nombreIntegrante.text = integrante.nombre
        holder.deudaIntegrante.text = "$${integrante.deuda}"
    }

    override fun getItemCount(): Int {
        return listaIntegrantes.size
    }
}