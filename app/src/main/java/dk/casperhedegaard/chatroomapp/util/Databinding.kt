package dk.casperhedegaard.chatroomapp.util

import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide

@BindingAdapter("roundImage")
fun AppCompatImageView.roundImage(round: Boolean) {
    if(round) {
        UtilFunctions.roundView(this)
    }
}

@BindingAdapter("cornerRadiusPX")
fun View.cornerRadiusPX(px: Int) {
    UtilFunctions.roundView(this, px)
}

@BindingAdapter("glideImage")
fun AppCompatImageView.glideImage(src: String?) {
    Glide.with(this).load(src).into(this)
}