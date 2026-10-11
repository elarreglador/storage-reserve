package eu.elarreglador.storage.reserve.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ReserveKeys {
    const val IS_RESERVED = "is_reserved"
    const val RESERVED_SIZE_BYTES = "reserved_size_bytes"
}

object ReservePresets {
    val PRESETS = listOf(
        536_870_912L,   // 512 MB
        1_073_741_824L, // 1 GB
        2_147_483_648L  // 2 GB
    )
    val DEFAULT = PRESETS[1] // 1 GB
}

class ReserveRepository(private val sharedPreferences: SharedPreferences) {

    private val _reserveState = MutableStateFlow(getState())
    val reserveState: Flow<ReserveState> = _reserveState

    private fun getState(): ReserveState {
        return ReserveState(
            isReserved = sharedPreferences.getBoolean(ReserveKeys.IS_RESERVED, false),
            reservedSizeBytes = sharedPreferences.getLong(ReserveKeys.RESERVED_SIZE_BYTES, ReservePresets.DEFAULT),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun updateState() {
        _reserveState.value = getState()
    }

    suspend fun setReserved(isReserved: Boolean): Boolean = try {
        sharedPreferences.edit().putBoolean(ReserveKeys.IS_RESERVED, isReserved).apply()
        updateState()
        true
    } catch (e: Exception) {
        false
    }

    suspend fun setReservedSize(sizeBytes: Long): Boolean = try {
        sharedPreferences.edit().putLong(ReserveKeys.RESERVED_SIZE_BYTES, sizeBytes).apply()
        updateState()
        true
    } catch (e: Exception) {
        false
    }

    suspend fun updateState(isReserved: Boolean, sizeBytes: Long): Boolean = try {
        sharedPreferences.edit()
            .putBoolean(ReserveKeys.IS_RESERVED, isReserved)
            .putLong(ReserveKeys.RESERVED_SIZE_BYTES, sizeBytes)
            .apply()
        updateState()
        true
    } catch (e: Exception) {
        false
    }
}