package dk.casperhedegaard.chatroomapp.controller

import android.os.AsyncTask
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dk.casperhedegaard.chatroomapp.model.Message
import dk.casperhedegaard.chatroomapp.model.Room
import dk.casperhedegaard.chatroomapp.util.DispatchGroup
import dk.casperhedegaard.chatroomapp.util.FirebaseUtils
import dk.casperhedegaard.chatroomapp.util.Globals
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

object RoomController {

    private val mdb: FirebaseFirestore get() = FirebaseUtils.instance.mDb

    fun getRooms(completion:(list: ArrayList<Room>) -> Unit) {
        val roomList = ArrayList<Room>()
        mdb.collection(Globals.FIREBASE_ROOM_COLLECTION).orderBy("name", Query.Direction.ASCENDING).get().addOnCompleteListener { task ->
            if(task.isSuccessful) {
                task.result?.let { result ->
                    result.forEach { quer ->
                        val tmp = quer.toObject(Room::class.java)
                        tmp.id = quer.id
                        roomList.add(tmp)
                    }
                }
            }
            sortRooms(roomList) {
                completion(it)
            }
        }
    }

    fun addRoom(name: String, completion:(success: Boolean) -> Unit) {
        val data = mutableMapOf<String, Any>()
        data["name"] = name
        mdb.collection(Globals.FIREBASE_ROOM_COLLECTION).add(data).addOnCompleteListener {
            completion(it.isSuccessful)
        }
    }

    private fun sortRooms(list: ArrayList<Room>, completion: (list: ArrayList<Room>) -> Unit) {
        val group = DispatchGroup()
        val tmpMsgList = ArrayList<Pair<Room, Timestamp?>>()
        AsyncTask.execute {
            list.forEach { room ->
                group.enter()
                mdb.collection(Globals.FIREBASE_ROOM_COLLECTION).document(room.id).collection(Globals.FIREBASE_MESSAGE_COLLECTION).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get().addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        task.result?.let { result ->
                            if(result.isEmpty) {
                                tmpMsgList.add(Pair(room, null))
                            }
                            result.forEach { msg ->
                                val tmpMsg = msg.toObject(Message::class.java)
                                tmpMsgList.add(Pair(room, tmpMsg.timestamp))
                            }
                        }
                    }
                    group.leave()
                }
            }
            group.notify {
                tmpMsgList.sortByDescending {
                    it.second
                }
                val returnList = ArrayList<Room>()
                tmpMsgList.forEach {
                    returnList.add(it.first)
                }
                completion(returnList)
            }
        }
    }
}