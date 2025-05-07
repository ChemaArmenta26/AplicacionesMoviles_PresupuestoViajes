package armenta.jose.proyectofinal_tripsplit.utilities

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.R

class IntegranteRegistroAdapter : RecyclerView.Adapter<IntegranteRegistroAdapter.IntegranteViewHolder>() {

    private var listaMiembros = listOf<MiembroInfo>()
    private val seleccionados = mutableSetOf<String>()

    class IntegranteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var imagenPerfil: ImageView = itemView.findViewById(R.id.iv_perfil_integrante)
        var nombreIntegrante: TextView = itemView.findViewById(R.id.tv_nombre_integrante)
        var checkBoxParticipante: CheckBox = itemView.findViewById(R.id.cb_participante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IntegranteViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registro_gasto, parent, false)
        return IntegranteViewHolder(vista)
    }

    override fun onBindViewHolder(holder: IntegranteViewHolder, position: Int) {
        val miembro = listaMiembros[position]
        holder.nombreIntegrante.text = miembro.nombre
        holder.imagenPerfil.setImageResource(R.mipmap.image_default_user)


        holder.checkBoxParticipante.setOnCheckedChangeListener(null)
        holder.checkBoxParticipante.isChecked = seleccionados.contains(miembro.uid)

        holder.checkBoxParticipante.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                seleccionados.add(miembro.uid)
            } else {
                seleccionados.remove(miembro.uid)
            }

        }
    }

    override fun getItemCount(): Int {
        return listaMiembros.size
    }


    fun actualizarLista(nuevaLista: List<MiembroInfo>) {
        this.listaMiembros = nuevaLista
        notifyDataSetChanged()
    }

    fun obtenerParticipantesSeleccionados(): List<String> {
        return seleccionados.toList()
    }
}