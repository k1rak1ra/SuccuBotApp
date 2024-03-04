package net.k1ra.succubotapp.features.dashboard.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentDashboardBinding
import net.k1ra.succubotapp.di.GsonProvider
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.ErrorOverlay
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import net.k1ra.succubotapp.features.dashboard.adapters.DashboardAdapter
import net.k1ra.succubotapp.features.dashboard.viewmodel.DashboardViewModel
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()

    private val adapter = DashboardAdapter(arrayListOf(), object: DashboardAdapter.Listener {
        override fun onClicked(item: RobotStatus) {
            findNavController().navigate(
                DashboardFragmentDirections.actionDashboardFragmentToRobotFragment(GsonProvider.provideGson().toJson(item))
            )
        }
    })

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) : Boolean {
            return when (menuItem.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(DashboardFragmentDirections.actionDashboardFragmentToAppSettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private val refreshTimer = object : CountDownTimer(2000L, 1000L) {
        override fun onTick(tick: Long) {
            //Do nothing
        }

        override fun onFinish() {
            if (viewModel.robotStatuses.value !is Loading) {
                viewModel.getRobotStatuses()
            }

            this.start()
        }
    }

    //If RobotFragment passes back updated robot, apply changes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(Constants.UpdatedRobotStatusPassBackKey) { _, bundle ->
            val result = bundle.getString(Constants.UpdatedRobotStatusPassBackKey) ?: return@setFragmentResultListener
            val robot = GsonProvider.provideGson().fromJson(result, RobotStatus::class.java)

            val index = adapter.list.indexOfFirst { it.did == robot.did }
            adapter.list[index] = robot
            adapter.notifyItemChanged(index)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater)
        return binding.root
    }

    override fun onPause() {
        super.onPause()

        requireActivity().removeMenuProvider(menuProvider)
        refreshTimer.cancel()
    }

    override fun onResume() {
        super.onResume()

        requireActivity().addMenuProvider(menuProvider)
        refreshTimer.onFinish()
        refreshTimer.start()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                //Do nothing, prevent navigation back
            }
        })

        requireActivity().title = resources.getString(R.string.dashboard)

        viewModel.robotStatuses.observeForever {
            when(it) {
                is Failure -> {
                    LoadingOverlay.getInstance().hide()
                    if (it.code != 401) {
                        ErrorOverlay.getInstance().showOverlay(parentFragmentManager, resources.getString(R.string.connection_failed)) {
                            viewModel.robotStatuses.value = Loading(resources.getString(R.string.loading_dashboard))
                            viewModel.getRobotStatuses()
                        }
                    }
                }
                is Loading -> {
                    ErrorOverlay.getInstance().hide()
                    LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                }
                is Success -> {
                    ErrorOverlay.getInstance().hide()
                    LoadingOverlay.getInstance().hide()
                    adapter.list.clear()

                    if (it.result.isEmpty()) {
                        binding.dashboardRv.visibility = View.GONE
                        binding.dashboardNoItems.visibility = View.VISIBLE
                    } else {
                        binding.dashboardRv.visibility = View.VISIBLE
                        binding.dashboardNoItems.visibility = View.GONE

                        adapter.list.addAll(it.result)
                        adapter.notifyDataSetChanged()
                    }
                }
            }
        }

        viewModel.getRobotStatuses()

        binding.dashboardRv.layoutManager = LinearLayoutManager(requireContext())
        binding.dashboardRv.adapter = adapter
    }
}