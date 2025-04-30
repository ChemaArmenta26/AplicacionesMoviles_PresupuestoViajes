package armenta.jose.proyectofinal_tripsplit.utilities

data class MiembroInfo(val uid: String, val nombre: String) {
    override fun toString(): String = nombre
}