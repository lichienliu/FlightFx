import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

val freecurrencyApiKey: String = Properties().run {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
    getProperty("FREECURRENCY_API_KEY", "")
}

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

    buildTypes {
        debug {
            buildConfigField("boolean", "USE_MOCK", "true")
        }
        release {
            optimization {
                enable = false
            }
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
}
