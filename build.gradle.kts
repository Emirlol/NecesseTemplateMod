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

val homeDir = System.getProperty("user.home")
val gamePath = "$homeDir/.local/share/Steam/steamapps/common/Necesse"

dependencies {
    // To change the versions see the gradle.properties file
    implementation(libs.fabricLoader)
    api(libs.spongeMixin)
	implementation(libs.fabricLanguageKotlin)

    compileOnly(files("$gamePath/Necesse.jar"))
//  compileOnly(files("$gamePath/Server.jar")) // For server mods
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
}

kotlin {
	jvmToolchain(17)
}
