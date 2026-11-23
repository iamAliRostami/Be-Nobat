package com.leon.be_nobat.ui.activities

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.leon.be_nobat.BuildConfig
import com.leon.be_nobat.R
import com.leon.be_nobat.data.local.TokenManager
import com.leon.be_nobat.helpers.DeepLinkHelper
import com.leon.be_nobat.ui.views.SplashCheckmarkView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class StartActivity : AppCompatActivity() {
    companion object {
        private const val DEFAULT_LANGUAGE_TAG = "fa"
        private const val STANDARD_SPLASH_DURATION_MS = 5000L
    }

    private val tokenManager: TokenManager by inject()
    private var isNavigated = false
    private var resolvedTarget: Intent? = null

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

        findViewById<android.widget.TextView>(R.id.tvAppVersion)?.text =
            getString(R.string.app_version_format, toPersianDigits(BuildConfig.VERSION_NAME))

        setupSplashAnimation()

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

    private fun setupSplashAnimation() {
        val videoView = findViewById<android.widget.VideoView>(R.id.vvSplashVideo)
        val centerBranding = findViewById<View>(R.id.centerBrandingLayout)
        val featuresLayout = findViewById<View>(R.id.featuresLayout)

        try {
            val videoUri = android.net.Uri.parse("android.resource://$packageName/${R.raw.benobat_splash}")
            videoView?.apply {
                visibility = View.VISIBLE
                centerBranding?.visibility = View.INVISIBLE
                featuresLayout?.visibility = View.INVISIBLE
                setVideoURI(videoUri)
                setOnPreparedListener { mp ->
                    mp.isLooping = false
                    start()
                }
                setOnCompletionListener {
                    resolvedTarget?.let { target -> navigateToTarget(target) }
                }
                setOnErrorListener { _, _, _ ->
                    visibility = View.GONE
                    centerBranding?.visibility = View.VISIBLE
                    featuresLayout?.visibility = View.VISIBLE
                    startSplashAnimations()
                    true
                }
            } ?: run {
                startSplashAnimations()
            }
        } catch (_: Exception) {
            videoView?.visibility = View.GONE
            centerBranding?.visibility = View.VISIBLE
            featuresLayout?.visibility = View.VISIBLE
            startSplashAnimations()
        }
    }

    private fun startSplashAnimations() {
        val emblemContainer = findViewById<FrameLayout>(R.id.emblemContainer)
        val ivInfinity = findViewById<ImageView>(R.id.ivSplashInfinity)
        val viewCheckmark = findViewById<SplashCheckmarkView>(R.id.viewSplashCheckmark)
        val ivTick12 = findViewById<ImageView>(R.id.ivSplashTick12)
        val ivTick3 = findViewById<ImageView>(R.id.ivSplashTick3)
        val ivTick6 = findViewById<ImageView>(R.id.ivSplashTick6)
        val ivTick9 = findViewById<ImageView>(R.id.ivSplashTick9)
        val titleSloganLayout = findViewById<View>(R.id.titleSloganLayout)
        val ivBadgeSmart = findViewById<ImageView>(R.id.ivBadgeSmart)
        val ivBadgeSecure = findViewById<ImageView>(R.id.ivBadgeSecure)
        val ivBadgeFast = findViewById<ImageView>(R.id.ivBadgeFast)

        val density = resources.displayMetrics.density

        // 1. Red marked part: Infinity Symbol (rotates and zooms in)
        ivInfinity.scaleX = 0.2f
        ivInfinity.scaleY = 0.2f
        ivInfinity.rotation = -220f
        ivInfinity.alpha = 0f

        // 2. Blue marked part: Checkmark starts at progress 0
        viewCheckmark.progress = 0f

        // 3. Yellow marked part: 4 Clock Ticks (scale 0, alpha 0)
        listOf(ivTick12, ivTick3, ivTick6, ivTick9).forEach { tick ->
            tick.scaleX = 0f
            tick.scaleY = 0f
            tick.alpha = 0f
        }

        // Set pivot points for clock ticks to pop from their respective positions
        emblemContainer.post {
            val w = emblemContainer.width.toFloat()
            val h = emblemContainer.height.toFloat()
            ivTick12.pivotX = w * (515f / 650f)
            ivTick12.pivotY = h * (166f / 392f)

            ivTick3.pivotX = w * (627f / 650f)
            ivTick3.pivotY = h * (270f / 392f)

            ivTick6.pivotX = w * (515f / 650f)
            ivTick6.pivotY = h * (379f / 392f)

            ivTick9.pivotX = w * (417f / 650f)
            ivTick9.pivotY = h * (270f / 392f)
        }

        // 4. Grey marked part: Title & Slogan (zooms in from far away)
        titleSloganLayout.scaleX = 0.2f
        titleSloganLayout.scaleY = 0.2f
        titleSloganLayout.alpha = 0f

        // 5. Orange marked part: 3 Badges (slide up from bottom)
        val badgeSlide = 65f * density
        ivBadgeSmart.translationY = badgeSlide
        ivBadgeSmart.alpha = 0f

        ivBadgeSecure.translationY = badgeSlide
        ivBadgeSecure.alpha = 0f

        ivBadgeFast.translationY = badgeSlide
        ivBadgeFast.alpha = 0f

        // --- EXECUTE ANIMATIONS ---

        // A) Red Part: Infinity Symbol rotates and scales into place
        ivInfinity.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .rotation(0f)
            .alpha(1.0f)
            .setDuration(850)
            .setInterpolator(DecelerateInterpolator(1.3f))
            .start()

        // B) Blue Part: Checkmark draws in arrow direction (short arm -> vertex -> long arm)
        ObjectAnimator.ofFloat(viewCheckmark, "progress", 0f, 1f).apply {
            duration = 750
            startDelay = 120
            interpolator = DecelerateInterpolator(1.2f)
            start()
        }

        // C) Yellow Part: 4 Clock Ticks appear one by one clockwise (12 -> 3 -> 6 -> 9)
        ivTick12.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setStartDelay(200)
            .setDuration(260)
            .setInterpolator(OvershootInterpolator(2.2f))
            .start()

        ivTick3.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setStartDelay(360)
            .setDuration(260)
            .setInterpolator(OvershootInterpolator(2.2f))
            .start()

        ivTick6.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setStartDelay(520)
            .setDuration(260)
            .setInterpolator(OvershootInterpolator(2.2f))
            .start()

        ivTick9.animate()
            .scaleX(1f).scaleY(1f).alpha(1f)
            .setStartDelay(680)
            .setDuration(260)
            .setInterpolator(OvershootInterpolator(2.2f))
            .start()

        // D) Grey Part: Title and slogan zoom in from far away to actual size
        titleSloganLayout.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .alpha(1.0f)
            .setStartDelay(250)
            .setDuration(750)
            .setInterpolator(DecelerateInterpolator(1.2f))
            .start()

        // E) Orange Part: Badges slide up 1 by 1 from right to left (Smart 1 -> Secure 2 -> Fast 3)
        // 1. Right: مدیریت هوشمند
        ivBadgeSmart.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(950)
            .setDuration(450)
            .setInterpolator(DecelerateInterpolator(1.4f))
            .start()

        // 2. Center: مطمئن و امن
        ivBadgeSecure.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(1130)
            .setDuration(450)
            .setInterpolator(DecelerateInterpolator(1.4f))
            .start()

        // 3. Left: سریع و آسان
        ivBadgeFast.animate()
            .translationY(0f)
            .alpha(1.0f)
            .setStartDelay(1310)
            .setDuration(450)
            .setInterpolator(DecelerateInterpolator(1.4f))
            .start()
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

    private fun toPersianDigits(text: String): String {
        val persian = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')
        return text.map { if (it in '0'..'9') persian[it - '0'] else it }.joinToString("")
    }
}