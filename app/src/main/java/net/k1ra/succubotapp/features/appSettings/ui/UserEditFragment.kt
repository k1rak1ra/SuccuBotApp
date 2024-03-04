package net.k1ra.succubotapp.features.appSettings.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import net.k1ra.succubotapp.R
import net.k1ra.succubotapp.databinding.FragmentUserEditBinding
import net.k1ra.succubotapp.di.GsonProvider
import net.k1ra.succubotapp.features.appSettings.model.userManagement.UserManagementRequest
import net.k1ra.succubotapp.features.appSettings.viewmodel.UserManagementViewModel
import net.k1ra.succubotapp.features.authentication.model.User
import net.k1ra.succubotapp.features.base.model.Failure
import net.k1ra.succubotapp.features.base.model.Loading
import net.k1ra.succubotapp.features.base.model.Success
import net.k1ra.succubotapp.features.base.ui.GlideProgessDrawableProvider
import net.k1ra.succubotapp.features.base.ui.LoadingOverlay
import java.util.Base64

@AndroidEntryPoint
class UserEditFragment : Fragment() {
    private lateinit var binding: FragmentUserEditBinding
    private val viewModel: UserManagementViewModel by viewModels()
    private val args : UserEditFragmentArgs by navArgs()
    private var newProfilePicture: ByteArray? = null
    private var uid: String? = null

    private val profilePictureChooser = registerForActivityResult(ActivityResultContracts.GetContent()) { imageUri ->
        imageUri ?: return@registerForActivityResult

        requireContext().contentResolver.openInputStream(imageUri)?.readBytes().let { imageBytes ->
            newProfilePicture = imageBytes

            Glide.with(requireContext())
                .load(BitmapFactory.decodeByteArray(newProfilePicture, 0, newProfilePicture!!.size))
                .circleCrop()
                .into(binding.profilePicture)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserEditBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.updateUserStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            when (it) {
                is Failure -> {
                    if (it.code == 403) {
                        when (it.error) {
                            "nameEmpty" -> binding.fieldName.error = resources.getString(R.string.field_cannot_be_empty)
                            "emailEmpty" -> binding.fieldEmail.error = resources.getString(R.string.field_cannot_be_empty)
                            "emailNotUnique" -> binding.fieldEmail.error = resources.getString(R.string.email_not_unique)
                            "userNotFound" -> Snackbar.make(view, R.string.user_not_found, Snackbar.LENGTH_SHORT).show()
                            "userNotNative" -> Snackbar.make(view, R.string.user_not_native, Snackbar.LENGTH_SHORT).show()
                        }
                    } else {
                        Snackbar.make(view, R.string.connection_failed, Snackbar.LENGTH_SHORT).show()
                    }
                }
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    Snackbar.make(view, R.string.updated_successfully, Snackbar.LENGTH_SHORT).show()

                    if (args.user == null)
                        parentFragmentManager.popBackStackImmediate()
                }
            }
        }

        viewModel.deleteUserStatus.observeForever {
            LoadingOverlay.getInstance().hide()
            when (it) {
                is Failure -> Snackbar.make(view, R.string.connection_failed, Snackbar.LENGTH_SHORT).show()
                is Loading -> LoadingOverlay.getInstance().showOverlay(parentFragmentManager, it.text)
                is Success -> {
                    Snackbar.make(view, R.string.deleted_user, Snackbar.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStackImmediate()
                }
            }
        }

        if (args.user == null) {
            //New account creation
            binding.userSecurityCard.visibility = View.GONE
            binding.headingUserEdit.text = getString(R.string.create_user)
            binding.fieldUserIsAdmin.isChecked = false
        } else {
            //Editing existing account
            val user = GsonProvider.provideGson().fromJson(args.user, User::class.java)
            uid = user.uid

            binding.headingUserEdit.text = getString(R.string.edit_user)

            //If LDAP user, get rid of parameters that come from LDAP, we don't want them to be editable
            if (!user.native) {
                binding.fieldPassword.visibility = View.GONE
                binding.fieldEmail.visibility = View.GONE
                binding.fieldName.visibility = View.GONE
                binding.fieldUserIsAdmin.visibility = View.GONE
            } else {
                binding.fieldName.editText?.setText(user.name)
                binding.fieldEmail.editText?.setText(user.email)
                binding.fieldUserIsAdmin.isChecked = user.admin
            }

            Glide
                .with(requireContext())
                .load(user.picture)
                .circleCrop()
                .placeholder(GlideProgessDrawableProvider.getProgressBarIndeterminate(requireContext()))
                .fallback(R.drawable.user)
                .into(binding.profilePicture)
        }

        binding.btnUploadProfilePicture.setOnClickListener {
            profilePictureChooser.launch("image/*")
        }

        binding.btnSaveUserEdit.setOnClickListener {
            val changeProfilePicture = if (newProfilePicture != null)
                Base64.getEncoder().encodeToString(newProfilePicture)
            else
                null

            val changePassword = if (binding.fieldPassword.editText?.text.isNullOrEmpty())
                null
            else
                binding.fieldPassword.editText!!.text.toString()

            val changeName = if (binding.fieldName.editText?.text.isNullOrEmpty())
                null
            else
                binding.fieldName.editText!!.text.toString()

            val changeEmail = if (binding.fieldEmail.editText?.text.isNullOrEmpty())
                null
            else
                binding.fieldEmail.editText!!.text.toString()

            val changeIsAdmin = if (binding.fieldUserIsAdmin.visibility == View.GONE)
                null
            else
                binding.fieldUserIsAdmin.isChecked

            viewModel.updateUserStatus.value = Loading(getString(R.string.updating_user))
            viewModel.updateUser(uid ?: "new", UserManagementRequest(
                uploadImage = changeProfilePicture,
                passwordChange = changePassword,
                changeName = changeName,
                changeEmail = changeEmail,
                changeAdminStatus = changeIsAdmin
            ))
        }

        binding.btnLogoutEverywhere.setOnClickListener {
            viewModel.updateUserStatus.value = Loading(getString(R.string.updating_user))
            viewModel.updateUser(uid!!, UserManagementRequest(logoutOutOfAll = true))
        }

        binding.btnDeleteUser.setOnClickListener {
            viewModel.deleteUserStatus.value = Loading(getString(R.string.deleting_user))
            viewModel.deleteUser(uid!!)
        }
    }
}