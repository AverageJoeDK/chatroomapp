package dk.casperhedegaard.chatroomapp.ui.fragment


import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager

import dk.casperhedegaard.chatroomapp.R
import dk.casperhedegaard.chatroomapp.controller.RoomController
import dk.casperhedegaard.chatroomapp.ui.adapter.RoomRecyclerAdapter
import dk.casperhedegaard.chatroomapp.util.FirebaseUtils
import dk.casperhedegaard.chatroomapp.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_room.*
import kotlinx.android.synthetic.main.new_room_dialog.view.*
import timber.log.Timber
import java.util.*

class RoomFragment : ExtendFragment() {

    private lateinit var vm: MainViewModel

    private lateinit var adapter: RoomRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        fragment_room_recyclerview.layoutManager = LinearLayoutManager(requireContext())
        adapter = RoomRecyclerAdapter(vm.rooms, vm)
        fragment_room_recyclerview.adapter = adapter

        fragment_room_swipe_refresh.setOnRefreshListener {
            vm.loadRooms {
                fragment_room_swipe_refresh.setRefreshing(false)
                updateRoomList()
            }
        }

        fragment_room_add_room_fab.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext())
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.new_room_dialog, null)
            dialogBuilder.setTitle("Add New Room")
            dialogBuilder.setView(dialogView)
            dialogBuilder.setNegativeButton("Cancel") { d, _ ->
                d.dismiss()
            }
            dialogBuilder.setPositiveButton("Add") { d, _ ->
                val newName = dialogView.dialog_new_room_name.text.toString()
                vm.addRoom(newName) {
                    vm.loadRooms {
                        updateRoomList()
                        d.dismiss()
                    }
                }
            }
            val dialog = dialogBuilder.create()
            dialog.show()
        }

        fragment_room_signout_button.setOnClickListener {
            FirebaseUtils.instance.mAuth.signOut()
        }
    }

    private fun updateRoomList() {
        adapter.roomList = vm.rooms
        adapter.notifyDataSetChanged()
    }
}
