package dk.casperhedegaard.chatroomapp.viewmodel

import android.app.Application
import android.content.DialogInterface
import android.view.View
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import dk.casperhedegaard.chatroomapp.controller.MessageController
import dk.casperhedegaard.chatroomapp.controller.RoomController
import dk.casperhedegaard.chatroomapp.model.Message
import dk.casperhedegaard.chatroomapp.model.Room
import dk.casperhedegaard.chatroomapp.util.Globals
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class MainViewModel(app: Application) : AndroidViewModel(app) {

    var appStatus: MutableLiveData<String> = MutableLiveData()

    var appStatusMessage: String = ""

    var showDialogTitle: String = ""

    var yesButton: String = ""

    var noButton: String = ""

    var positiveClick: DialogInterface.OnClickListener? = null

    var negativeClick: DialogInterface.OnClickListener? = null

    var currentUserId: String = ""

    var rooms: ArrayList<Room> = ArrayList()

    var room: Room? = null

    var messageList = MutableLiveData<ArrayList<Message>>()

    val userMap = mutableMapOf<String, Pair<String, String>>()

    fun loadRooms(completion:() -> Unit) {
        RoomController.getRooms {
            rooms = it
            completion()
        }
    }

    fun addRoom(name: String, description: String, completion:(success: Boolean) -> Unit) {
        RoomController.addRoom(name, description) {
            completion(it)
        }
    }

    fun clickRoom(view: View, room: Room) {
        this.room = room
        messageList.postValue(ArrayList())
        appStatus.postValue(Globals.APP_STATUS_CHAT)
    }

    fun getInitialMessages(completion: () -> Unit) {
        room?.let { r ->
            MessageController.getMessagesForRoom(r.id) {
                messageList.postValue(it)
                completion()
            }
        }
    }

    fun getMoreMessages(completion: () -> Unit) {
        room?.let { r ->
            MessageController.fetchMoreMessages(r.id) {
                if (it.isNotEmpty()) {
                    val tmpList = ArrayList<Message>()
                    tmpList.addAll(messageList.value ?: emptyList())
                    tmpList.addAll(it)
                    messageList.postValue(tmpList)
                }
                completion()
            }
        }
    }

    fun showDialog(title: String, message: String, yesButton: String, noButton: String, positiveClick: DialogInterface.OnClickListener?, negativeClick: DialogInterface.OnClickListener?) {
        appStatusMessage = message
        showDialogTitle = title
        this.yesButton = yesButton
        this.noButton = noButton
        this.positiveClick = positiveClick
        this.negativeClick = negativeClick
        appStatus.postValue(Globals.APP_STATUS_DIALOG_ERROR)
    }
}