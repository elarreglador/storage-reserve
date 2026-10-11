package eu.elarreglador.storage.reserve

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import eu.elarreglador.storage.reserve.data.ReserveRepository

class ReserveApplication : Application() {

    private var reserveRepository: ReserveRepository? = null

    override fun onCreate() {
        super.onCreate()
        val sharedPreferences = getSharedPreferences("reserve_prefs", Context.MODE_PRIVATE)
        reserveRepository = ReserveRepository(sharedPreferences)
    }

    fun getReserveRepository(): ReserveRepository = reserveRepository!!
}