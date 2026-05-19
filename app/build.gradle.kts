plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

val configuredDataSourceProvider = providers.gradleProperty("dataSourceProvider").orNull ?: "cloudflare"
val configuredCloudflareBaseUrl = providers.gradleProperty("cloudflareApiBaseUrl").orNull
val configuredPythonBaseUrl = providers.gradleProperty("pythonApiBaseUrl").orNull
val configuredTenantKey = providers.gradleProperty("tenantKey").orNull ?: "hkbp-kedaton"

fun String.toEnvKey(): String {
    return replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()
}

fun configuredValue(name: String): String? {
    return providers.gradleProperty(name).orNull
        ?: providers.environmentVariable(name.toEnvKey()).orNull
}

fun stringResourceValue(value: String): String {
    return "\"${value.replace("\\", "\\\\").replace("\"", "\\\"")}\""
}

val configuredGoogleWebClientId = configuredValue("googleWebClientId").orEmpty()
val configuredDebugGoogleWebClientId =
    configuredValue("debugGoogleWebClientId") ?: configuredGoogleWebClientId
val configuredSimulateGoogleWebClientId =
    configuredValue("simulateGoogleWebClientId") ?: configuredGoogleWebClientId
val configuredReleaseGoogleWebClientId =
    configuredValue("releaseGoogleWebClientId") ?: configuredGoogleWebClientId

android {
    namespace = "com.lampung.baktimarsada"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("ibadah.jks")
            storePassword = "123123"
            keyAlias = "hkbp"
            keyPassword = "123123"
        }
    }

    defaultConfig {
        applicationId = "com.lampung.baktimarsada"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        buildConfigField("String", "API_BASE_URL", "\"https://example.com/\"")
        buildConfigField("String", "API_BASE_URL_CLOUDFLARE", "\"https://example.com/\"")
        buildConfigField("String", "API_BASE_URL_PYTHON", "\"https://example.com/\"")
        buildConfigField("String", "DATA_SOURCE_PROVIDER", "\"$configuredDataSourceProvider\"")
        buildConfigField("String", "TENANT_KEY", "\"$configuredTenantKey\"")
        buildConfigField("String", "APP_ENVIRONMENT", "\"debug\"")
        buildConfigField("String", "DATABASE_NAME", "\"bakti_marsada.db\"")
        buildConfigField("Boolean", "SIMULATION_ENABLED", "false")
        resValue("string", "bakti_google_web_client_id", stringResourceValue(configuredGoogleWebClientId))

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        debug {
            val debugCloudflareUrl = configuredCloudflareBaseUrl ?: "http://10.0.2.2:8787/"
            val debugPythonUrl = configuredPythonBaseUrl ?: "http://10.0.2.2:8000/"
            buildConfigField("String", "API_BASE_URL", "\"$debugCloudflareUrl\"")
            buildConfigField("String", "API_BASE_URL_CLOUDFLARE", "\"$debugCloudflareUrl\"")
            buildConfigField("String", "API_BASE_URL_PYTHON", "\"$debugPythonUrl\"")
            buildConfigField("String", "DATA_SOURCE_PROVIDER", "\"cloudflare\"")
            buildConfigField("String", "TENANT_KEY", "\"$configuredTenantKey\"")
            buildConfigField("String", "APP_ENVIRONMENT", "\"debug\"")
            buildConfigField("String", "DATABASE_NAME", "\"bakti_marsada_debug.db\"")
            buildConfigField("Boolean", "SIMULATION_ENABLED", "false")
            resValue("string", "app_name", "Bakti Marsada Dev")
            resValue(
                "string",
                "bakti_google_web_client_id",
                stringResourceValue(configuredDebugGoogleWebClientId)
            )
        }
        create("simulate") {
            initWith(getByName("debug"))
            matchingFallbacks += listOf("debug")
            applicationIdSuffix = ".simulate"
            versionNameSuffix = "-simulate"
            val simulateCloudflareUrl = configuredCloudflareBaseUrl ?: "https://simulate.baktimarsada.local/"
            val simulatePythonUrl = configuredPythonBaseUrl ?: "https://simulate-python.baktimarsada.local/"
            buildConfigField("String", "API_BASE_URL", "\"$simulateCloudflareUrl\"")
            buildConfigField("String", "API_BASE_URL_CLOUDFLARE", "\"$simulateCloudflareUrl\"")
            buildConfigField("String", "API_BASE_URL_PYTHON", "\"$simulatePythonUrl\"")
            buildConfigField("String", "DATA_SOURCE_PROVIDER", "\"$configuredDataSourceProvider\"")
            buildConfigField("String", "TENANT_KEY", "\"$configuredTenantKey\"")
            buildConfigField("String", "APP_ENVIRONMENT", "\"simulate\"")
            buildConfigField("String", "DATABASE_NAME", "\"bakti_marsada_simulate.db\"")
            buildConfigField("Boolean", "SIMULATION_ENABLED", "true")
            resValue("string", "app_name", "Bakti Marsada Simulate")
            resValue(
                "string",
                "bakti_google_web_client_id",
                stringResourceValue(configuredSimulateGoogleWebClientId)
            )
        }
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            val releaseCloudflareUrl = configuredCloudflareBaseUrl ?: "https://bakti-marsada-be.deomories.workers.dev/"
            val releasePythonUrl = configuredPythonBaseUrl ?: "https://python-bakti-marsada.example.com/"
            buildConfigField("String", "API_BASE_URL", "\"$releaseCloudflareUrl\"")
            buildConfigField("String", "API_BASE_URL_CLOUDFLARE", "\"$releaseCloudflareUrl\"")
            buildConfigField("String", "API_BASE_URL_PYTHON", "\"$releasePythonUrl\"")
            buildConfigField("String", "DATA_SOURCE_PROVIDER", "\"$configuredDataSourceProvider\"")
            buildConfigField("String", "TENANT_KEY", "\"$configuredTenantKey\"")
            buildConfigField("String", "APP_ENVIRONMENT", "\"production\"")
            buildConfigField("String", "DATABASE_NAME", "\"bakti_marsada.db\"")
            buildConfigField("Boolean", "SIMULATION_ENABLED", "false")
            resValue("string", "app_name", "Bakti Marsada")
            resValue(
                "string",
                "bakti_google_web_client_id",
                stringResourceValue(configuredReleaseGoogleWebClientId)
            )
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":ui"))
    implementation(project(":firebase-core"))
    implementation(project(":feature-roulette"))
    implementation(project(":data-source-cloudflare"))
    implementation(project(":data-source-python"))
    implementation(project(":data-source-firebase"))
    implementation(project(":data-source-simulate"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.moshi)
    implementation(libs.moshi.kotlin)
    implementation(libs.okhttp.logging)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)

    implementation(libs.androidx.security.crypto)
    implementation(libs.sqlcipher.android)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    add("simulateImplementation", libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.chucker)
    add("simulateImplementation", libs.chucker)
    releaseImplementation(libs.chucker.no.op)
}

val googleServicesConfigCandidates = listOf(
    "google-services.json",
    "src/debug/google-services.json",
    "src/release/google-services.json",
    "src/simulate/google-services.json"
)
val hasDebugGoogleServicesConfig = file("google-services.json").exists() || file("src/debug/google-services.json").exists()
val hasReleaseGoogleServicesConfig = file("google-services.json").exists() || file("src/release/google-services.json").exists()
val hasSimulateGoogleServicesConfig = file("src/simulate/google-services.json").exists()

if (googleServicesConfigCandidates.any { file(it).exists() }) {
    apply(plugin = "com.google.gms.google-services")

    tasks.matching { it.name == "processDebugGoogleServices" }.configureEach {
        enabled = hasDebugGoogleServicesConfig
    }
    tasks.matching { it.name == "processReleaseGoogleServices" }.configureEach {
        enabled = hasReleaseGoogleServicesConfig
    }
    tasks.matching { it.name == "processSimulateGoogleServices" }.configureEach {
        enabled = hasSimulateGoogleServicesConfig
    }
}
