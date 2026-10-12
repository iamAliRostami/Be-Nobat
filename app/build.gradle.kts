plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}

val productionApiUrl = providers.environmentVariable("BENOBAT_API_BASE_URL").orNull?.trimEnd('/')
require(productionApiUrl == null || (productionApiUrl.startsWith("https://") && !productionApiUrl.contains('"'))) {
    "BENOBAT_API_BASE_URL must be an HTTPS URL."
}
android {
    namespace = "com.leon.be_nobat"
    compileSdk = 37
    defaultConfig {
        applicationId = "com.leon.be_nobat"
        minSdk = 24
        targetSdk = 37
        versionCode = 2
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8080\"")
        // Retained only for the legacy login classes, which are no longer the launcher.
        buildConfigField("String", "POCKET_BASE_URL", "\"http://10.0.2.2:8090\"")
        buildConfigField("String", "AUTH_LOGIN_PATH", "\"/api/collections/users/auth-with-password\"")
    }
    flavorDimensions += "person"
    productFlavors {
        create("user") { dimension = "person" }
        create("admin") { dimension = "person"; applicationIdSuffix = ".admin" }
    }
    val signingPath = providers.environmentVariable("BENOBAT_KEYSTORE_PATH").orNull
    if (signingPath != null) signingConfigs.create("release") {
        storeFile = file(signingPath)
        storePassword = providers.environmentVariable("BENOBAT_KEYSTORE_PASSWORD").get()
        keyAlias = providers.environmentVariable("BENOBAT_KEY_ALIAS").get()
        keyPassword = providers.environmentVariable("BENOBAT_KEY_PASSWORD").get()
    }
    buildTypes {
        release {
            buildConfigField("String", "API_BASE_URL", "\"${productionApiUrl ?: ""}\"")
            isMinifyEnabled = false
            isDebuggable = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (signingPath != null) signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures { viewBinding = true; buildConfig = true }
}
dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation("io.ktor:ktor-serialization-kotlinx-json:" + libs.versions.ktorClientAndroid.get())
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.auth)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")
    testImplementation(libs.junit)
    testImplementation("org.json:json:20260814")
    testImplementation("io.ktor:ktor-client-mock:" + libs.versions.ktorClientAndroid.get())
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}