package net.k1ra.succubotapp.features.robotStatus.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.features.base.model.BaseRetrofitCallback
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.LoadingStatus
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.robotStatus.model.RobotCommand
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import net.k1ra.succubotapp.features.robotStatus.repository.RobotStatusRetrofitInterface
import net.k1ra.succubotapp.features.authentication.model.AuthenticationSettings
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import javax.inject.Inject

@SuppressLint("StaticFieldLeak") //It's an applicationContext, stop complaining
@HiltViewModel
class RobotStatusViewModel @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val appSettings: AuthenticationSettings,
    retrofit: Retrofit,
    gson: Gson,
    state: SavedStateHandle,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val client = retrofit.create(RobotStatusRetrofitInterface::class.java)

    private val robotId = gson.fromJson(state.get<String>("robotStatus"), RobotStatus::class.java).did

    val robotStatus = MutableLiveData<LoadingStatus<RobotStatus>>(Success(gson.fromJson(state.get<String>("robotStatus"), RobotStatus::class.java)))
    val mapObservable = MutableLiveData<LoadingStatus<ByteArray>>(Loading(""))
    val pathObservable = MutableLiveData<LoadingStatus<ByteArray>>(Loading(""))
    val robotCommandStatus = MutableLiveData<LoadingStatus<Unit>>()

    fun fetchMap() = CoroutineScope(Dispatchers.IO).launch {
        try {
            Handler(Looper.getMainLooper()).post {
                mapObservable.value = Loading("")
            }

            Log.d("RobotMapViewModel", "Fetching map")

            val request = Request.Builder()
                .url("${appSettings.baseUrl}layout/$robotId")
                .method("GET", null)
                .build()

            val resp = okHttpClient.newCall(request).execute()

            if (resp.code() == 200) {
                val dat = resp.body()!!.bytes()

                Handler(Looper.getMainLooper()).post {
                    mapObservable.value = Success(dat)
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    mapObservable.value = Failure(resp.code())
                }
            }

            resp.close()
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                mapObservable.value = Failure(0, e.message)
            }
        }
    }

    fun fetchPath() = CoroutineScope(Dispatchers.IO).launch {
        try {
            Handler(Looper.getMainLooper()).post {
                pathObservable.value = Loading("")
            }

            val request = Request.Builder()
                .url("${appSettings.baseUrl}route/$robotId")
                .method("GET", null)
                .build()

            val resp = okHttpClient.newCall(request).execute()

            if (resp.code() == 200) {
                val dat = resp.body()!!.bytes()

                Handler(Looper.getMainLooper()).post {
                    pathObservable.value = Success(dat)
                }
            } else {
                Handler(Looper.getMainLooper()).post {
                    pathObservable.value = Failure(resp.code())
                }
            }

            resp.close()
        } catch (e: Exception) {
            Handler(Looper.getMainLooper()).post {
                pathObservable.value = Failure(0, e.message)
            }
        }
    }

    fun getRobotStatus() = CoroutineScope(Dispatchers.IO).launch {
        client.getRobotStatus(robotId).enqueue(BaseRetrofitCallback(robotStatus::setValue))
    }

    fun sendRobotCommand(command: RobotCommand) = CoroutineScope(Dispatchers.IO).launch {
        Handler(Looper.getMainLooper()).post {
            robotCommandStatus.value = Loading(context.getString(R.string.sending_command))
        }

        client.sendRobotCommand(robotId, command).enqueue(BaseRetrofitCallback(robotCommandStatus::setValue) {
           CoroutineScope(Dispatchers.IO).launch {
               delay(3000)
               getRobotStatus()
           }
        })
    }
}