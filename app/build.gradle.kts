plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.leon.be_nobat"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.leon.be_nobat"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs{
        create("release") {
            keyAlias = "ali_angel"
            keyPassword = "kaka019930"
            storeFile = file("D:/keys/MyFourthKey.jks")
            storePassword = "kaka019930"
        }
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
        getByName("main") {
            res {
                java.directories.add("src/main/java")
                resources.directories.add("src/main/res")
            }
        }
    }
    buildTypes {
        release {
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
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
//    implementation(libs.pocketbase.kotlin)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}