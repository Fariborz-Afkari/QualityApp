import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.qualityapp"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.qualityapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        val props = Properties()
        val localPropsFile = project.rootProject.file("local.properties")
        if (localPropsFile.exists()) {
            localPropsFile.inputStream().use { props.load(it) }
        }

        // مالک هر دو مخزن یکسان است
        buildConfigField("String", "GITHUB_USERNAME", "\"${props.getProperty("GITHUB_USERNAME", "Fariborz-Afkari")}\"")

        // مخزن کد (همین پروژه) — فقط برای نمایش/اطلاعات
        buildConfigField("String", "GITHUB_CODE_REPO", "\"${props.getProperty("GITHUB_CODE_REPO", "QualityApp")}\"")

        // مخزن داده — اینجا فایل‌های JSON خوانده و نوشته می‌شوند
        buildConfigField("String", "GITHUB_DATA_REPO", "\"${props.getProperty("GITHUB_DATA_REPO", "MetroQuality")}\"")
        buildConfigField("String", "GITHUB_BRANCH",    "\"${props.getProperty("GITHUB_BRANCH", "main")}\"")

        // مسیر پوشه‌ها داخل مخزن داده — اگر فایل‌ها در ریشه هستند، مقدار را خالی بگذارید
        buildConfigField("String", "QUESTIONNAIRES_PATH", "\"${props.getProperty("QUESTIONNAIRES_PATH", "questionnaires")}\"")
        buildConfigField("String", "SUBMISSIONS_PATH",   "\"${props.getProperty("SUBMISSIONS_PATH", "submissions")}\"")      buildConfigField("String", "SUBMISSIONS_PATH",   "\"${props.getProperty("SUBMISSIONS_PATH", "submissions")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")

    implementation("com.google.dagger:hilt-android:2.51.1")
    ksp("com.google.dagger:hilt-android-compiler:2.51.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.11.0")

    implementation("com.squareup.moshi:moshi:1.15.1")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.1")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.1")

    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}