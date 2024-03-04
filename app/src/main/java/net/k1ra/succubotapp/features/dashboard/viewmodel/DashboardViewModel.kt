package net.k1ra.succubotapp.features.dashboard.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.features.base.model.BaseRetrofitCallback
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.LoadingStatus
import net.k1ra.succubotapp.features.dashboard.repository.DashboardRetrofitInterface
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import retrofit2.Retrofit
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    retrofit: Retrofit,
    @ApplicationContext context: Context
) : ViewModel() {

    private val client = retrofit.create(DashboardRetrofitInterface::class.java)

    val robotStatuses = MutableLiveData<LoadingStatus<List<RobotStatus>>>(Loading(context.getString(R.string.loading_dashboard)))

    fun getRobotStatuses() = CoroutineScope(Dispatchers.IO).launch {
        client.getRobotStatuses().enqueue(BaseRetrofitCallback(robotStatuses::setValue))
    }

}