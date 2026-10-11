package eu.elarreglador.storage.reserve.data

data class ReserveState(
    val isReserved: Boolean = false,
    val reservedSizeBytes: Long = ReservePresets.DEFAULT,
    val lastUpdated: Long = System.currentTimeMillis()
)