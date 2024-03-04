package net.k1ra.succubotapp.features.robotStatus.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.Constants
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentRobotSettingsBinding
import net.k1ra.succubotapp.di.GsonProvider
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import net.k1ra.succubotapp.features.robotStatus.model.RobotCommand
import net.k1ra.succubotapp.features.robotStatus.model.RobotStatus
import net.k1ra.succubotapp.features.robotStatus.model.SuctionPowers
import net.k1ra.succubotapp.features.robotStatus.model.VolumeLevels
import net.k1ra.succubotapp.features.robotStatus.model.WaterFlowLevels
import net.k1ra.succubotapp.features.robotStatus.viewmodel.RobotStatusViewModel
import java.time.LocalTime

@AndroidEntryPoint
class RobotSettingsFragment : Fragment() {
    private lateinit var binding: FragmentRobotSettingsBinding
    private val viewModel: RobotStatusViewModel by viewModels()
    private val args : RobotFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRobotSettingsBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var robot = GsonProvider.provideGson().fromJson(args.robotStatus, RobotStatus::class.java)

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

        viewModel.robotStatus.observeForever {
            if (it !is Loading)
                LoadingOverlay.getInstance().hide()

            if (it !is Success)
                return@observeForever

            robot = it.result

            //Set result to be passed back to dashboard
            setFragmentResult(
                Constants.UpdatedRobotSettingsPassBackKey,
                bundleOf(Constants.UpdatedRobotSettingsPassBackKey to GsonProvider.provideGson().toJson(it.result))
            )

            requireActivity().title = it.result.name
        }

        binding.fieldRobotName.editText?.setText(robot.name)
        binding.fieldSuctionPower.value = (robot.suctionPower.ordinal + 1).toFloat()
        binding.fieldWaterFlow.value = (robot.waterFlow.ordinal + 1).toFloat()
        binding.fieldVolume.value = robot.volume.ordinal.toFloat()
        binding.fieldContinueCleaningAfterCharge.isChecked = robot.continueCleaningAfterCharge

        binding.btnSaveRobotSettings.setOnClickListener {
            viewModel.sendRobotCommand(RobotCommand(
                changeName = valueOrNullIfEquals(binding.fieldRobotName.editText!!.text.toString(), robot.name),
                suctionPower = valueOrNullIfEquals(SuctionPowers.entries[binding.fieldSuctionPower.value.toInt()-1], robot.suctionPower),
                waterFlow = valueOrNullIfEquals(WaterFlowLevels.entries[binding.fieldWaterFlow.value.toInt()-1], robot.waterFlow),
                volumeLevels = valueOrNullIfEquals(VolumeLevels.entries[binding.fieldVolume.value.toInt()], robot.volume),
                continueCleaningAfterCharge = valueOrNullIfEquals(binding.fieldContinueCleaningAfterCharge.isChecked, robot.continueCleaningAfterCharge)
            ))
        }

        binding.fieldAutocleanEnabled.isChecked = robot.dailyAutoCleanEnabled
        binding.fieldAutocleanTime.editText?.setText(robot.dailyCleaningTime.toString())

        binding.btnSaveAutocleanSettings.setOnClickListener {
            try {
                val time = LocalTime.parse(binding.fieldAutocleanTime.editText!!.text.toString())
                viewModel.sendRobotCommand(RobotCommand(
                    changeAutoCleanEnabled = binding.fieldAutocleanEnabled.isChecked,
                    changeAutoCleanTime = time
                ))
            } catch (e: Exception) {
                Snackbar.make(view, R.string.failed_to_parse_time, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    private fun<T> valueOrNullIfEquals(new: T, old: T) : T? {
        return if (new == old)
            null
        else
            new
    }
}