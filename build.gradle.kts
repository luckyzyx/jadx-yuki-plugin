import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	kotlin("jvm") version "2.1.10"
	alias(libs.plugins.shadow)
	// auto update dependencies with 'useLatestVersions' task
	alias(libs.plugins.use.latest.versions)
	alias(libs.plugins.ben.manes.versions)
}

kotlin {
	jvmToolchain(17)
}

version = "1.0.2"

dependencies {
	compileOnly(libs.jadx.core)
	implementation(libs.yuki.reflection)
}

tasks {
	val shadowJar = withType(ShadowJar::class) {
		archiveClassifier = ""
		minimize()
	}

	register<Copy>("dist") {
		group = "build"
		dependsOn(shadowJar)
		dependsOn(withType(Jar::class))

		from(shadowJar)
		into(layout.buildDirectory.dir("dist"))
	}
	register<Copy>("distDev") {
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
	mavenCentral()
	google()
}
