package dk.casperhedegaard.chatroomapp.controller

import android.net.Uri
import com.google.firebase.firestore.*
import dk.casperhedegaard.chatroomapp.model.Message
import dk.casperhedegaard.chatroomapp.util.FirebaseUtils
import dk.casperhedegaard.chatroomapp.util.Globals
import dk.casperhedegaard.chatroomapp.util.UtilFunctions
import timber.log.Timber

object MessageController {

    private val mdb: FirebaseFirestore get() = FirebaseUtils.instance.mDb

    private var messageListener: ListenerRegistration? = null

    private var lastDocumentFetched: QueryDocumentSnapshot? = null

    private var listenerAssignLastFetched = true

    fun saveMessage(room: String, message: String, authorId: String, imageUri: Uri?, completion:(success: Boolean, error: String?) -> Unit) {
        val data = mutableMapOf<String, Any>()
        val ref = mdb.collection(Globals.FIREBASE_ROOM_COLLECTION).document(room).collection(Globals.FIREBASE_MESSAGE_COLLECTION).document()
        data["authorId"] = authorId
        data["timestamp"] = FieldValue.serverTimestamp()
        data["message"] = message
        imageUri?.let {
            UtilFunctions.uploadImageToStorage(imageUri, ref.path) { storageSuccess, imageData ->
                if(storageSuccess) {
                    val imageurl = imageData.replace("http://", "https://")
                    data["imageurl"] = imageurl
                    uploadMessage(data, ref) { success, error ->
                        completion(success, error)
                    }
                } else {
                    completion(false, "Couldn't upload your image")
                }
            }
        } ?: run {
            uploadMessage(data, ref) { success, error ->
                completion(success, error)
            }
        }
    }

    private fun uploadMessage(data: Map<String, Any>, ref: DocumentReference, completion:(success: Boolean, error: String?) -> Unit) {
        ref.set(data).addOnCompleteListener { task ->
            completion(task.isSuccessful, if(task.isSuccessful) null else "Failed to upload your message. Please try again.")
        }
    }

    fun getMessagesForRoom(room: String, completion:(list: ArrayList<Message>) -> Unit) {
        val msgList = ArrayList<Message>()
        messageListener = mdb.collection(Globals.FIREBASE_ROOM_COLLECTION).document(room).collection(Globals.FIREBASE_MESSAGE_COLLECTION).orderBy("timestamp", Query.Direction.DESCENDING).limit(50).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            querySnapshot?.let { snapshots ->
                snapshots.documentChanges.forEach { change ->
                    val msg = change.document.toObject(Message::class.java)
                    Timber.d("$msg")
                    if(listenerAssignLastFetched) {
                        lastDocumentFetched = change.document
                    }
                    when(change.type) {
                        DocumentChange.Type.ADDED -> {
                            if(!change.document.metadata.hasPendingWrites()) {
                                if(!listenerAssignLastFetched) {
                                    msgList.add(0, msg)
                                } else {
                                    msgList.add(msg)
                                }
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            if(!listenerAssignLastFetched) {
                                msgList.add(0, msg)
                            } else {
                                msgList.add(msg)
                            }
                        }
                        else -> {

                        }
                    }
                }
                listenerAssignLastFetched = false
            }
            completion(msgList)
        }
    }

    fun fetchMoreMessages(room: String, completion:(list: ArrayList<Message>) -> Unit) {
        val msgList = ArrayList<Message>()
        lastDocumentFetched?.let {
            mdb.collection(Globals.FIREBASE_ROOM_COLLECTION).document(room).collection(Globals.FIREBASE_MESSAGE_COLLECTION).orderBy("timestamp", Query.Direction.DESCENDING).startAfter(it).limit(20).get().addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    task.result?.let { result ->
                        result.forEach { res ->
                            val msg = res.toObject(Message::class.java)
                            msgList.add(msg)
                            lastDocumentFetched = res
                        }
                    }
                }
                completion(msgList)
            }
        } ?: run {
            completion(msgList)
        }
    }

    fun removeListener() {
        messageListener?.remove()
        listenerAssignLastFetched = true
        lastDocumentFetched = null
    }
}