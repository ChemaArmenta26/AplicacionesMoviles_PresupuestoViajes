import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import armenta.jose.proyectofinal_tripsplit.R
import armenta.jose.proyectofinal_tripsplit.utilities.Integrante

class ParticipantesAdapter : RecyclerView.Adapter<ParticipantesAdapter.ViewHolder>() {
    private var integrantes = ArrayList<Integrante>()
    private val participantesSeleccionados = mutableSetOf<String>() // Cambiado a String

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imagenPerfil: ImageView = view.findViewById(R.id.iv_perfil_integrante)
        val nombreIntegrante: TextView = view.findViewById(R.id.tv_nombre_integrante)
        val checkBox: CheckBox = view.findViewById(R.id.cb_participante)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_registro_gasto, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val integrante = integrantes[position]

        holder.imagenPerfil.setImageResource(integrante.imagenPerfilId)
        holder.nombreIntegrante.text = integrante.nombre

        // Configurar checkbox
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = participantesSeleccionados.contains(integrante.id.toString())
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                participantesSeleccionados.add(integrante.id.toString())
            } else {
                participantesSeleccionados.remove(integrante.id.toString())
            }
        }
    }

    override fun getItemCount() = integrantes.size

    fun actualizarLista(nuevosIntegrantes: List<Integrante>, participantesIds: List<String>) {
        integrantes = ArrayList(nuevosIntegrantes)
        participantesSeleccionados.clear()
        participantesSeleccionados.addAll(participantesIds)
        notifyDataSetChanged()
    }

    fun getParticipantesSeleccionados(): List<String> = participantesSeleccionados.toList()
}