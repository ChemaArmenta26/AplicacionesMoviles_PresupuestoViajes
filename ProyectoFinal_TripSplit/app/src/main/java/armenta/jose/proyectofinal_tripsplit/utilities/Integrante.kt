package armenta.jose.proyectofinal_tripsplit.utilities

import armenta.jose.proyectofinal_tripsplit.R

data class Integrante(
    val id: Int,
    val nombre: String,
    val deuda: Double?,
    val imagenPerfilId: Int = R.mipmap.image_default_user
)
