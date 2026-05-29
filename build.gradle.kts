plugins {
    kotlin("jvm") version "2.3.21"
    id("com.gradleup.shadow") version "8.3.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
}

group = "net.halflove"
version = "1.0.1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven ("https://jitpack.io")
}

dependencies {
    // Spigot API & PlaceholderAPI (Provided / CompileOnly)
    compileOnly("org.spigotmc:spigot-api:26.1.1-R0.1-SNAPSHOT")

    // Kotlin (Compile / Implementation)
    implementation(kotlin("stdlib-jdk8"))
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(25)
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    shadowJar {
        archiveFileName.set("WetWings-v${project.version}.jar")
    }

    runServer {
        minecraftVersion("26.1.2")
        jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
        pluginJars(shadowJar.flatMap { it.archiveFile })
    }
}