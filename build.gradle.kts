plugins {
	alias(libs.plugins.kotlin)
	base
	`maven-publish`
}

version = property("mod_version") as String
group = property("maven_group") as String

base {
	archivesName = property("archives_base_name") as String
}

repositories {
	// Add repositories to retrieve artifacts from in here.
	// You should only use this when depending on other mods because
	// Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
	// See https://docs.gradle.org/current/userguide/declaring_repositories.html
	// for more information about repositories.
	mavenCentral()
	maven("https://repo.spongepowered.org/maven/") {
		name = "SpongePowered"
	}
	maven("https://maven.fabricmc.net/") {
		name = "FabricMC"
	}
}

val homeDir = System.getProperty("user.home") ?: error("Could not find user home directory, please set it manually.")
val gamePath = "$homeDir/.local/share/Steam/steamapps/common/Necesse" // Path to your game installation. Modify this if necessary.

val clientJar = "$gamePath/Necesse.jar"
val serverJar = "$gamePath/Server.jar"
require(file(clientJar).exists()) { error("Necesse client jar not found at the path: $gamePath. Please set the correct game path manually.") }
require(file(serverJar).exists()) { error("Necesse server jar not found at the path: $gamePath. Please set the correct game path manually.") }


dependencies {
	// To change the versions see the gradle.properties file
	implementation(libs.fabricLoader)
	api(libs.spongeMixin)
	api(libs.mixinExtras)
	implementation(libs.fabricLanguageKotlin)

	compileOnly(files(clientJar))
//  compileOnly(files(serverJar)) // For server mods
}

kotlin {
	jvmToolchain(17)
}

tasks {
	processResources {
		val props = mapOf(
			"mod_name" to project.property("mod_name") as String,
			"mod_id" to project.property("mod_id") as String,
			"version" to version,
			"necesse_version" to libs.versions.necesse.get(),
			"loader_version" to libs.versions.fabricLoader.get(),
			"fabric_language_kotlin_version" to libs.versions.fabricLanguageKotlin.get()
		)
		inputs.properties(props)
		filesMatching("fabric.mod.json") {
			expand(props)
		}
	}
	jar {
		from("LICENSE") {
			rename { "${it}_${base.archivesName.get()}" }
		}
	}
	register<Copy>("copyJar") {
		description = "Copies the resulting mod jar to the mods folder in the game directory."
		group = "build"
		dependsOn("jar")
		from(jar)
		into("$gamePath/mods")
	}
	register<JavaExec>("runClient") {
		description = "Runs the client from the game path."
		group = "run"
		dependsOn("copyJar", "createAppID")
		workingDir = file(gamePath)
		environment["__GL_THREADED_OPTIMIZATIONS"] = "0" // Workaround for game crashing on window actions for some nvidia drivers
		mainClass = "net.fabricmc.loader.launch.knot.KnotClient"
		classpath = files("$gamePath/lib/*")
		jvmArgs = listOf("-Dfabric.skipMcProvider=true")
		doLast {
			exec()
		}
		isIgnoreExitValue = true
	}
	register("createAppID") {
		group = "build"
		description = "Creates steam_appid.txt file in the game path."
		val appIdFile = file("$gamePath/steam_appid.txt")
		if (appIdFile.exists()) didWork = false
		else doLast { appIdFile.writeText("1169040") }
	}
}
