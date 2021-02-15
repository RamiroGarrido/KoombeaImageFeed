package col.rgm.koombea.main.adapters

import android.graphics.Bitmap
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import col.rgm.koombea.R
import col.rgm.koombea.main.MainActivity
import col.rgm.koombea.utilities.Constantes
import com.squareup.picasso.Picasso

class RecyclerViewImagenesHAdapter(
    val activity: MainActivity,
    val pics: List<String>?,
    val picsOffline: MutableList<Bitmap?>?,
    val user:String,
    val constantes: Constantes
) :
    RecyclerView.Adapter<RecyclerViewImagenesHAdapter.ViewHolder>() {

    //Crea un nuevo layout para el recyclerview horizontal
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view.
        val v = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.rv_adapter_imagenes_h_opcion4, viewGroup, false)
        return ViewHolder(v)
    }

    //Define el tamaño del recyclerview dependiendo si recibe datos online u offline
    override fun getItemCount(): Int {
        return pics?.size ?: picsOffline!!.size
    }

    //Se cargan las imagenes en el recyclerview horizontal dependiendo si se tienen datos online u offline
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            //Si se cargan imagenes desde el webservice
            if (pics != null) {
                holder.imagen.tag = constantes.ON +" "+pics[position]
                Picasso
                    .get()
                    .load(pics[position])
                    .placeholder(R.drawable.imagen_vacia)
                    .error(R.drawable.imagen_vacia)
                    .into(holder.imagen)
            }
            //Si se cargan imagenes desde BD
            else {
                //Se suma uno al tag porque el formato de 4 imagenes o mas tiene una imagen colocada fuera del recyclerview horizontal
                holder.imagen.tag = constantes.OFF +" "+user+position.plus(1).toString()
                if(picsOffline!![position] != null) {
                    holder.imagen.setImageBitmap(picsOffline[position])
                }
                else{
                    holder.imagen.setImageResource(R.drawable.imagen_vacia)
                }
            }
            holder.imagen.setOnClickListener(activity)
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message!!)
        }
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        val imagen: ImageView = v.findViewById<ImageView>(R.id.imagenRVHCOpc4)
    }

}