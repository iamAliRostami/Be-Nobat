package com.leon.be_nobat.ui.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.leon.be_nobat.R
import com.leon.be_nobat.helpers.BaseActivity
import com.leon.be_nobat.ui.view_models.ProfileUiState
import com.leon.be_nobat.ui.view_models.ProfileViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.ByteArrayOutputStream

class ProfileActivity : BaseActivity() {
    override val layoutResourceId: Int = R.layout.activity_profile

    private val viewModel: ProfileViewModel by viewModel()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            uploadAvatarFromUri(uri)
        }
    }

    override fun setupViews() {
        setToolbarTitle(getString(R.string.profile_title))
        setMenuVisibility(false)

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnUploadAvatar).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnRemoveAvatar).setOnClickListener {
            viewModel.deleteAvatar()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveProfile).setOnClickListener {
            val name = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDisplayName).text.toString()
            val phone = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone).text.toString()
            viewModel.setDisplayName(name)
            viewModel.setPhoneNumber(phone)
            viewModel.saveProfile()
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangePassword).setOnClickListener {
            val current = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCurrentPassword).text.toString()
            val new = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewPassword).text.toString()
            viewModel.changePassword(current, new)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangeEmail).setOnClickListener {
            val current = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCurrentPassword).text.toString()
            val email = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNewEmail).text.toString()
            viewModel.changeEmail(email, current)
        }

        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLogout).setOnClickListener {
            viewModel.logout()
            val intent = Intent(this, StartActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finishAffinity()
        }

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { startActivity(Intent(this, HomeActivity::class.java)); finish(); true }
                R.id.nav_appointments -> { startActivity(Intent(this, AppointmentsActivity::class.java)); finish(); true }
                R.id.nav_favorites -> { startActivity(Intent(this, FavoritesActivity::class.java)); finish(); true }
                R.id.nav_profile -> { true }
                else -> false
            }
        })
        bottomNav.selectedItemId = R.id.nav_profile
    }

    override fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { render(it) }
            }
        }
    }

    private fun render(state: ProfileUiState) {
        val user = state.user
        if (user != null) {
            findViewById<android.widget.TextView>(R.id.tvDisplayName).text = user.displayName
            findViewById<android.widget.TextView>(R.id.tvEmail).text = user.email ?: ""
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etDisplayName).setText(state.displayName)
            findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPhone).setText(state.phoneNumber)
        }
        if (com.leon.be_nobat.BuildConfig.DEMO_MODE) {
            val avatar = findViewById<android.widget.ImageView>(R.id.ivAvatar)
            val bitmap = state.avatarUrl?.takeIf { it.startsWith("data:image/") }?.substringAfter("base64,")?.let { encoded ->
                runCatching { val bytes = Base64.decode(encoded, Base64.DEFAULT); BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }.getOrNull()
            }
            if (bitmap != null) avatar.setImageBitmap(bitmap) else avatar.setImageResource(com.leon.be_nobat.R.drawable.ic_person)
        }
        // Save button state
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSaveProfile).let {
            it.text = if (state.savingProfile) getString(R.string.saving) else getString(R.string.save)
            it.isEnabled = !state.savingProfile
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangePassword).let {
            it.text = if (state.changingPassword) getString(R.string.saving) else getString(R.string.change_password)
            it.isEnabled = !state.changingPassword
        }
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnChangeEmail).let {
            it.text = if (state.changingEmail) getString(R.string.saving) else getString(R.string.change_email)
            it.isEnabled = !state.changingEmail
        }
        // Messages
        state.message?.let { msg ->
            val resId = when (msg) {
                "saved" -> R.string.saved
                "password_changed" -> R.string.password_changed
                "email_changed" -> R.string.email_changed
                "avatar_updated" -> R.string.avatar_updated
                "avatar_removed" -> R.string.avatar_removed
                else -> null
            }
            resId?.let { Snackbar.make(findViewById(R.id.scrollView), getString(it), Snackbar.LENGTH_SHORT).show() }
            viewModel.clearMessages()
        }
        state.error?.let { err ->
            Snackbar.make(findViewById(R.id.scrollView), err, Snackbar.LENGTH_LONG).show()
            viewModel.clearMessages()
        }
    }

    private fun uploadAvatarFromUri(uri: Uri) {
        try {
            val input = contentResolver.openInputStream(uri) ?: return
            val bitmap = BitmapFactory.decodeStream(input)
            input.close()
            if (bitmap == null) {
                Snackbar.make(findViewById(R.id.scrollView), getString(R.string.error_generic), Snackbar.LENGTH_SHORT).show()
                return
            }

            // Downscale large bitmaps so we stay well under the 2 MiB raw
            // server-side limit even before base64 encoding (which inflates
            // by ~33%). 2048x2048 is a generous max for a profile avatar.
            val maxDim = 2048
            val scaled = if (bitmap.width > maxDim || bitmap.height > maxDim) {
                val scale = maxDim.toFloat() / maxOf(bitmap.width, bitmap.height)
                android.graphics.Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scale).toInt().coerceAtLeast(1),
                    (bitmap.height * scale).toInt().coerceAtLeast(1),
                    true,
                ).also { if (it !== bitmap) bitmap.recycle() }
            } else bitmap

            // Encode as JPEG (much smaller than PNG for photo content),
            // then fall back to PNG if JPEG size is unexpectedly large.
            val jpegStream = ByteArrayOutputStream()
            scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, jpegStream)
            var bytes = jpegStream.toByteArray()
            var contentType = "image/jpeg"

            // Hard cap at ~1.4 MiB raw so base64 output stays under 2 MiB.
            val maxRaw = 1_400_000
            if (bytes.size > maxRaw) {
                val lowered = ByteArrayOutputStream()
                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 60, lowered)
                bytes = lowered.toByteArray()
            }
            // Final fallback: if JPEG is still too big, drop quality to 40.
            if (bytes.size > maxRaw) {
                val last = ByteArrayOutputStream()
                scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 40, last)
                bytes = last.toByteArray()
            }

            scaled.recycle()
            val base64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
            viewModel.uploadAvatar(contentType, base64)
        } catch (e: Exception) {
            Snackbar.make(findViewById(R.id.scrollView), e.message ?: getString(R.string.error_generic), Snackbar.LENGTH_SHORT).show()
        }
    }
}
