import com.google.firebase.appdistribution.gradle.firebaseAppDistribution
import java.io.FileInputStream
import java.util.Properties

val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")

if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}

val auth0ClientId =
    localProperties.getProperty("AUTH0_CLIENT_ID") ?: System.getenv("AUTH0_CLIENT_ID") ?: ""
val auth0Audience =
    localProperties.getProperty("AUTH0_AUDIENCE")
        ?: System.getenv("AUTH0_AUDIENCE")
        ?: "https://api.example.com/"
val apiBaseUrl =
    localProperties.getProperty("API_BASE_URL")
        ?: System.getenv("API_BASE_URL")
        ?: "https://api.example.com/"

val firebaseAppId = localProperties.getProperty("FIREBASE_APP_ID") ?: System.getenv("FIREBASE_APP_ID") ?: ""
val firebaseGroups = localProperties.getProperty("FIREBASE_GROUPS") ?: System.getenv("FIREBASE_GROUPS") ?: "MyAccounts"
val firebaseReleaseNotes =
    localProperties.getProperty("FIREBASE_RELEASE_NOTES")
        ?: System.getenv("FIREBASE_RELEASE_NOTES")
        ?: "Manual build upload via Gradle"
val firebaseServiceCredentials =
    localProperties.getProperty("FIREBASE_SERVICE_CREDENTIALS_FILE")
        ?: System.getenv("FIREBASE_SERVICE_CREDENTIALS_FILE")
        ?: ""

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.github.hierynomus.license") version "0.16.1"
    id("org.jetbrains.kotlinx.kover")
    id("com.google.firebase.appdistribution")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = "com.example.notesapp"
    compileSdk = 34

    signingConfigs {
        getByName("debug") {
            storeFile = file("$rootDir/etc/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    defaultConfig {
        applicationId = "com.example.notesapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
        manifestPlaceholders["auth0Domain"] = "dev-9sa8k5kv.us.auth0.com"
        manifestPlaceholders["auth0Scheme"] = "notesapp"
        resValue("string", "auth0_client_id", auth0ClientId)
        resValue("string", "auth0_audience", auth0Audience)
        buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")
    }

    buildTypes {
        debug {
            versionNameSuffix = "-Debug"
            firebaseAppDistribution {
                artifactType = "APK"
                appId = firebaseAppId
                groups = firebaseGroups
                releaseNotes = firebaseReleaseNotes
                if (firebaseServiceCredentials.isNotEmpty()) {
                    serviceCredentialsFile = firebaseServiceCredentials
                }
            }
        }
        release {
            versionNameSuffix = "-Release"
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            firebaseAppDistribution {
                artifactType = "APK"
                appId = firebaseAppId
                groups = firebaseGroups
                releaseNotes = firebaseReleaseNotes
                if (firebaseServiceCredentials.isNotEmpty()) {
                    serviceCredentialsFile = firebaseServiceCredentials
                }
            }
        }
    }
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE*"
            excludes += "/META-INF/NOTICE*"
            excludes += "/META-INF/DEPENDENCIES"
        }
    }
    lint {
        abortOnError = true
        checkReleaseBuilds = false
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.tracing:tracing:1.2.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.lifecycle:lifecycle-service:2.8.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("com.auth0.android:auth0:2.10.2")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    implementation("com.google.mlkit:genai-summarization:1.0.0-beta1")
    implementation("com.google.ai.edge.aicore:aicore:0.0.1-exp01")
    implementation("com.google.mediapipe:tasks-text:0.10.35")
    ksp("androidx.room:room-compiler:2.6.1")

    // Hilt
    implementation("com.google.dagger:hilt-android:2.51")
    ksp("com.google.dagger:hilt-android-compiler:2.51")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    ksp("androidx.hilt:hilt-compiler:1.2.0")

    // Security
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    implementation("io.coil-kt:coil-compose:2.6.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("org.robolectric:robolectric:4.12.2")
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("org.json:json:20240303")
    testImplementation("androidx.test.ext:junit:1.2.1")

    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.06.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("io.mockk:mockk-android:1.13.10")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kover {
    reports {
        filters {
            excludes {
                // Framework-bound MediaRecorder lifecycle is verified by connected service tests.
                packages("com.example.notesapp.data.voice.service")
                classes(
                    "com.example.notesapp.MainActivity",
                    "com.example.notesapp.NotesApplication",
                    "com.example.notesapp.auth.*",
                    "com.example.notesapp.ui.*.screen.*",
                    "com.example.notesapp.navigation.*",
                    "com.example.notesapp.ui.*.components.*",
                    "com.example.notesapp.ui.theme.*",
                    "*_Impl",
                    "*_Impl$*",
                    "*_Factory",
                    "*_Factory$*",
                    "*_HiltModules*",
                    "*Hilt_*",
                    "*_MembersInjector",
                    "com.example.notesapp.di.*",
                    "com.example.notesapp.data.local.AppDatabase*"
                )
            }
        }
        total {
            log { onCheck = true }
            verify {
                rule("Overall coverage") {
                    bound { minValue = 80 }
                }
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/detekt.yml"))
}

configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
    }
}
