package dk.casperhedegaard.chatroomapp.util

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.storage.FirebaseStorage

class FirebaseUtils {

    private val firestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()

    val mAuth = FirebaseAuth.getInstance()
    val mDb = FirebaseFirestore.getInstance()
    val mStorage = FirebaseStorage.getInstance()

    private object Holder {
        val INSTANCE = FirebaseUtils()
    }

    companion object {
        val instance: FirebaseUtils by lazy { Holder.INSTANCE }
    }

    init {
        mDb.firestoreSettings = firestoreSettings
    }
}