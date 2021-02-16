package col.rgm.koombea.main

import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import col.rgm.koombea.R
import col.rgm.koombea.repository.remote.IWebServices
import col.rgm.koombea.repository.remote.models.UsuariosDTO
import col.rgm.koombea.utilities.Constantes
import com.couchbase.lite.Blob
import com.couchbase.lite.Database
import com.couchbase.lite.MutableDocument
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(
    private val conexion: IWebServices,
    private val database: Database,
    private val constantes: Constantes
) :
    ViewModel() {
    private var jobGuardarDatos:Job?=null
    private var asyntaskHelper: AsyncTask<UsuariosDTO, Unit, Unit>? = null
    var mutableDoc: MutableDocument? = null
    private var gson: Gson = Gson()
    var progressBarVisibility = MutableLiveData<Int>()
    var mainContainerVisibility = MutableLiveData<Int>()
    var recyclerViewVisibility = MutableLiveData<Int>()
    var imagenSinDatosVisibility = MutableLiveData<Int>()
    var isRefreshing = MutableLiveData<Boolean>()
    var imagenMainVisibility = MutableLiveData<Int>()

    //Se encapsulan los datos obtenidos del webservice sobreescribiendo el get de los livedata
    private var _listaUsuarios = MutableLiveData<UsuariosDTO>()
    private var _listaObtenida = MutableLiveData<Database>()
    val listaUsuarios: LiveData<UsuariosDTO> get() = _listaUsuarios
    val listaObtenida: LiveData<Database> get() = _listaObtenida

    init {
        imagenMainVisibility.value = View.GONE
        imagenSinDatosVisibility.value = View.GONE
        progressBarVisibility.value = View.GONE
        mainContainerVisibility.value = View.VISIBLE
        recyclerViewVisibility.value = View.VISIBLE
    }

    //Obtiene informacion via webservice
    fun obtenerListaUsuarios() {
        try {
            progressBarVisibility.value = View.VISIBLE
            mainContainerVisibility.value = View.INVISIBLE
            viewModelScope.launch {
                try {
                    val response = conexion.getUsersList()
                    if (response.isSuccessful) {
                        _listaUsuarios.value = response.body()
                    } else {
                        _listaUsuarios.value = null
                    }
                }catch (e:Exception){
                    _listaUsuarios.value = null
                    Log.i(constantes.TAG_GENERAL, e.message!!)
                }
            }
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }

    //Guarda los datos obtenidos via webservice e inicia la descarga async de imagenes para guardar en BD
    fun guardarDatosEnBD(datosObtenidos: UsuariosDTO) {
        try {
            jobGuardarDatos = viewModelScope.launch {
                //Se serializan los datos de BD a JSON y se guardan en BD
                val json = gson.toJson(datosObtenidos)
                mutableDoc = MutableDocument()
                mutableDoc!!.setValue(constantes.KEY_DATA, json)
                //Se inicia la descarga de imagenes async
                asyntaskHelper = AsyncTaskHelper().execute(datosObtenidos)
            }
        } catch (e: Exception) {
            asyntaskHelper?.cancel(false)
            jobGuardarDatos?.cancel(null)
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }

    //Muestra u oculta cualquier imagen onclick
    fun mostrarImagen(url: String?, llaveBlob: String?, imageView: ImageView) {
        try {
            imageView.bringToFront()
            //Si solo se debe ocultar la imagen
            if (imagenMainVisibility.value == View.VISIBLE) {
                imagenMainVisibility.value = View.GONE

            }
            //Se muestra imagen cargada via URL
            else if (llaveBlob == null) {
                Picasso
                    .get()
                    .load(url)
                    .placeholder(R.drawable.imagen_vacia)
                    .error(R.drawable.imagen_vacia)
                    .into(imageView)
                imagenMainVisibility.value = View.VISIBLE

            }
            //Se muestra imagen cargada via BD
            else {
                val pic: Blob? =
                    mutableDoc!!.getBlob(llaveBlob)
                //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
                if(pic != null) {
                    if(pic.length()>0) {
                        val picByteArray = pic.content
                        var bitMap = BitmapFactory.decodeByteArray(
                            picByteArray,
                            0,
                            picByteArray!!.size
                        )
                        imageView.setImageBitmap(bitMap)
                    }
                    else{
                        imageView.setImageResource(R.drawable.imagen_vacia)
                    }
                }
                //Si la imagen no existe, se coloca la imagen por defecto
                else{
                    imageView.setImageResource(R.drawable.imagen_vacia)
                }
                imagenMainVisibility.value = View.VISIBLE
            }
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }

    //Clase helper para descargar imagenes en segundo plano
    inner class AsyncTaskHelper : AsyncTask<UsuariosDTO, Unit, Unit>() {
        //Se guardan todas las imagenes en formato BLOB
        override fun doInBackground(vararg datosObtenidos: UsuariosDTO?) {
            try {
                lateinit var mDocument: MutableDocument
                for (usuario in datosObtenidos[0]!!.data) {
                    database.save(obtenerBitmap(usuario.uid, usuario.profilePic))
                    for ((index, imagen) in usuario.post.pics.withIndex()) {
                        database.save(obtenerBitmap(usuario.uid + index.toString(), imagen))
                    }
                }
            } catch (e: Exception) {
                Log.i(constantes.TAG_GENERAL, e.message!!)
            }
        }

        override fun onPostExecute(result: Unit) {
            super.onPostExecute(result)
            try {
                //Se actualiza la BD
                _listaObtenida.value = database
            } catch (e: Exception) {
                Log.i(constantes.TAG_GENERAL, e.message!!)
            }
        }
    }

    //Descarga una imagen, guarda un BLOB y retorna un MutableDocument para persistir en BD
    fun obtenerBitmap(blobKey: String, url: String): MutableDocument {
        return try {
            var blob: Blob? = null
            val uri = URL(url)
            var urlConnection = uri.openConnection() as HttpURLConnection
            val statusCode: Int = urlConnection.responseCode
            if (statusCode == constantes.HTTP_OK) {
                val inputStream: InputStream = urlConnection.inputStream
                blob = Blob(constantes.TYPE_JPG, inputStream)
                mutableDoc!!.setBlob(blobKey, blob)
                Log.i(constantes.TAG_GENERAL, constantes.EXITO_CARGA)
                mutableDoc!!
            } else {
                Log.i(constantes.TAG_GENERAL, constantes.FALLA_CARGA)
                mutableDoc!!
            }
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
            mutableDoc!!
        }
    }

    //Cancela las descargas al destruirse el viewmodel
    override fun onCleared() {
        super.onCleared()
        cancelarDescargaImagenesAsync()
    }

    //Cancela la descarga asyncrona de imagenes
    fun cancelarDescargaImagenesAsync() {
        asyntaskHelper?.cancel(false)
        jobGuardarDatos?.cancel(null)
    }
}