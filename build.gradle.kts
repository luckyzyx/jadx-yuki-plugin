import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
	`java-library`
	kotlin("jvm") version "2.1.10"

	id("com.github.johnrengelman.shadow") version "8.1.1"

	// auto update dependencies with 'useLatestVersions' task
	id("se.patrikerdes.use-latest-versions") version "0.2.18"
	id("com.github.ben-manes.versions") version "0.51.0"
}

kotlin {
	jvmToolchain(11)
}

val isDev = false
version = "1.0.1" + if (isDev) "-dev" else ""

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
}

dependencies {
	val jadxVersion = "1.5.1"
	val isJadxSnapshot = jadxVersion.endsWith("-SNAPSHOT")

	// use compile only scope to exclude jadx-core and its dependencies from result jar
	implementation("io.github.skylot:jadx-core:$jadxVersion") {
		isChanging = isJadxSnapshot
	}

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

