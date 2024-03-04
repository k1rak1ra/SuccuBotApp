package net.k1ra.succubotapp.features.robotStatus.ui

import android.os.Bundle
import android.os.CountDownTimer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentRobotBinding
import net.k1ra.succubotapp.di.GsonProvider
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import net.k1ra.succubotapp.features.robotStatus.model.RobotCommand
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import net.k1ra.succubotapp.features.robotStatus.viewmodel.RobotStatusViewModel
import org.ocpsoft.prettytime.PrettyTime
import java.time.ZoneId
import java.time.ZoneOffset

@AndroidEntryPoint
class RobotFragment : Fragment() {
    private lateinit var binding: FragmentRobotBinding
    private val viewModel: RobotStatusViewModel by viewModels()
    private var robot: RobotStatus? = null

    private var mapLoaded = false
    private var pathLoaded = false

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) : Boolean {
            return when (menuItem.itemId) {
                R.id.action_settings -> {
                    if (robot != null)
                        findNavController().navigate(RobotFragmentDirections.actionRobotFragmentToRobotSettingsFragment(
                            GsonProvider.provideGson().toJson(robot)
                        ))
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
            if (viewModel.pathObservable.value !is Loading && viewModel.mapObservable.value !is Loading) {
                viewModel.fetchMap()
                viewModel.fetchPath()
            }

            if (viewModel.robotStatus.value !is Loading) {
                viewModel.getRobotStatus()
            }

            this.start()
        }
    }

    //If RobotSettingsFragment passes back updated robot, apply changes
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentResultListener(Constants.UpdatedRobotSettingsPassBackKey) { _, bundle ->
            val result = bundle.getString(Constants.UpdatedRobotSettingsPassBackKey) ?: return@setFragmentResultListener
            val robot = GsonProvider.provideGson().fromJson(result, RobotStatus::class.java)

            viewModel.robotStatus.value = Success(robot)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRobotBinding.inflate(inflater)
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        refreshTimer.onFinish()
        refreshTimer.start()
        requireActivity().addMenuProvider(menuProvider)
    }

    override fun onPause() {
        super.onPause()

        refreshTimer.cancel()
        requireActivity().removeMenuProvider(menuProvider)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshTimer.start()

        //Set up robotMapView
        binding.robotMapView.visibility = View.INVISIBLE
        binding.robotMapTextError.visibility = View.INVISIBLE
        binding.robotMapViewLoading.visibility = View.VISIBLE

        viewModel.mapObservable.observeForever {
            if (it is Success) {
                try {
                    binding.robotMapView.processCompressedMapBytes(it.result)
                } catch (e: Exception) {
                    viewModel.mapObservable.value = Failure(1, e.message)
                }
            } else if (it is Failure) {
                binding.robotMapView.visibility = View.INVISIBLE
                binding.robotMapTextError.visibility = View.VISIBLE
                binding.robotMapViewLoading.visibility = View.INVISIBLE
            }
        }

        viewModel.pathObservable.observeForever {
            if (it is Success) {
                try {
                    binding.robotMapView.parsePathData(it.result)
                } catch (e: Exception) {
                    e.printStackTrace()
                    viewModel.pathObservable.value = Failure(2, e.message)
                }
            } else if (it is Failure) {
                binding.robotMapView.visibility = View.INVISIBLE
                binding.robotMapTextError.visibility = View.VISIBLE
                binding.robotMapViewLoading.visibility = View.INVISIBLE
            }
        }

        val showDataRunnable = Runnable {
            if (mapLoaded && pathLoaded) {
                binding.robotMapView.visibility = View.VISIBLE
                binding.robotMapTextError.visibility = View.INVISIBLE
                binding.robotMapViewLoading.visibility = View.INVISIBLE
            }
        }

        binding.robotMapView.onDoneParsingMapData = Runnable {
            mapLoaded = true
            showDataRunnable.run()
        }

        binding.robotMapView.onDoneParsingPathData = Runnable {
            pathLoaded = true
            showDataRunnable.run()
        }

        viewModel.fetchMap()
        viewModel.fetchPath()

        //Set up robot status display
        viewModel.robotStatus.observeForever {
            if (it !is Loading)
                LoadingOverlay.getInstance().hide()

            if (it !is Success)
                return@observeForever

            robot = it.result

            //Set result to be passed back to dashboard
            setFragmentResult(
                Constants.UpdatedRobotStatusPassBackKey,
                bundleOf(Constants.UpdatedRobotStatusPassBackKey to GsonProvider.provideGson().toJson(it.result))
            )

            requireActivity().title = it.result.name

            binding.robotStatus.text = when(it.result.status) {
                "standby" -> resources.getString(R.string.standby)
                "charging" -> resources.getString(R.string.charging)
                "relocating" -> resources.getString(R.string.relocating)
                "goto_charge" -> resources.getString(R.string.goto_charge)
                "sleep" -> resources.getString(R.string.sleep)
                "smart" -> resources.getString(R.string.smart)
                else -> resources.getString(R.string.unknown_status)
            }

            binding.robotBatteryGauge.value = it.result.battery
            binding.robotBatteryText.text = resources.getString(R.string.robot_battery, it.result.battery)

            binding.lastCleanedAt.text = resources.getString(
                R.string.last_cleaned,
                PrettyTime().format(it.result.lastCleaned.atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime())
            )

            binding.robotArea.text = it.result.squareMetersCleanedLast.toString()
            binding.robotMinutes.text = it.result.minutesCleanedLast.toString()

            if (it.result.runningCleaningCycle) {
                binding.btnCleaningControl.text = resources.getString(R.string.pause_cleaning)
                binding.btnCleaningControl.setOnClickListener {
                    viewModel.sendRobotCommand(RobotCommand(runCleaningCycle = false))
                }
            } else {
                binding.btnCleaningControl.text = resources.getString(R.string.start_cleaning)
                binding.btnCleaningControl.setOnClickListener {
                    viewModel.sendRobotCommand(RobotCommand(runCleaningCycle = true))
                }
            }

            if (it.result.errCode == 0) {
                binding.robotStatusBanner.visibility = View.GONE
            } else {
                binding.robotStatusBanner.visibility = View.VISIBLE

                when (it.result.errCode) {
                    32 -> binding.robotErrorText.text = resources.getString(R.string.robot_err_mop)
                    1024 -> binding.robotErrorText.text = resources.getString(R.string.robot_err_stuck)
                    else -> binding.robotErrorText.text = resources.getString(R.string.robot_err_unknown)
                }
            }
        }

        viewModel.getRobotStatus()

        binding.btnReturnToCharger.setOnClickListener {
            viewModel.sendRobotCommand(RobotCommand(goToCharge = true))
        }

        viewModel.robotCommandStatus.observeForever {
            when (it) {
                is Failure -> {
                    LoadingOverlay.getInstance().hide()
                    Snackbar.make(view, R.string.command_failed, Snackbar.LENGTH_SHORT).show()
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> Snackbar.make(view, R.string.sent_command, Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
