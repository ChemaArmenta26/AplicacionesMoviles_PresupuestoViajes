package armenta.jose.proyectofinal_tripsplit.utilities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import armenta.jose.proyectofinal_tripsplit.R

class IntegranteGrupoAdapter(
    private val context: Context,
    private val integrantes: MutableList<Integrante>,
    private var isAdmin: Boolean,
    private val onRemove: (pos: Int) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = integrantes.size
    override fun getItem(position: Int): Any = integrantes[position]
    override fun getItemId(position: Int): Long = position.toLong()

    fun setIsAdmin(admin: Boolean) {
        isAdmin = admin
        notifyDataSetChanged()
    }

    @SuppressLint("ViewHolder")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val v = convertView
            ?: LayoutInflater.from(context)
                .inflate(R.layout.item_configuracion_grupo, parent, false)

        val iv        = v.findViewById<ImageView>(R.id.ivMember)
        val tvNombre  = v.findViewById<TextView>(R.id.tvMemberName)
        val btnRemove = v.findViewById<ImageButton>(R.id.btnRemoveMember)
        val tvEliminar1 = v.findViewById<TextView>(R.id.tvEliminar1)

        val integ = integrantes[position]
        tvNombre.text = integ.nombre
        iv.setImageResource(integ.imagenPerfilId)

        // Si NO soy admin => oculto todos los remove
        // Si SOY admin y esta es la fila 0 (admin) => oculto
        // Si SOY admin y fila>0 => muestro botón
        if (!isAdmin || position == 0) {
            btnRemove.visibility = View.GONE
            tvEliminar1.visibility = View.GONE
        } else {
            btnRemove.visibility = View.VISIBLE
            btnRemove.setOnClickListener {
                AlertDialog.Builder(context)
                    .setTitle("Eliminar integrante")
                    .setMessage("¿Seguro que quieres eliminar a ${integ.nombre}?")
                    .setPositiveButton("Sí") { _, _ ->
                        onRemove(position)
                    }
                    .setNegativeButton("No", null)
                    .show()
            }
        }

        return v
    }
}