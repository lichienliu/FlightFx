import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// API key 與 release 簽章皆放 local.properties(不進 git),這裡統一載入
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
val freecurrencyApiKey: String = localProps.getProperty("FREECURRENCY_API_KEY", "")

android {
    namespace = "com.michaelliu.flightfx"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.michaelliu.flightfx"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        buildConfigField("String", "FREECURRENCY_API_KEY", "\"$freecurrencyApiKey\"")
        buildConfigField("boolean", "USE_MOCK", "false")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    // 簽章設定沒填時跳過(create 都不做),其他機器 clone 下來仍可建 debug
    val releaseStoreFile = localProps.getProperty("RELEASE_STORE_FILE", "")
    signingConfigs {
        if (releaseStoreFile.isNotEmpty()) {
            create("release") {
                storeFile = file(releaseStoreFile)
                storePassword = localProps.getProperty("RELEASE_STORE_PASSWORD", "")
                keyAlias = localProps.getProperty("RELEASE_KEY_ALIAS", "")
                keyPassword = localProps.getProperty("RELEASE_KEY_PASSWORD", "")
            }
        }
    }

    buildTypes {
        debug {
            buildConfigField("boolean", "USE_MOCK", "true")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.timber)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    implementation(libs.coil)
    implementation(libs.coil.network.okhttp)
    implementation(libs.facebook.shimmer)
    implementation(libs.androidx.swiperefreshlayout)

    implementation(libs.keval)
}
