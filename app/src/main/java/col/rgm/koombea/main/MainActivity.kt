package col.rgm.koombea.main

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import col.rgm.koombea.R
import col.rgm.koombea.databinding.ActivityMainBinding
import col.rgm.koombea.main.adapters.RecyclerViewUsuariosAdapter
import col.rgm.koombea.repository.remote.ConnectionService
import col.rgm.koombea.repository.remote.models.UsuariosDTO
import col.rgm.koombea.utilities.Constantes
import com.couchbase.lite.CouchbaseLite
import com.couchbase.lite.Database
import com.couchbase.lite.DatabaseConfiguration
import com.google.gson.Gson
import com.google.gson.GsonBuilder


class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener,
    View.OnClickListener {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private lateinit var database: Database
    private val constantes = Constantes()
    private val conService = ConnectionService(constantes, constantes.URL_KOOMBEA)

    //Configuraciones iniciales
    override fun onCreate(savedInstanceState: Bundle?) {
        try {
            super.onCreate(savedInstanceState)
            this.supportActionBar?.hide()
            binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
            //OJO, la BD requiere de internet inicialmente para cargar. No se verifica conexion a internet por requerimiento del documento
            createDB()
            viewModel = ViewModelProvider(
                this,
                MainVMFactory(conService.conexion, database, constantes)
            ).get(MainViewModel::class.java)
            binding.mainViewModel = viewModel
            binding.lifecycleOwner = this
            setObservers()
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }
    //Se crea BD. OJO el app debe tener internet al iniciarse. No se agrega validacion de internet por requerimiento del documento
    private fun createDB() {
        try {
            // Initialize the Couchbase Lite system
            CouchbaseLite.init(applicationContext)
            // Get the database (and create it if it doesn’t exist).
            val config = DatabaseConfiguration()
            config.directory = applicationContext.filesDir.absolutePath
            database = Database(constantes.DATABASE, config)
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }

    //Se configuran las vistas de la interfaz en onResume, ya que en onCreate no han sido creadas.
    override fun onResume() {
        try {
            super.onResume()
            binding.swipeRefreshLayoutMain.setOnRefreshListener(this)
            binding.imagenMain.setOnClickListener(this)
            binding.recyclerViewMain.layoutManager = LinearLayoutManager(
                applicationContext,
                RecyclerView.VERTICAL,
                false
            )
            viewModel.obtenerListaUsuarios()
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }
    //Se cargan los datos desde BD, si existen
    private fun cargarDesdeBD() {
        try {
            viewModel.cancelarDescargaImagenesAsync()
            val gson: Gson = GsonBuilder().create()
            if(viewModel.mutableDoc != null) {
                restaurarVisibilidades()
                val document = database.getDocument(viewModel.mutableDoc!!.id)
                val json: String = document.getValue(constantes.KEY_DATA) as String
                val offUsuariosDTO: UsuariosDTO = gson.fromJson(json, UsuariosDTO::class.java)
                crearMensaje(applicationContext, getString(R.string.cargaOffline))
                binding.recyclerViewMain.adapter =
                    RecyclerViewUsuariosAdapter(
                        this,
                        offUsuariosDTO,
                        document.toMutable(),
                        constantes
                    )
            }
            else{
                sinDatos()
            }
            viewModel.isRefreshing.value = false
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }

    private fun sinDatos() {
        viewModel.imagenSinDatosVisibility.value = View.VISIBLE
        viewModel.recyclerViewVisibility.value = View.GONE
        viewModel.mainContainerVisibility.value = View.VISIBLE
        viewModel.imagenMainVisibility.value = View.GONE
        viewModel.progressBarVisibility.value = View.GONE
        crearMensaje(applicationContext, getString(R.string.noExistenDatos))
    }

    //Se observan datos obtenidos del viewmodel
    private fun setObservers() {
        try {
            viewModel.listaUsuarios.observe(this) {
                //Si se obtienen datos del webservice, entra
                if (it != null) {
                    //Una vez se reciben los datos del webservice, se guardan los datos en BD
                    viewModel.guardarDatosEnBD(it)
                    binding.recyclerViewMain.adapter =
                        RecyclerViewUsuariosAdapter(this, it, null, constantes)
                    restaurarVisibilidades()
                }
                //Si no se obtienen datos online, se intenta de manera offline
                else{
                    cargarDesdeBD()
                }
            }
            viewModel.listaObtenida.observe(this) {
                if (it != null) {
                    //Una vez se tienen todos los datos e imagenes guardadas, se actualiza la BD.
                    database = it
                }
            }
            viewModel.isRefreshing.observe(this) {
                if (it != null) {
                    binding.swipeRefreshLayoutMain.isRefreshing = it
                }
            }
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }
    //Se restauran las visibilidades
    fun restaurarVisibilidades() {
        try {
            viewModel.progressBarVisibility.value = View.GONE
            viewModel.imagenMainVisibility.value = View.GONE
            viewModel.mainContainerVisibility.value = View.VISIBLE
            viewModel.recyclerViewVisibility.value = View.VISIBLE
            viewModel.imagenSinDatosVisibility.value = View.GONE
            viewModel.isRefreshing.value = false
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }
    //No se desea regresar a la pantalla de splash
    override fun onBackPressed() {
        //No se usa
    }
    //Se crean mensajes toast
    fun crearMensaje(contexto: Context, mensaje: String) {
        try {
            Toast.makeText(contexto, mensaje, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }
    //on swipe refresh se actualiza la lista desde el webservice
    override fun onRefresh() {
        try {
            viewModel.isRefreshing.value = true
            restaurarVisibilidades()
            viewModel.cancelarDescargaImagenesAsync()
            viewModel.obtenerListaUsuarios()
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }
    //Evento para todas las imagenes
    override fun onClick(v: View?) {
        try {
            //Entra si es una imagen de recyclerview
            if (v!!.id != R.id.imagenMain) {
                var estado = (v.tag as String).split(" ")
                //estado[0] = indica si la imagen es online u offline
                //estado[1] = Si es online, trae la url de la imagen. Si es offline, trae la llave del blob de la imagen en BD
                when (estado[0]) {
                    //Si se tiene internet
                    constantes.ON -> viewModel.mostrarImagen(estado[1], null, binding.imagenMain)
                    //Si no se tiene internet
                    else -> viewModel.mostrarImagen(null, estado[1], binding.imagenMain)
                }
            }
            //Entra si es la imagen grande principal
            else {
                viewModel.mostrarImagen(null, null, binding.imagenMain)
            }
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }

}