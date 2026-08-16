import org.gradle.api.initialization.resolve.RepositoriesMode

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()

        // OneUI design system (SESL) — GitHub Packages, requires a PAT with
        // read:packages scope. Provide credentials via env vars GPR_USER /
        // GPR_TOKEN (or Gradle properties gprUser / gprToken), e.g. from
        // GitHub Actions secrets USERNAME / TOKEN.
        val gprUser = providers.gradleProperty("gprUser").orElse(providers.environmentVariable("GPR_USER")).orNull
        val gprToken = providers.gradleProperty("gprToken").orElse(providers.environmentVariable("GPR_TOKEN")).orNull
        if (gprUser != null && gprToken != null) {
            listOf("oneui-design", "sesl-androidx", "sesl-material-components-android").forEach { repo ->
                maven {
                    url = uri("https://maven.pkg.github.com/tribalfs/$repo")
                    credentials {
                        username = gprUser
                        password = gprToken
                    }
                }
            }
        }
    }
}
rootProject.name = "Etar-Calendar"
include(":app")
