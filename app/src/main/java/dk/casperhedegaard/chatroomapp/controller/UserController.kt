package dk.casperhedegaard.chatroomapp.controller

import dk.casperhedegaard.chatroomapp.util.FirebaseUtils
import dk.casperhedegaard.chatroomapp.util.Globals
import timber.log.Timber

object UserController {

    private val mdb get() = FirebaseUtils.instance.mDb

    private val mauth get() = FirebaseUtils.instance.mAuth

    fun userLogin(userId: String) {
        mdb.collection(Globals.FIREBASE_USER_COLLECTION).document(userId).get().addOnCompleteListener { _ ->
            mauth.currentUser?.let { user ->
                val data = mutableMapOf<String, Any>()
                data["name"] = user.displayName!!
                data["email"] = user.email!!
                user.photoUrl?.let { photo ->
                    data["photo"] = photo.toString()
                }
                mdb.collection(Globals.FIREBASE_USER_COLLECTION).document(userId).set(data)
            }
        }
    }

    fun getUserInfoForUserId(userId: String, completion:(name: Pair<String, String>) -> Unit) {
        var result = Pair("", "")
        mdb.collection(Globals.FIREBASE_USER_COLLECTION).document(userId).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                task.result?.let {
                    result = Pair(it["name"] as String, it["photo"] as String)
                }
            }
            completion(result)
        }
    }
}