package dk.casperhedegaard.chatroomapp.ui.adapter

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dk.casperhedegaard.chatroomapp.R
import dk.casperhedegaard.chatroomapp.controller.UserController
import dk.casperhedegaard.chatroomapp.databinding.ChatMessageItemBinding
import dk.casperhedegaard.chatroomapp.model.Message
import dk.casperhedegaard.chatroomapp.util.UtilFunctions
import dk.casperhedegaard.chatroomapp.util.glideImage
import dk.casperhedegaard.chatroomapp.viewmodel.MainViewModel

class ChatRecyclerAdapter(val vm: MainViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val list = ArrayList<Message>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<ChatMessageItemBinding>(LayoutInflater.from(parent.context), R.layout.chat_message_item, parent, false)
        return MessageViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position]
        if(holder is MessageViewHolder) {
            holder.bind(message)
        }
    }

    fun addItem(message: Message, pos: Int) {
        list.add(pos, message)
    }

    inner class MessageViewHolder(private val binding: ChatMessageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            val timestamp = UtilFunctions.getTimeAgo(message.timestamp)
            if(vm.userMap[message.authorId!!] == null) {
                UserController.getUserInfoForUserId(message.authorId!!) {
                    vm.userMap[message.authorId!!] = it
                    binding.chatMessageItemInfoName.text = it.first
                    binding.chatMessageItemProfileOwn.glideImage(it.second)
                    binding.chatMessageItemProfileOther.glideImage(it.second)
                }
            } else {
                val curUser = vm.userMap[message.authorId!!]!!
                binding.chatMessageItemInfoName.text = curUser.first
                binding.chatMessageItemProfileOwn.glideImage(curUser.second)
                binding.chatMessageItemProfileOther.glideImage(curUser.second)
            }
            binding.message = message
            if(message.authorId == vm.currentUserId) {
                binding.chatMessageItemContainer.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.backgroundMessageOwn))
                binding.chatMessageItemTop.gravity = Gravity.END
                binding.chatMessageItemInfoTimestamp.textAlignment = View.TEXT_ALIGNMENT_TEXT_END
                binding.chatMessageItemProfileOther.visibility = View.GONE
                binding.chatMessageItemProfileOwn.visibility = View.VISIBLE
            } else {
                binding.chatMessageItemContainer.setBackgroundColor(ContextCompat.getColor(binding.root.context, R.color.backgroundMessageOther))
                binding.chatMessageItemTop.gravity = Gravity.START
                binding.chatMessageItemInfoTimestamp.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                binding.chatMessageItemProfileOther.visibility = View.VISIBLE
                binding.chatMessageItemProfileOwn.visibility = View.GONE
            }
            binding.chatMessageItemInfoTimestamp.text = timestamp

            if(message.message.isNullOrBlank()) {
                binding.chatMessageItemMessage.visibility = View.GONE
            } else {
                binding.chatMessageItemMessage.visibility = View.VISIBLE
            }

            if(message.imageurl == null) {
                binding.chatMessageItemImage.visibility = View.GONE
            } else {
                binding.chatMessageItemImage.visibility = View.VISIBLE
            }
        }
    }
}