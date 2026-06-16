plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

fun versionBanner(): String {
    return try {
        val process = Runtime.getRuntime().exec(arrayOf("git", "rev-parse", "--short=8", "HEAD"))
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0) output else "Unknown"
    } catch (e: Exception) {
        System.getenv("GITHUB_SHA")?.take(8) ?: "Unknown"
    }
}

fun builder(): String {
    return try {
        val process = Runtime.getRuntime().exec(arrayOf("git", "config", "user.name"))
        val output = process.inputStream.bufferedReader().readText().trim()
        process.waitFor()
        if (process.exitValue() == 0) output else "Unknown"
    } catch (e: Exception) {
        System.getenv("GITHUB_ACTOR") ?: "Unknown"
    }
}

val git: String = versionBanner()
val builder: String = builder()
ext["git_version"] = git
ext["builder"] = builder

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
    }

    tasks.processResources {
        filteringCharset = "UTF-8"
        
        // ⬇️⬇️⬇️ ГЛАВНОЕ ИСПРАВЛЕНИЕ ⬇️⬇️⬇️
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        filesMatching("custom-nameplates.properties") {
            expand(rootProject.properties)
        }

        filesMatching(listOf("*.yml", "*/*.yml")) {
            expand(
                Pair("project_version", rootProject.properties["project_version"]!!),
                Pair("config_version", rootProject.properties["config_version"]!!)
            )
        }
    }
}
