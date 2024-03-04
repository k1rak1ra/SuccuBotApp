package net.k1ra.succubotapp.features.base.model

import android.os.Handler
import android.os.Looper
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BaseRetrofitCallback<T>(
    private val observableSetter: (LoadingStatus<T>) -> Unit,
    private val additionalOnSuccess: ((T) -> Unit)? = null
) : Callback<T> {

    override fun onResponse(call: Call<T>, response: Response<T>) {
        if (response.code() == 200) {
            Handler(Looper.getMainLooper()).post {
                additionalOnSuccess?.invoke(response.body()!!)
                observableSetter.invoke(Success(response.body()!!))
            }
        } else {
            Handler(Looper.getMainLooper()).post {
                var reason: String? = null

                try {
                    reason = response.errorBody()?.string()?.let { JSONObject(it).getString("reason") }
                } catch (e: Exception) { /* Failed to parse error reason, maybe it's missing. Continue */}

                observableSetter.invoke(Failure(response.code(), reason))
            }
        }
    }

    override fun onFailure(call: Call<T>, t: Throwable) {
        Handler(Looper.getMainLooper()).post {
            observableSetter.invoke(Failure(0, t.message))
        }
    }
}