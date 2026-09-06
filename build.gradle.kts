import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.shadow)
	// auto update dependencies with 'useLatestVersions' task
	alias(libs.plugins.use.latest.versions)
	alias(libs.plugins.ben.manes.versions)
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
	jvmToolchain(21)
}

version = "1.0.6"

dependencies {
	compileOnly(libs.jadx.core)
	implementation(platform(libs.kavaref.bom))
	implementation(libs.kavaref.core)
	implementation(libs.kavaref.jvm)
	implementation(libs.kavaref.extension)
}

tasks {
	val shadowJar = withType(ShadowJar::class) {
		archiveClassifier = ""
		minimize()
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}

	register<Copy>("dist") {
		description = ""
		group = "build"
		dependsOn(shadowJar)
		dependsOn(withType(Jar::class))

		from(shadowJar)
		into(layout.buildDirectory.dir("dist"))
	}
	register<Copy>("distDev") {
		description = ""
		group = "build dev"
		version = "$version-dev"
		dependsOn(shadowJar)
		dependsOn(withType(Jar::class))

		from(shadowJar)
		into(layout.buildDirectory.dir("distDev"))
	}
}

repositories {
	maven("https://jitpack.io")
	maven("https://api.xposed.info")
	maven("https://raw.githubusercontent.com/HighCapable/maven-repository/main/repository/releases")
	mavenCentral()
	google()
}
