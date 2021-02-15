package col.rgm.koombea.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import col.rgm.koombea.repository.remote.IWebServices
import col.rgm.koombea.utilities.Constantes
import com.couchbase.lite.Database

class MainVMFactory(val conexion: IWebServices, val database: Database, val constantes: Constantes) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(conexion, database, constantes) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}