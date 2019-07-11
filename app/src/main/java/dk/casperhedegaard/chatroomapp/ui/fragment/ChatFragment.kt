package dk.casperhedegaard.chatroomapp.ui.fragment


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListUpdateCallback
import androidx.recyclerview.widget.RecyclerView
import com.theartofdev.edmodo.cropper.CropImage
import dk.casperhedegaard.chatroomapp.R
import dk.casperhedegaard.chatroomapp.controller.MessageController
import dk.casperhedegaard.chatroomapp.databinding.FragmentChatBinding
import dk.casperhedegaard.chatroomapp.ui.adapter.ChatRecyclerAdapter
import dk.casperhedegaard.chatroomapp.util.Globals
import dk.casperhedegaard.chatroomapp.util.UtilFunctions
import dk.casperhedegaard.chatroomapp.viewmodel.MainViewModel
import dk.casperhedegaard.chatroomapp.model.MessageDiffCallback
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_chat.view.*

class ChatFragment : ExtendFragment() {

    private lateinit var vm: MainViewModel

    private lateinit var adapter: ChatRecyclerAdapter

    private lateinit var recycler: RecyclerView

    private var lastScrollUp = false

    private val updateCallback = object : ListUpdateCallback {

        override fun onChanged(position: Int, count: Int, payload: Any?) {
//            Timber.d("You should change here: $position. Count: $count")
        }

        override fun onMoved(fromPosition: Int, toPosition: Int) {
//            Timber.d("You should move here $fromPosition - $toPosition")
        }

        override fun onInserted(position: Int, count: Int) {
//            Timber.d("Message inserted. pos: $position - count: $count")
            vm.messageList.value?.let { messageList ->
                for (i in position until position + count) {
                    adapter.addItem(messageList[i], i)
                }
                adapter.notifyItemRangeInserted(position, count)
                if (isRecyclerAtBottom()) {
                    scrollToBottom()
                }
//                Timber.d("at bottom: ${isRecyclerAtBottom()}")
            }
        }

        override fun onRemoved(position: Int, count: Int) {
//            Timber.d("You should remove here: $position. Count: $count")
        }
    }

    private var uriToImage: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        vm = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        val binding = DataBindingUtil.inflate<FragmentChatBinding>(inflater, R.layout.fragment_chat, container, false)
        binding.vm = vm

        adapter = ChatRecyclerAdapter(vm)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recycler = fragment_chat_recyclerview

        recycler.addOnScrollListener(object: RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                when (newState) {
                    RecyclerView.SCROLL_STATE_IDLE -> {
                        if(lastScrollUp) {
                            if((recyclerView.layoutManager as LinearLayoutManager).findLastVisibleItemPosition() == adapter.itemCount - 1) {
                                vm.getMoreMessages {

                                }
                            }
                        }
                    }
                    RecyclerView.SCROLL_STATE_DRAGGING -> {
                    }
                    RecyclerView.SCROLL_STATE_SETTLING -> {
                    }
                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                when {
                    dy > 0 -> {
                        lastScrollUp = false
                    }
                    dy < 0 -> {
                        lastScrollUp = true
                    }
                    else -> {
                    }
                }
            }
        })

        fragment_chat_swipe_refresh.setOnRefreshListener {
            vm.getMoreMessages {
                fragment_chat_swipe_refresh.setRefreshing(false)
                vm.appStatusMessage = "No more messages"
                vm.appStatus.postValue(Globals.APP_STATUS_TOAST_ERROR)
            }
        }

        recycler.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, true)
        recycler.adapter = adapter

        vm.messageList.observe(this, Observer {
            val messageCallback = MessageDiffCallback(adapter.list, it)
            val result = DiffUtil.calculateDiff(messageCallback)
            result.dispatchUpdatesTo(updateCallback)
        })

        vm.getInitialMessages {
            vm.appStatus.postValue(Globals.APP_STATUS_LOADING_COMPLETE)
        }

        fragment_chat_new_message_button.setOnClickListener {
            val msg = fragment_chat_new_message_text.text.toString()
            if(msg.isNotBlank()) {
                UtilFunctions.hideSoftkeyboard(requireActivity())
                MessageController.saveMessage(vm.room!!.id, msg, vm.currentUserId, uriToImage) { success, error ->
                    if(success) {
                        view.fragment_chat_new_message_text.clearFocus()
                        view.fragment_chat_new_message_text.text?.clear()
                        scrollToBottom()
                    }
                }
            }
        }

        fragment_chat_new_message_image_button.setOnClickListener {
            CropImage.startPickImageActivity(requireContext(), this)
        }
    }

    private fun isRecyclerAtBottom() : Boolean {
        val layoutManager = recycler.layoutManager as LinearLayoutManager
        return layoutManager.findFirstVisibleItemPosition() == 0
    }

    private fun scrollToBottom() {
        Handler().postDelayed({
            recycler.smoothScrollToPosition(0)
        }, 500)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                uriToImage = CropImage.getPickImageResultUri(requireContext(), data)
                val msg = fragment_chat_new_message_text.text.toString()
                MessageController.saveMessage(vm.room!!.id, msg, vm.currentUserId, uriToImage) { success, error ->
                    vm.appStatusMessage = "Failed to save message. Try again."
                    vm.appStatus.postValue(Globals.APP_STATUS_TOAST_ERROR)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        MessageController.removeListener()
        vm.messageList.postValue(ArrayList())
    }
}
