package dk.casperhedegaard.firebasechat.model

import androidx.recyclerview.widget.DiffUtil
import dk.casperhedegaard.chatroomapp.model.Message

class MessageDiffCallback(val oldList: List<Message>, val newList: List<Message>) : DiffUtil.Callback() {

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].message == newList[newItemPosition].message
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}