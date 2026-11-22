package com.leon.be_nobat.ui.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.leon.be_nobat.R
import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.helpers.DeepLinkHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class StartActivity : AppCompatActivity() {
    companion object {
        private const val DEFAULT_LANGUAGE_TAG = "fa"
        private const val STANDARD_SPLASH_DURATION_MS = 2800L
    }

    private val tokenManager: TokenManager by inject()
    private var isNavigated = false
    private var resolvedTarget: Intent? = null
    private var infinityRotationAnimator: ObjectAnimator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            splashScreenViewProvider.remove()
        }
        super.onCreate(savedInstanceState)
        setDefaultLocaleIfNeeded()

        // Edge-to-edge transparent system bars for seamless gradient background
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        setContentView(R.layout.activity_start)

        startSplashAnimations()

        // Allow tapping anywhere to skip the remaining splash duration
        findViewById<View>(R.id.splashRoot)?.setOnClickListener {
            resolvedTarget?.let { target -> navigateToTarget(target) }
        }

        lifecycleScope.launch {
            // Resolve target route in background while animations run
            val target = resolveTargetIntent()
            resolvedTarget = target

            // Keep standard splash screen visible for standard duration
            delay(STANDARD_SPLASH_DURATION_MS)

            navigateToTarget(target)
        }
    }

    private fun startSplashAnimations() {
        val ivInfinity = findViewById<ImageView>(R.id.ivSplashInfinity)
        val ivHands = findViewById<ImageView>(R.id.ivSplashHands)
        val ivTick12 = findViewById<ImageView>(R.id.ivSplashTick12)
        val ivTick3 = findViewById<ImageView>(R.id.ivSplashTick3)
        val ivTick6 = findViewById<ImageView>(R.id.ivSplashTick6)
        val ivTick9 = findViewById<ImageView>(R.id.ivSplashTick9)
        val ivTitle = findViewById<ImageView>(R.id.ivSplashTitle)
        val ivSlogan = findViewById<ImageView>(R.id.ivSplashSlogan)
        val ivFeatureSmart = findViewById<ImageView>(R.id.ivFeatureSmart)
        val ivFeatureSecure = findViewById<ImageView>(R.id.ivFeatureSecure)
        val ivFeatureFast = findViewById<ImageView>(R.id.ivFeatureFast)

        // 1. Infinity Shape: continuous infinite rotation
        infinityRotationAnimator = ObjectAnimator.ofFloat(ivInfinity, View.ROTATION, 0f, 360f).apply {
            duration = 6000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            start()
        }

        // 2. Checkmark Clock Hands: scale up from small to large
        ivHands.scaleX = 0f
        ivHands.scaleY = 0f
        ivHands.alpha = 0f
        ivHands.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .alpha(1.0f)
            .setDuration(850L)
            .setInterpolator(OvershootInterpolator(1.4f))
            .start()

        // 3. Clock Ticks (12h, 3h, 6h, 9h): appear one by one clockwise
        val ticks = listOf(ivTick12, ivTick3, ivTick6, ivTick9)
        ticks.forEachIndexed { index, tickView ->
            tickView.scaleX = 0.2f
            tickView.scaleY = 0.2f
            tickView.alpha = 0f
            tickView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .alpha(1.0f)
                .setStartDelay(350L + (index * 180L))
                .setDuration(380L)
                .setInterpolator(OvershootInterpolator(1.3f))
                .start()
        }

        // 4. Title & Slogan: appear simultaneously
        val slideOffset = 30f * resources.displayMetrics.density
        ivTitle.translationY = slideOffset
        ivTitle.alpha = 0f
        ivSlogan.translationY = slideOffset
        ivSlogan.alpha = 0f

        val titleDelay = 1050L
        ivTitle.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(titleDelay)
            .setDuration(600L)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()

        ivSlogan.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(titleDelay)
            .setDuration(600L)
            .setInterpolator(DecelerateInterpolator(1.5f))
            .start()

        // 5. Feature icons and texts: slide up from below one by one, color/alpha from faint to full
        val featureSlide = 40f * resources.displayMetrics.density
        val features = listOf(ivFeatureSmart, ivFeatureSecure, ivFeatureFast)
        features.forEachIndexed { index, featureView ->
            featureView.translationY = featureSlide
            featureView.alpha = 0f
            featureView.animate()
                .translationY(0f)
                .alpha(1.0f)
                .setStartDelay(1350L + (index * 220L))
                .setDuration(550L)
                .setInterpolator(DecelerateInterpolator(1.4f))
                .start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        infinityRotationAnimator?.cancel()
    }

    private suspend fun resolveTargetIntent(): Intent {
        val deepLinkUri = intent?.data
        if (deepLinkUri != null) {
            val parsed = DeepLinkHelper.parse(deepLinkUri)
            if (parsed != null && (!parsed.businessId.isNullOrBlank() || !parsed.branchId.isNullOrBlank() || !parsed.providerId.isNullOrBlank())) {
                val businessRepository: com.leon.be_nobat.domain.repository.BusinessRepository =
                    org.koin.java.KoinJavaComponent.get(com.leon.be_nobat.domain.repository.BusinessRepository::class.java)
                var resolvedBizId = parsed.businessId
                var resolvedBranchId = parsed.branchId

                if (resolvedBizId.isNullOrBlank() && !parsed.providerId.isNullOrBlank()) {
                    val providerInfo = businessRepository.findBusinessIdByProvider(parsed.providerId)
                    if (providerInfo != null) {
                        resolvedBizId = providerInfo.first
                        resolvedBranchId = providerInfo.second
                    }
                }

                if (resolvedBizId.isNullOrBlank() && !resolvedBranchId.isNullOrBlank()) {
                    resolvedBizId = businessRepository.findBusinessIdByBranch(resolvedBranchId)
                }

                if (parsed.isBookingRoute && !resolvedBizId.isNullOrBlank()) {
                    return Intent(this, BookingActivity::class.java).apply {
                        putExtra("businessId", resolvedBizId)
                        resolvedBranchId?.let { putExtra("branchId", it) }
                        parsed.providerId?.let { putExtra("providerId", it) }
                    }
                } else if (!resolvedBizId.isNullOrBlank()) {
                    return Intent(this, BusinessDetailActivity::class.java).apply {
                        putExtra("businessId", resolvedBizId)
                        resolvedBranchId?.let { putExtra("branchId", it) }
                        parsed.providerId?.let { putExtra("providerId", it) }
                    }
                }
            }
        }

        val session = tokenManager.currentSession()
        return if (session != null) {
            if (session.user.isStaff) {
                Intent(this, AdminHomeActivity::class.java)
            } else {
                Intent(this, HomeActivity::class.java)
            }
        } else {
            Intent(this, LoginActivity::class.java)
        }
    }

    private fun navigateToTarget(target: Intent) {
        if (isNavigated || isFinishing || isDestroyed) return
        isNavigated = true
        startActivity(target)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }

    private fun setDefaultLocaleIfNeeded() {
        if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            AppCompatDelegate.setApplicationLocales(
                LocaleListCompat.forLanguageTags(DEFAULT_LANGUAGE_TAG)
            )
        }
    }
}
