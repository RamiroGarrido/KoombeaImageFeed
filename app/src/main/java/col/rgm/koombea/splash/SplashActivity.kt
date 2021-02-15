package col.rgm.koombea.splash

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import col.rgm.koombea.R
import col.rgm.koombea.main.MainActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        this.supportActionBar?.hide()
        iniciarAnimacion()
    }

    fun iniciarAnimacion(){
        val contenedor = findViewById<LinearLayout>(R.id.splash_container)
        val animation = AnimationUtils.loadAnimation(this,
            R.anim.slide_up
        )
        animation.setAnimationListener(object : Animation.AnimationListener{
            override fun onAnimationRepeat(animation: Animation?) {

            }

            override fun onAnimationStart(animation: Animation?) {

            }
            //Se inicia la clase principal al terminar la animación
            override fun onAnimationEnd(animation: Animation?) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
            }
        })
        contenedor.startAnimation(animation)
    }
}