import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidxRoom)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    jvm()
    
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // Navigation
            implementation(libs.androidx.navigation.compose)
            // Koin DI
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            // Room & SQLite
            implementation(libs.androidx.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            // DataStore (Settings)
            implementation(libs.androidx.datastore.preferences)
            // Coil 3 (Ảnh) & Compottie (Lottie)
            implementation(libs.coil.compose)
            implementation(libs.compottie)
            implementation(libs.compottie.resources)
            // Coroutines & Serialization
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.serialization.json)
            // Sentry KMP Crash Reporting
            implementation(libs.sentry.kmp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}
android {
    namespace = "com.thupo.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

buildkonfig {
    packageName = "com.thupo"
    exposeObjectWithName = "BuildKonfig"
    defaultConfigs {
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "BASE_URL", "http://10.0.2.2:8080/api")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "APP_VERSION", "1.0.0")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN, "IS_DEBUG", "true")
        buildConfigField(com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING, "SENTRY_DSN", "")
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.androidx.room.compiler)
    add("kspJvm", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
}

afterEvaluate {
    dependencies {
        add("kspAndroid", libs.androidx.room.compiler)
    }
}