package dk.casperhedegaard.chatroomapp.ui.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment

open class ExtendFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.isClickable = true
    }
}