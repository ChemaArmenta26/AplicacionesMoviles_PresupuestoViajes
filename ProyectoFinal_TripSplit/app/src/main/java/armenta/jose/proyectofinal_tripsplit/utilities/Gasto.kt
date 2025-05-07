package armenta.jose.proyectofinal_tripsplit.utilities

import java.io.Serializable

data class Gasto(
    
    var groupId: String? = null,
    var nombre: String? = null,
    var montoTotal: Double? = null,
    var categoria: String? = null,
    var participantesIds: List<String>? = null,
    var pagadorId: String? = null,
    var creadorId: String? = null,
    var id: String? = null
    ){
    constructor() : this(null, null, null, null, null, null, null)
}