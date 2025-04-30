package armenta.jose.proyectofinal_tripsplit.utilities

data class Grupo(
    val codigo: String? = null,
    val nombre: String? = null,
    val desde: String? = null,
    val hacia: String? = null,
    val salida: String? = null,
    val llegada: String? = null,
    val adminId: String? = null,
    var miembrosIds: List<String>? = null,
    var gastosIds: List<String>? = null
) {
    constructor() : this(null, null, null, null, null, null, null, null, null)
}