package dk.casperhedegaard.chatroomapp.ui.activity

import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import dk.casperhedegaard.chatroomapp.BuildConfig
import dk.casperhedegaard.chatroomapp.R
import dk.casperhedegaard.chatroomapp.controller.UserController
import dk.casperhedegaard.chatroomapp.ui.fragment.ChatFragment
import dk.casperhedegaard.chatroomapp.ui.fragment.ExtendFragment
import dk.casperhedegaard.chatroomapp.ui.fragment.LoginFragment
import dk.casperhedegaard.chatroomapp.ui.fragment.RoomFragment
import dk.casperhedegaard.chatroomapp.util.FirebaseUtils
import dk.casperhedegaard.chatroomapp.util.Globals
import dk.casperhedegaard.chatroomapp.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber

class MainActivity : AppCompatActivity() {

    private lateinit var vm: MainViewModel

    private val mAuth = FirebaseUtils.instance.mAuth

    private val fragmentMap = mutableMapOf<String, ExtendFragment>()

    private val loginListener = FirebaseAuth.AuthStateListener {
        it.currentUser?.let { user ->
            vm.appStatus.postValue(Globals.APP_STATUS_LOADING)
            vm.currentUserId = user.uid
            UserController.userLogin(user.uid)
            login()
        } ?: run {
            if(fragmentMap["LoginFragment"] == null) {
                fragmentMap["LoginFragment"] = LoginFragment()
            }
            vm.appStatus.postValue(Globals.APP_STATUS_LOADING_COMPLETE)
            changeFragment(fragmentMap["LoginFragment"]!!, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (BuildConfig.DEBUG && Timber.treeCount() == 0) {
            Timber.plant(object : Timber.DebugTree() {
                override fun createStackElementTag(element: StackTraceElement): String? {
                    return "Timber:" + super.createStackElementTag(element) + ":" + element.lineNumber
                }
            })
        }

        mAuth.addAuthStateListener(loginListener)

        vm = ViewModelProviders.of(this).get(MainViewModel::class.java)

        vm.appStatus.observeForever { status ->
            when(status) {
                Globals.APP_STATUS_LOADING -> {
                    activity_main_loading_container.visibility = View.VISIBLE
                }
                Globals.APP_STATUS_LOADING_COMPLETE -> activity_main_loading_container.visibility = View.GONE
                Globals.APP_STATUS_CHAT -> {
                    if(fragmentMap["ChatFragment"] == null) {
                        fragmentMap["ChatFragment"] = ChatFragment()
                    }
                    changeFragment(fragmentMap["ChatFragment"]!!)
                }
                Globals.APP_STATUS_TOAST_ERROR -> Toast.makeText(this, vm.appStatusMessage, Toast.LENGTH_SHORT).show()
                Globals.APP_STATUS_DIALOG_ERROR -> showDialog(vm.showDialogTitle, vm.appStatusMessage, vm.yesButton, vm.noButton, vm.positiveClick, vm.negativeClick)
            }
        }
    }

    private fun changeFragment(fragment: ExtendFragment, replace: Boolean = false) {
        if(replace) {
            supportFragmentManager.beginTransaction().replace(R.id.activity_main_fragment_container, fragment).commitNowAllowingStateLoss()
        } else {
            supportFragmentManager.beginTransaction().add(R.id.activity_main_fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss()
        }
    }

    private fun login() {
        vm.loadRooms {
            if(fragmentMap["RoomFragment"] == null) {
                fragmentMap["RoomFragment"] = RoomFragment()
            }
            changeFragment(fragmentMap["RoomFragment"]!!, true)
            vm.appStatus.postValue(Globals.APP_STATUS_LOADING_COMPLETE)
        }
    }

    private fun showDialog(title: String, message: String, yesButton: String, noButton: String, positiveClick: DialogInterface.OnClickListener?, negativeClick: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(message)
        if(positiveClick == null) {
            builder.setPositiveButton(R.string.ok) { d, _ ->
                d.dismiss()
            }
        } else {
            builder.setPositiveButton(yesButton, positiveClick)
        }

        if(negativeClick != null) {
            builder.setNegativeButton(noButton, negativeClick)
        }

        builder.create().show()
    }
}
