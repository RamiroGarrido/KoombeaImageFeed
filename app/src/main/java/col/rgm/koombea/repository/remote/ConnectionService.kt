package col.rgm.koombea.repository.remote

import android.content.Context
import android.util.Log
import col.rgm.koombea.R
import col.rgm.koombea.utilities.Constantes
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ConnectionService(val constates: Constantes, val endPoint:String) {

    private var retrofit: Retrofit? = getInstance()
    val conexion: IWebServices by lazy {
        retrofit!!.create(IWebServices::class.java)
    }

    fun getInstance():Retrofit?{
        return try{
            Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        catch (e: Exception) {
            Log.e(constates.TAG_GENERAL, e.message!!)
            null
        }
    }
}