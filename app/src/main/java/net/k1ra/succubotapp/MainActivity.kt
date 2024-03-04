package net.k1ra.succubotapp

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.telephony.PhoneStateListener
import androidx.navigation.findNavController
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.di.EncryptedSharedPrefProvider
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.authentication.ui.LoginFragment
import net.k1ra.succubotapp.features.base.ui.ErrorOverlay
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject lateinit var sharedpref: EncryptedSharedPrefProvider

    private val logoutReceiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, i: Intent) {
            sharedpref.setStored(Constants.CurrentUserName, null)

            findNavController(R.id.fragmentContainerView).navigate(R.id.loginFragment)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(logoutReceiver, IntentFilter(Constants.LogoutIntent.action), RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(logoutReceiver, IntentFilter(Constants.LogoutIntent.action))
        }
    }
}