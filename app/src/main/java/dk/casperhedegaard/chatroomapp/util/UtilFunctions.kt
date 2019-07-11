package dk.casperhedegaard.chatroomapp.util

import android.app.Activity
import android.graphics.Outline
import android.net.Uri
import android.text.format.DateUtils
import android.view.View
import android.view.ViewOutlineProvider
import android.view.inputmethod.InputMethodManager
import com.google.firebase.Timestamp


object UtilFunctions {

    private val storage = FirebaseUtils.instance.mStorage

    fun roundView(view: View) {
        view.clipToOutline = true // Enables clipping on the view. Required for outline to work.

        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.let {
                    val radius = view.height / 2.toFloat()
                    it.setRoundRect(0, 0, view.width, view.height, radius)
                }
            }
        }
    }

    fun roundView(view: View, cornerRadius: Int) {
        view.clipToOutline = true // Enables clipping on the view. Required for outline to work.

        val radius = cornerRadius.toFloat()

        view.outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline?) {
                outline?.let {
                    it.setRoundRect(0, 0, view.width, view.height, radius)
                }
            }
        }
    }

    fun getTimeAgo(timestamp: Timestamp?): String {
        return DateUtils.getRelativeTimeSpanString(timestamp?.toDate()?.time ?: 0).toString()
    }

    fun uploadImageToStorage(uri: Uri, path: String, completion: (success: Boolean, photoUrl: String) -> Unit) {
        val uploadPath = storage.reference.child(path)
        uploadPath.child("posted_image_${Timestamp.now().toDate().time}").putFile(uri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                task.result?.let { result ->
                    val url = "${Globals.FIREBASE_STORAGE_URL}${result.metadata?.path}"
                    completion(true, url)
                }
            } else {
                completion(false, "Upload failed")
            }
        }
    }

    fun hideSoftkeyboard(activity: Activity) {
        val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        //Find the currently focused view, so we can grab the correct window token from it.
        var view = activity.currentFocus
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(activity)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}