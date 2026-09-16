plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.milehighweb.riftclash"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.milehighweb.riftclash"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

// GameState, PlayerState and CreatureInstance (in :game) are mutated in place by the
// engine and are compiled outside the Compose plugin, so the compiler can't prove their
// fields are immutable -- it correctly infers them as unstable. Compose's default "strong
// skipping" optimization skips recomposing a composable when an unstable argument's
// *reference* is unchanged, but the engine never replaces these objects, only mutates
// their fields (hero health, creature attack/health, mana) in place. That made stat
// displays (hero health gem, creature attack/health gems) silently go stale after combat
// -- e.g. attacking the enemy hero updated the real game state but the health gem never
// re-rendered. Disabling strong skipping restores the classic rule (unstable argument =>
// never skip => always recompose), which reads these mutated fields fresh every time.
composeCompiler {
    featureFlags.add(org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag.StrongSkipping.disabled())
}

dependencies {
    implementation(project(":game"))

    implementation(platform("androidx.compose:compose-bom:2024.09.02"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
