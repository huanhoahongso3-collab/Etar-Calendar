plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ec4j.editorconfig)
}

editorconfig {
	excludes = listOf("metadata/**", "**/*.xml", "**/*.webp")
}

kotlin {
    jvmToolchain(21)
}

android {
	namespace = "ws.xsoh.etar"
	testNamespace = "com.android.calendar.tests"
	compileSdk = 37

	defaultConfig {
		minSdk = 23
		targetSdk = 37
		versionCode = 57
		versionName = "1.0.57"
		applicationId = "ws.xsoh.etar"
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			// TODO: could be enabled for ProGuard minimization
			isMinifyEnabled = false
			resValue(
				"string",
				"search_authority",
				defaultConfig.applicationId + ".CalendarRecentSuggestionsProvider"
			)
		}

		debug {
			isMinifyEnabled = false

			applicationIdSuffix = ".debug"
			resValue(
				"string",
				"search_authority",
				defaultConfig.applicationId + ".debug.CalendarRecentSuggestionsProvider"
			)
		}
	}

	buildFeatures {
        buildConfig = true
		viewBinding = true
		resValues = true
	}

	/*
	 * To sign release build, create file gradle.properties in ~/.gradle/ with this content:
	 *
	 * signingStoreLocation=/home/key.store
	 * signingStorePassword=xxx
	 * signingKeyAlias=alias
	 * signingKeyPassword=xxx
	 */
	val signingStoreLocation: String? by project
	val signingStorePassword: String? by project
	val signingKeyAlias: String? by project
	val signingKeyPassword: String? by project

	if (
		signingStoreLocation != null &&
		signingStorePassword != null &&
		signingKeyAlias != null &&
		signingKeyPassword != null
	) {
		println("Found sign properties in gradle.properties! Signing build…")

		signingConfigs {
			named("release").configure {
				storeFile = File(signingStoreLocation!!)
				storePassword = signingStorePassword
				keyAlias = signingKeyAlias
				keyPassword = signingKeyPassword
			}
		}

		buildTypes.named("release").get().signingConfig = signingConfigs.named("release").get()
	} else {
		buildTypes.named("release").get().signingConfig = null
	}

	lint {
		lintConfig = file("lint.xml")
		// TODO: Resolve lint errors due to 363aa9c237a33e9e1a40bdfd9039dcaaa855a5a0
		abortOnError = false
	}

	compileOptions {
		isCoreLibraryDesugaringEnabled = true

		sourceCompatibility(JavaVersion.VERSION_21)
		targetCompatibility(JavaVersion.VERSION_21)
	}

	useLibrary("android.test.base")
	useLibrary("android.test.mock")

	androidResources {
		generateLocaleConfig = true
	}

}

// OneUI (SESL) design system replaces the stock AndroidX core/appcompat/
// fragment/preference/material modules app-wide, since they share package
// namespaces and cannot coexist with their SESL forks on the same classpath.
// Any transitive pull of the stock modules (via constraintlayout, work,
// lifecycle, etc.) must be excluded in favor of the sesl.* equivalents below.
val seslReplacedModules = listOf(
	"androidx.core" to "core",
	"androidx.core" to "core-ktx",
	"androidx.appcompat" to "appcompat",
	"androidx.fragment" to "fragment",
	"androidx.preference" to "preference",
	"androidx.preference" to "preference-ktx",
	"androidx.recyclerview" to "recyclerview",
	"androidx.drawerlayout" to "drawerlayout",
	"androidx.coordinatorlayout" to "coordinatorlayout",
	"androidx.customview" to "customview",
	"androidx.slidingpanelayout" to "slidingpanelayout",
	"androidx.swiperefreshlayout" to "swiperefreshlayout",
	"com.google.android.material" to "material",
)

configurations.all {
	seslReplacedModules.forEach { (group, module) ->
		exclude(group = group, module = module)
	}
}

dependencies {

	// Core
	implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.work.runtime)
    implementation(libs.androidx.concurrent.futures)
    testImplementation(libs.junit)
	testImplementation(libs.androidx.test.runner)

	coreLibraryDesugaring(libs.android.tools.desugar)

	// Coroutines
	implementation(libs.kotlinx.coroutines.android)

	// https://mvnrepository.com/artifact/org.dmfs/lib-recur
	implementation(libs.dmfs.lib.recur)

	// lifecycle
	implementation(libs.androidx.lifecycle.livedata)

	// OneUI design system (Samsung One UI 7 look and feel) — see
	// https://github.com/tribalfs/oneui-design. Requires GitHub Packages
	// credentials, see settings.gradle.kts.
	implementation(libs.oneui.design)
	implementation(libs.oneui.icons)
	implementation(libs.bundles.sesl.androidx)
	implementation(libs.sesl.material)
}
