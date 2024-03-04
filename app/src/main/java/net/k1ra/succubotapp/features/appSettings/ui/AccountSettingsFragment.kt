package net.k1ra.succubotapp.features.appSettings.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentSettingsAccountBinding
import net.k1ra.succubotapp.features.appSettings.model.PasswordChange
import net.k1ra.succubotapp.features.appSettings.model.UserRequest
import net.k1ra.succubotapp.features.appSettings.viewmodel.UserSettingsViewModel
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.GlideProgessDrawableProvider
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import java.util.Base64
import javax.inject.Inject


@AndroidEntryPoint
class AccountSettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsAccountBinding
    private val viewModel: UserSettingsViewModel by viewModels()
    var user: User? = null
        @Inject set

    private val profilePictureChooser = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
        imageUri ?: return@registerForActivityResult

        requireContext().contentResolver.openInputStream(imageUri)?.readBytes().let { imageBytes ->
            viewModel.updateUserStatus.value = Loading(resources.getString(R.string.uploading_image))
            viewModel.updateUser(UserRequest(uploadImage = Base64.getEncoder().encodeToString(imageBytes)))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsAccountBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = resources.getString(R.string.settings_account)

        viewModel.updateUserStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            when (it) {
                is Failure -> {
                    if (it.code == 403) {
                        when (it.error) {
                            "nameEmpty" -> binding.fieldName.error = resources.getString(R.string.field_cannot_be_empty)
                            "emailEmpty" -> binding.fieldEmail.error = resources.getString(R.string.field_cannot_be_empty)
                            "emailNotUnique" -> binding.fieldEmail.error = resources.getString(R.string.email_not_unique)
                            "passwordNoMatch" -> {
                                binding.fieldPasswordNew.error = resources.getString(R.string.passwords_dont_match)
                                binding.fieldPasswordNewConfirm.error = resources.getString(R.string.passwords_dont_match)
                            }
                            "newLength" -> {
                                binding.fieldPasswordNew.error = resources.getString(R.string.password_too_short)
                                binding.fieldPasswordNewConfirm.error = resources.getString(R.string.password_too_short)
                            }
                            "oldIncorrect" -> binding.fieldPasswordOld.error = resources.getString(R.string.incorrect_credentials)
                        }
                    } else {
                        Snackbar.make(view, R.string.connection_failed, Snackbar.LENGTH_SHORT).show()
                    }
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    user = it.result
                    loadProfilePicture() //It'll just be loaded from cache if it's unchanged
                    Snackbar.make(view, R.string.updated_successfully, Snackbar.LENGTH_SHORT).show()
                }
            }
        }

        loadProfilePicture()

        binding.fieldEmail.editText!!.setText(user!!.email)
        binding.fieldName.editText!!.setText(user!!.name)
        binding.btnSaveEmailName.setOnClickListener {
            binding.fieldEmail.error = null
            binding.fieldName.error = null

            viewModel.updateUserStatus.value = Loading(resources.getString(R.string.updating_name_and_email))
            viewModel.updateUser(UserRequest(
                changeEmail = binding.fieldEmail.editText!!.text.toString(),
                changeName = binding.fieldName.editText!!.text.toString()
            ))
        }

        binding.btnSavePassword.setOnClickListener {
            binding.fieldPasswordOld.error = null
            binding.fieldPasswordNew.error = null
            binding.fieldPasswordNewConfirm.error = null

            if (binding.fieldPasswordNew.editText!!.text.toString() == binding.fieldPasswordNewConfirm.editText!!.text.toString()) {
                viewModel.updateUserStatus.value = Loading(resources.getString(R.string.updating_password))
                viewModel.updateUser(UserRequest(
                    passwordChange = PasswordChange(
                        binding.fieldPasswordOld.editText!!.text.toString(),
                        binding.fieldPasswordNew.editText!!.text.toString()
                    )
                ))

                binding.fieldPasswordOld.editText?.text?.clear()
                binding.fieldPasswordNew.editText?.text?.clear()
                binding.fieldPasswordNewConfirm.editText?.text?.clear()
            } else {
                viewModel.updateUserStatus.value = Failure(403, "passwordNoMatch")
            }
        }

        binding.btnUploadProfilePicture.setOnClickListener {
            profilePictureChooser.launch("image/*")
        }

        binding.btnLogoutEverywhere.setOnClickListener {
            viewModel.updateUserStatus.value = Loading(resources.getString(R.string.logging_out))
            viewModel.updateUser(UserRequest(logoutOutOfAll = true))
        }

        //Only native users can change their passwords, names, and emails
        if (!user!!.native) {
            binding.passwordChangeCard.visibility = View.GONE
            binding.nameEmailCard.visibility = View.GONE
        }
    }

    private fun loadProfilePicture() {
        Glide
            .with(this)
            .load(user!!.picture)
            .circleCrop()
            .placeholder(GlideProgessDrawableProvider.getProgressBarIndeterminate(requireContext()))
            .fallback(R.drawable.user)
            .into(binding.profilePicture)
    }
}