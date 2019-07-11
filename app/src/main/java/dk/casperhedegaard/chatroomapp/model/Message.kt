package dk.casperhedegaard.chatroomapp.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

data class Message(
    @get:PropertyName("message")
    @set:PropertyName("message")
    var message: String? = null,

    @get:PropertyName("authorId")
    @set:PropertyName("authorId")
    var authorId: String? = null,

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    @ServerTimestamp
    var timestamp: Timestamp? = null,

    @get:PropertyName("imageurl")
    @set:PropertyName("imageurl")
    var imageurl: String? = null
)