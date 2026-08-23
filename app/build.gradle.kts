plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.leon.be_nobat"
    compileSdk = 37

    signingConfigs{
        create("release") {
            keyAlias = "ali_angel"
            keyPassword = "kaka019930"
            storeFile = file("D:/keys/MyFourthKey.jks")
            storePassword = "kaka019930"
        }
    }
    defaultConfig {
        applicationId = "com.leon.be_nobat"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        signingConfig = signingConfigs.getByName("release")
        buildConfigField("String", "POCKET_BASE_URL", "\"http://10.0.2.2:8090\"")
        buildConfigField("String", "AUTH_LOGIN_PATH", "\"/api/collections/users/auth-with-password\"")
    }

    flavorDimensions += "person"
    productFlavors {
        create("user") {
            dimension = "person"
        }
        create("admin") {
            dimension = "person"
            applicationIdSuffix = ".admin"
        }
    }
    sourceSets {
        getByName("admin") {
            res {
                manifest.srcFile("src/admin/AndroidManifest.xml")
                java.directories.add("src/admin/java")
                resources.directories.add("src/admin/res")
            }
        }
        getByName("user") {
            res {
                manifest.srcFile("src/user/AndroidManifest.xml")
                java.directories.add("src/user/java")
                resources.directories.add("src/user/res")
            }
        }
       /*getByName("main") {
            res {
                java.directories.add("src/main/java")
                resources.directories.add("src/main/res")
            }
        }*/
    }
    buildTypes {
        release {
//            buildConfigField("String", "POCKET_BASE_URL", "\"https://api.example.com\"")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // PocketBase & Networking
    implementation(libs.agrevster.pocketbase.kotlin)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.ktor.client.auth)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.koin.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}