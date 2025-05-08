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
    private var integrantes = mutableListOf<Integrante>()
    private val participantesSeleccionados = mutableSetOf<String>()
    private val integranteToUserIdMap = mutableMapOf<Int, String>() // Mapa para relacionar id con userId

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

        holder.nombreIntegrante.text = integrante.nombre
        holder.imagenPerfil.setImageResource(integrante.imagenPerfilId)

        // Configurar checkbox usando el userId del mapa
        holder.checkBox.setOnCheckedChangeListener(null)
        val userId = integranteToUserIdMap[integrante.id]
        holder.checkBox.isChecked = userId?.let { participantesSeleccionados.contains(it) } ?: false
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            userId?.let {
                if (isChecked) {
                    participantesSeleccionados.add(it)
                } else {
                    participantesSeleccionados.remove(it)
                }
            }
        }
    }

    override fun getItemCount() = integrantes.size

    fun actualizarLista(
        nuevosIntegrantes: List<Integrante>,
        participantesIds: List<String>,
        userIdMap: Map<Int, String>
    ) {
        integrantes.clear()
        integrantes.addAll(nuevosIntegrantes)
        integranteToUserIdMap.clear()
        integranteToUserIdMap.putAll(userIdMap)
        participantesSeleccionados.clear()
        participantesSeleccionados.addAll(participantesIds)
        notifyDataSetChanged()
    }

    fun getParticipantesSeleccionados(): List<String> = participantesSeleccionados.toList()
}