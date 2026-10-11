package eu.elarreglador.storage.reserve.data

import android.content.Context
import android.os.StatFs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class ReserveManager(private val context: Context) {

    private val reserveFile: File
        get() = File(context.filesDir, "reserve.bin")

    companion object {
        const val CHUNK_SIZE = 4 * 1024 * 1024 // 4 MB
    }

    suspend fun createReserve(sizeBytes: Long, onProgress: (Float) -> Unit): Result<File> = withContext(Dispatchers.IO) {
        try {
            val freeSpace = getFreeSpaceBytes()
            if (freeSpace < sizeBytes) {
                return@withContext Result.failure(IOException("Espacio insuficiente: ${formatBytes(freeSpace)} disponible"))
            }

            val file = reserveFile
            if (file.exists()) {
                file.delete()
            }

            var written = 0L
            val buffer = ByteArray(CHUNK_SIZE)

            FileOutputStream(file).use { fos ->
                while (written < sizeBytes) {
                    val remaining = sizeBytes - written
                    val toWrite = if (remaining < CHUNK_SIZE) remaining.toInt() else CHUNK_SIZE
                    fos.write(buffer, 0, toWrite)
                    written += toWrite
                    onProgress(written.toFloat() / sizeBytes)
                }
                fos.flush()
            }

            // Verificar que el archivo ocupa el espacio real
            if (file.length() != sizeBytes) {
                file.delete()
                return@withContext Result.failure(IOException("Error: archivo creado con tamaño incorrecto"))
            }

            Result.success(file)
        } catch (e: Exception) {
            reserveFile.delete()
            Result.failure(e)
        }
    }

    suspend fun deleteReserve(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val file = reserveFile
            if (file.exists()) {
                val deleted = file.delete()
                if (!deleted) {
                    return@withContext Result.failure(IOException("No se pudo eliminar el archivo"))
                }
                // Forzar sync para liberación inmediata en F2FS
                Runtime.getRuntime().exec("sync").waitFor()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFreeSpaceBytes(): Long {
        val statFs = StatFs("/data")
        return statFs.availableBlocksLong * statFs.blockSizeLong
    }

    fun getReserveSize(): Long = if (reserveFile.exists()) reserveFile.length() else 0L

    fun isReserveExists(): Boolean = reserveFile.exists()

    private fun formatBytes(bytes: Long): String {
        return when {
            bytes >= 1_073_741_824L -> String.format("%.1f GB", bytes / 1_073_741_824.0)
            bytes >= 1_048_576L -> String.format("%.1f MB", bytes / 1_048_576.0)
            else -> String.format("%.1f KB", bytes / 1024.0)
        }
    }
}