import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	`java-library`
	kotlin("jvm") version "2.1.10"

	alias(libs.plugins.shadow)

	// auto update dependencies with 'useLatestVersions' task
	alias(libs.plugins.use.latest.versions)
	alias(libs.plugins.ben.manes.versions)
}

kotlin {
	jvmToolchain(17)
}

version = "1.0.1"

tasks {
	val shadowJar = withType(ShadowJar::class) {
		archiveClassifier.set("") // remove '-all' suffix
	}

	// copy result jar into "build/dist" directory
	register<Copy>("dist") {
		group = "jadx-yuki-plugin"
		dependsOn(shadowJar)
		dependsOn(withType(Jar::class))

		from(shadowJar)
		into(layout.buildDirectory.dir("dist"))
	}
	register<Copy>("distDev") {
		group = "jadx-yuki-plugin"
		version = "$version-dev"
		dependsOn(shadowJar)
		dependsOn(withType(Jar::class))

		from(shadowJar)
		into(layout.buildDirectory.dir("distDev"))
	}
}

dependencies {
	implementation(libs.jadx.core)
	implementation(kotlin("stdlib-jdk8"))
}

repositories {
	mavenCentral()
	maven("https://jitpack.io")
	maven("https://api.xposed.info")
	maven("https://maven.aliyun.com/repository/public")
	maven("https://maven.aliyun.com/repository/jcenter")
	maven("https://maven.aliyun.com/repository/google")
	maven("https://maven.aliyun.com/repository/gradle-plugin")
	maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
	google()
}

