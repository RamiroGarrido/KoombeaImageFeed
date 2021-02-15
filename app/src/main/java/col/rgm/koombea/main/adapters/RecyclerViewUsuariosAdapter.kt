package col.rgm.koombea.main.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import col.rgm.koombea.R
import col.rgm.koombea.main.MainActivity
import col.rgm.koombea.repository.remote.models.UsuariosDTO
import col.rgm.koombea.utilities.Constantes
import com.couchbase.lite.Blob
import com.couchbase.lite.MutableDocument
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class RecyclerViewUsuariosAdapter(
    val activity: MainActivity,
    val listaUsuarios: UsuariosDTO,
    val listaImagenes: MutableDocument?,
    val constantes: Constantes
) : RecyclerView.Adapter<RecyclerViewUsuariosAdapter.BaseViewHolder>() {

    //Se decide que layout a mostrar dependiendo del numero de imagenes.
    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): BaseViewHolder {
        return when (viewType) {
            1 -> {
                val v = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.rv_adapter_usuarios_opcion_1, viewGroup, false)
                Opcion1ViewHolder(v)
            }
            2 -> {
                val v = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.rv_adapter_usuarios_opcion_2, viewGroup, false)
                Opcion2ViewHolder(v)
            }
            3 -> {
                val v = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.rv_adapter_usuarios_opcion_3, viewGroup, false)
                Opcion3ViewHolder(v)
            }
            //Para 4 o más imágenes
            else -> {
                val v = LayoutInflater.from(viewGroup.context)
                    .inflate(R.layout.rv_adapter_usuarios_opcion_4, viewGroup, false)
                Opcion4ViewHolder(v)
            }
        }
    }

    //Se devuelve el numero de imagenes por usuario
    override fun getItemViewType(position: Int): Int {
        val item = listaUsuarios.data[position]
        return item.post.pics.size
    }

    override fun getItemCount(): Int {
        return listaUsuarios.data.size
    }
    //Se cargan las vistas dependiendo del numero de imagenes
    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        try {
            val calendar = Calendar.getInstance()
            var fechaCreacion: String? = null
            val parser = SimpleDateFormat(
                constantes.FORMATO_DATETIME_BD,
                Locale(constantes.LOCALIZACION_INGLES)
            )
            val formatter = SimpleDateFormat(constantes.FORMATO_DATETIME)
            val date: Date = parser.parse(listaUsuarios.data[position].post.date)
            fechaCreacion = formatter.format(date)
            calendar.time = date
            fechaCreacion += definirSufijo(calendar.get(Calendar.DAY_OF_MONTH))
            when (holder) {
                is Opcion1ViewHolder -> {
                    cargarOpcion1(fechaCreacion, holder, position)
                }
                is Opcion2ViewHolder -> {
                    cargarOpcion2(fechaCreacion, holder, position)
                }
                is Opcion3ViewHolder -> {
                    cargarOpcion3(fechaCreacion, holder, position)
                }
                is Opcion4ViewHolder -> {
                    cargarOpcion4(fechaCreacion, holder, position)
                }
            }
        } catch (e: Exception) {
            Log.i(constantes.TAG_GENERAL, e.message)
        }
    }

    fun cargarOpcion1(fechaCreacion: String, holder: Opcion1ViewHolder, position: Int) {
        holder.nombreUsuarioOpc1.text = listaUsuarios.data[position].name
        holder.emailUsuarioOpc1.text = listaUsuarios.data[position].email
        holder.fechaPostOpc1.text = fechaCreacion
        //Si se cargan datos desde el webservice
        if (listaImagenes == null) {
            holder.imagenOpc1.tag = constantes.ON +" "+listaUsuarios.data[position].post.pics[0]
            Picasso
                .get()
                .load(listaUsuarios.data[position].post.pics[0])
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenOpc1)
            Picasso
                .get()
                .load(listaUsuarios.data[position].profilePic)
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenHeaderOpc1)
        }
        //Si se cargan imagenes desde BD interna
        else {
            val profilePicB: Blob? =
                listaImagenes.getBlob(listaUsuarios.data[position].uid)
            //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
            if(profilePicB != null) {
                val profilePic = profilePicB.content
                var profilePicBitMap = BitmapFactory.decodeByteArray(
                    profilePic,
                    0,
                    profilePic!!.size
                )
                holder.imagenHeaderOpc1.setImageBitmap(profilePicBitMap)
            }
            //Si la imagen no existe, se coloca la imagen por defecto
            else{
                holder.imagenHeaderOpc1.setImageResource(R.drawable.imagen_vacia)
            }
            val llaveBlob = listaUsuarios.data[position].uid + "0"
            holder.imagenOpc1.tag = constantes.OFF +" "+ llaveBlob
            val pic: Blob? = listaImagenes.getBlob(llaveBlob)
            //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
            if(pic != null) {
                val picByteArray = pic.content
                var bitMap = BitmapFactory.decodeByteArray(
                    picByteArray,
                    0,
                    picByteArray!!.size
                )
                holder.imagenOpc1.setImageBitmap(bitMap)
            }
            //Si la imagen no existe, se coloca la imagen por defecto
            else{
                holder.imagenOpc1.setImageResource(R.drawable.imagen_vacia)
            }
        }
        holder.imagenOpc1.setOnClickListener(activity)
    }

    fun cargarOpcion2(fechaCreacion: String, holder: Opcion2ViewHolder, position: Int) {
        holder.nombreUsuarioOpc2.text = listaUsuarios.data[position].name
        holder.emailUsuarioOpc2.text = listaUsuarios.data[position].email
        holder.fechaPostOpc2.text = fechaCreacion
        //Si se cargan datos desde el webservice
        if (listaImagenes == null) {
            holder.imagenAOpc2.tag = constantes.ON +" "+ listaUsuarios.data[position].post.pics[0]
            holder.imagenBOpc2.tag = constantes.ON +" "+ listaUsuarios.data[position].post.pics[1]
            Picasso
                .get()
                .load(listaUsuarios.data[position].profilePic)
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenHeaderOpc2)
            Picasso
                .get()
                .load(listaUsuarios.data[position].post.pics[0])
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenAOpc2)
            Picasso
                .get()
                .load(listaUsuarios.data[position].post.pics[1])
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenBOpc2)
        }
        //Si se cargan imagenes desde BD interna
        else {
            holder.imagenAOpc2.tag = constantes.OFF +" "+ listaUsuarios.data[position].uid+"0"
            holder.imagenBOpc2.tag = constantes.OFF +" "+ listaUsuarios.data[position].uid+"1"
            val profilePicB: Blob? =
                listaImagenes.getBlob(listaUsuarios.data[position].uid)
            //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
            if(profilePicB != null) {
                val profilePic = profilePicB.content
                var profilePicBitMap = BitmapFactory.decodeByteArray(
                    profilePic,
                    0,
                    profilePic!!.size
                )
                holder.imagenHeaderOpc2.setImageBitmap(profilePicBitMap)
            }
            //Si la imagen no existe, se coloca la imagen por defecto
            else{
                holder.imagenHeaderOpc2.setImageResource(R.drawable.imagen_vacia)
            }
            for (index in 0..listaUsuarios.data[position].post.pics.size.minus(1)) {
                val pic: Blob? =
                    listaImagenes.getBlob(listaUsuarios.data[position].uid + index.toString())
                //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
                if(pic != null) {
                    val picByteArray = pic.content
                    var bitMap = BitmapFactory.decodeByteArray(
                        picByteArray,
                        0,
                        picByteArray!!.size
                    )
                    when (index) {
                        0 -> holder.imagenAOpc2.setImageBitmap(bitMap)
                        1 -> holder.imagenBOpc2.setImageBitmap(bitMap)
                    }
                }
                //Si la imagen no existe, se coloca la imagen por defecto
                else{
                    when (index) {
                        0 -> holder.imagenAOpc2.setImageResource(R.drawable.imagen_vacia)
                        1 -> holder.imagenBOpc2.setImageResource(R.drawable.imagen_vacia)
                    }
                }
            }
        }
        holder.imagenAOpc2.setOnClickListener(activity)
        holder.imagenBOpc2.setOnClickListener(activity)
    }

    fun cargarOpcion3(fechaCreacion: String, holder: Opcion3ViewHolder, position: Int) {
        holder.nombreUsuarioOpc3.text = listaUsuarios.data[position].name
        holder.emailUsuarioOpc3.text = listaUsuarios.data[position].email
        holder.fechaPostOpc3.text = fechaCreacion
        //Si se cargan datos desde el webservice
        if (listaImagenes == null) {
            holder.imagenAOpc3.tag = constantes.ON +" "+listaUsuarios.data[position].post.pics[0]
            holder.imagenBOpc3.tag = constantes.ON +" "+listaUsuarios.data[position].post.pics[1]
            holder.imagenCOpc3.tag = constantes.ON +" "+listaUsuarios.data[position].post.pics[2]
            Picasso
                .get()
                .load(listaUsuarios.data[position].profilePic)
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenHeaderOpc3)
            Picasso
                .get()
                .load(listaUsuarios.data[position].post.pics[0])
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenAOpc3)
            Picasso
                .get()
                .load(listaUsuarios.data[position].post.pics[1])
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenBOpc3)
            Picasso
                .get()
                .load(listaUsuarios.data[position].post.pics[2])
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenCOpc3)
        }
        //Si se cargan imagenes desde BD interna
        else {
            holder.imagenAOpc3.tag = constantes.OFF +" "+listaUsuarios.data[position].uid+"0"
            holder.imagenBOpc3.tag = constantes.OFF +" "+listaUsuarios.data[position].uid+"1"
            holder.imagenCOpc3.tag = constantes.OFF +" "+listaUsuarios.data[position].uid+"2"
            val profilePicB: Blob? =
                listaImagenes.getBlob(listaUsuarios.data[position].uid)
            //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
            if(profilePicB != null) {
                val profilePic = profilePicB.content
                var profilePicBitMap = BitmapFactory.decodeByteArray(
                    profilePic,
                    0,
                    profilePic!!.size
                )
                holder.imagenHeaderOpc3.setImageBitmap(profilePicBitMap)
            }
            //Si la imagen no existe, se coloca la imagen por defecto
            else{
                holder.imagenHeaderOpc3.setImageResource(R.drawable.imagen_vacia)
            }
            for (index in 0..listaUsuarios.data[position].post.pics.size.minus(1)) {
                val pic: Blob? =
                    listaImagenes.getBlob(listaUsuarios.data[position].uid + index.toString())
                //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
                if(pic != null) {
                    val picByteArray = pic.content
                    var bitMap = BitmapFactory.decodeByteArray(
                        picByteArray,
                        0,
                        picByteArray!!.size
                    )
                    when (index) {
                        0 -> holder.imagenAOpc3.setImageBitmap(bitMap)
                        1 -> holder.imagenBOpc3.setImageBitmap(bitMap)
                        2 -> holder.imagenCOpc3.setImageBitmap(bitMap)
                    }
                }
                //Si la imagen no existe, se coloca la imagen por defecto
                else{
                    when (index) {
                        0 -> holder.imagenAOpc3.setImageResource(R.drawable.imagen_vacia)
                        1 -> holder.imagenBOpc3.setImageResource(R.drawable.imagen_vacia)
                        2 -> holder.imagenCOpc3.setImageResource(R.drawable.imagen_vacia)
                    }
                }
            }
        }
        holder.imagenAOpc3.setOnClickListener(activity)
        holder.imagenBOpc3.setOnClickListener(activity)
        holder.imagenCOpc3.setOnClickListener(activity)
    }

    fun cargarOpcion4(fechaCreacion: String, holder: Opcion4ViewHolder, position: Int) {
        holder.nombreUsuarioOpc4.text = listaUsuarios.data[position].name
        holder.emailUsuarioOpc4.text = listaUsuarios.data[position].email
        holder.fechaPostOpc4.text = fechaCreacion
        holder.recyclerViewOpc4.layoutManager = LinearLayoutManager(
            activity.applicationContext,
            RecyclerView.HORIZONTAL,
            false
        )
        //Si se cargan datos desde el webservice
        if (listaImagenes == null) {
            holder.imagenOpc4.tag = constantes.ON+" "+listaUsuarios.data[position].post.pics[0]
            Picasso
                .get()
                .load(listaUsuarios.data[position].profilePic)
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenHeaderOpc4)
            Picasso
                .get()
                .load(listaUsuarios.data[position].post.pics[0])
                .placeholder(R.drawable.imagen_vacia)
                .error(R.drawable.imagen_vacia)
                .into(holder.imagenOpc4)

            //Se saca la imagen ya puesta anteriormente
            var pics = listaUsuarios.data[position].post.pics.slice(
                1..listaUsuarios.data[position].post.pics.lastIndex
            )
            holder.recyclerViewOpc4.adapter =
                RecyclerViewImagenesHAdapter(
                    activity, pics, null,
                    listaUsuarios.data[position].uid, constantes
                )
        }
        //Si se cargan imagenes desde BD interna
        else {
            val profilePicB: Blob? =
                listaImagenes.getBlob(listaUsuarios.data[position].uid)
            //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
            if(profilePicB!=null) {
                val profilePic = profilePicB.content
                var profilePicBitMap = BitmapFactory.decodeByteArray(
                    profilePic,
                    0,
                    profilePic!!.size
                )
                holder.imagenHeaderOpc4.setImageBitmap(profilePicBitMap)
            }
            //Si la imagen no existe, se coloca la imagen por defecto
            else{
                holder.imagenHeaderOpc4.setImageResource(R.drawable.imagen_vacia)
            }
            holder.imagenOpc4.tag = constantes.OFF+" "+listaUsuarios.data[position].uid+"0"
            var imagenesOffline = mutableListOf<Bitmap?>()
            for (index in 0..listaUsuarios.data[position].post.pics.size.minus(1)) {
                val pic: Blob? =
                    listaImagenes.getBlob(listaUsuarios.data[position].uid + index.toString())
                //Si la imagen existe, es porque se alcanzó a descargar antes de perder el internet
                if(pic != null) {
                    val picByteArray = pic.content
                    var bitMap = BitmapFactory.decodeByteArray(
                        picByteArray,
                        0,
                        picByteArray!!.size
                    )
                    when (index) {
                        0 -> holder.imagenOpc4.setImageBitmap(bitMap)
                        else -> {
                            imagenesOffline.add(bitMap)
                        }
                    }
                }
                //Si la imagen no existe, se coloca la imagen por defecto
                else{
                    when (index) {
                        0 -> holder.imagenOpc4.setImageResource(R.drawable.imagen_vacia)
                        else -> {
                            imagenesOffline.add(null)
                        }
                    }
                }
            }
            holder.recyclerViewOpc4.adapter =
                RecyclerViewImagenesHAdapter(
                    activity,
                    null,
                    imagenesOffline,
                    listaUsuarios.data[position].uid,
                    constantes
                )
        }
        holder.imagenOpc4.setOnClickListener(activity)
    }
    //Se definen los sufijos para los numeros ordinales
    fun definirSufijo(dia: Int): String {
        return if (dia in 11..13) {
            "th"
        } else when (dia % 10) {
            1 -> "st"
            2 -> "nd"
            3 -> "rd"
            else -> "th"
        }
    }
    //Clase envolvente para los distintos layouts necesarios
    abstract class BaseViewHolder(v: View) : RecyclerView.ViewHolder(v) {

    }
    //Viewholder cuando se tiene 1 imagen
    class Opcion1ViewHolder(v: View) : BaseViewHolder(v) {
        val imagenHeaderOpc1 = v.findViewById<ImageView>(R.id.imagenHeaderOpcion1)
        val nombreUsuarioOpc1 = v.findViewById<TextView>(R.id.nombreUsuarioOpcion1)
        val emailUsuarioOpc1 = v.findViewById<TextView>(R.id.emailUsuarioOpcion1)
        val fechaPostOpc1 = v.findViewById<TextView>(R.id.fechaPostOpcion1)
        val imagenOpc1 = v.findViewById<ImageView>(R.id.imagenOpcion1)
    }
    //Viewholder cuando se tiene 2 imagenes
    class Opcion2ViewHolder(v: View) : BaseViewHolder(v) {
        val imagenHeaderOpc2 = v.findViewById<ImageView>(R.id.imagenHeaderOpcion2)
        val nombreUsuarioOpc2 = v.findViewById<TextView>(R.id.nombreUsuarioOpcion2)
        val emailUsuarioOpc2 = v.findViewById<TextView>(R.id.emailUsuarioOpcion2)
        val fechaPostOpc2 = v.findViewById<TextView>(R.id.fechaPostOpcion2)
        val imagenAOpc2 = v.findViewById<ImageView>(R.id.imagenAOpcion2)
        val imagenBOpc2 = v.findViewById<ImageView>(R.id.imagenBOpcion2)
    }
    //Viewholder cuando se tiene 3 imagenes
    class Opcion3ViewHolder(v: View) : BaseViewHolder(v) {
        val imagenHeaderOpc3 = v.findViewById<ImageView>(R.id.imagenHeaderOpcion3)
        val nombreUsuarioOpc3 = v.findViewById<TextView>(R.id.nombreUsuarioOpcion3)
        val emailUsuarioOpc3 = v.findViewById<TextView>(R.id.emailUsuarioOpcion3)
        val fechaPostOpc3 = v.findViewById<TextView>(R.id.fechaPostOpcion3)
        val imagenAOpc3 = v.findViewById<ImageView>(R.id.imagenAOpcion3)
        val imagenBOpc3 = v.findViewById<ImageView>(R.id.imagenBOpcion3)
        val imagenCOpc3 = v.findViewById<ImageView>(R.id.imagenCOpcion3)
    }
    //Viewholder cuando se tiene 4 imagenes o más
    class Opcion4ViewHolder(v: View) : BaseViewHolder(v) {
        val imagenHeaderOpc4 = v.findViewById<ImageView>(R.id.imagenHeaderOpcion4)
        val nombreUsuarioOpc4 = v.findViewById<TextView>(R.id.nombreUsuarioOpcion4)
        val emailUsuarioOpc4 = v.findViewById<TextView>(R.id.emailUsuarioOpcion4)
        val fechaPostOpc4 = v.findViewById<TextView>(R.id.fechaPostOpcion4)
        val imagenOpc4 = v.findViewById<ImageView>(R.id.imagenOpcion4)
        val recyclerViewOpc4 = v.findViewById<RecyclerView>(R.id.recyclerViewRVCOpc4)
    }
}