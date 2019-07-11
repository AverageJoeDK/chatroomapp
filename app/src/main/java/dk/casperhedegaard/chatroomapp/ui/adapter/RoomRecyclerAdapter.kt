package dk.casperhedegaard.chatroomapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import dk.casperhedegaard.chatroomapp.R
import dk.casperhedegaard.chatroomapp.databinding.RoomListItemBinding
import dk.casperhedegaard.chatroomapp.model.Room
import dk.casperhedegaard.chatroomapp.viewmodel.MainViewModel
import timber.log.Timber

class RoomRecyclerAdapter(var roomList: ArrayList<Room>, val vm: MainViewModel) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = DataBindingUtil.inflate<RoomListItemBinding>(LayoutInflater.from(parent.context), R.layout.room_list_item, parent, false)
        return RoomViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val room = roomList[position]
        if(holder is RoomViewHolder) {
            holder.bind(room)
        }
    }

    inner class RoomViewHolder(private val binding: RoomListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(room: Room) {
            binding.room = room
            binding.vm = vm
        }
    }
}