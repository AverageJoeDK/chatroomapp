package dk.casperhedegaard.chatroomapp.ui.fragment


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.firebase.ui.auth.AuthUI
import dk.casperhedegaard.chatroomapp.R
import dk.casperhedegaard.chatroomapp.util.FirebaseUtils
import dk.casperhedegaard.chatroomapp.util.Globals
import dk.casperhedegaard.chatroomapp.viewmodel.MainViewModel

class LoginFragment : ExtendFragment() {

    private lateinit var vm: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        vm = ViewModelProviders.of(requireActivity()).get(MainViewModel::class.java)

        val providers = arrayListOf(
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build())

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setIsSmartLockEnabled(false)
                .build(),
            Globals.RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(FirebaseUtils.instance.mAuth.currentUser == null) {
            vm.showDialog(
                "Login Failed",
                "We could not log you in. Please try again.",
            "OK",
                "Cancel",
                null, null
            )
        }
    }
}
