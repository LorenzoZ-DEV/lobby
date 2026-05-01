plugins {
    kotlin("jvm") version "2.0.21"
    id("maven-publish")
    id("com.gradleup.shadow") version "9.0.0-beta8"
}

group = "xyz.lorenzzz"
version = "1.2.1"

val relocateGroup = "dev.lorenzzz.lobby"
val artifact = "Lobby"

kotlin {
    jvmToolchain(21)
    compilerOptions {
        javaParameters.set(true)
    }
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/repositories/snapshots")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repo.codemc.io/repository/nms/")
    maven("https://repo.lorenzzzz.xyz/releases")
    maven("https://repo.j4c0b3y.net/public/")
    maven("https://jitpack.io")
    maven("https://libraries.minecraft.net")
    maven("https://repo.helpch.at/releases")
    maven("https://repo.bluecolored.de/releases")
    maven("https://repo.mikeprimm.com")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.8-R0.1-SNAPSHOT")

    implementation("net.j4c0b3y:MenuAPI-core:1.5.5")
    implementation("net.j4c0b3y:MenuAPI-extras:1.5.5")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.github.cryptomorin:XSeries:13.0.0")

    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        exclude(group = "org.bukkit", module = "bukkit")
    }
    compileOnly("me.clip:placeholderapi:2.12.2")
    compileOnly("de.bluecolored:bluemap-api:2.7.7")
    compileOnly("org.projectlombok:lombok:1.18.36")
    annotationProcessor("org.projectlombok:lombok:1.18.36")
    compileOnly("com.zaxxer:HikariCP:5.1.0")
    compileOnly("org.xerial:sqlite-jdbc:3.46.1.0")
    compileOnly("com.mysql:mysql-connector-j:8.4.0")
}

tasks {
    compileKotlin {
        dependsOn(clean)
    }

    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    build {
        dependsOn(shadowJar)
        dependsOn(publish)
    }

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(project.configurations.runtimeClasspath.get())

        minimize {
            exclude(dependency("net.j4c0b3y:MenuAPI-core:.*"))
            exclude(dependency("net.j4c0b3y:MenuAPI-extras:.*"))
            exclude(dependency("co.aikar:acf-paper:.*"))
            exclude(dependency("co.aikar:acf-core:.*"))
            exclude(dependency("dev.lorenzz.libs:discord-api-common:.*"))
            exclude(dependency("dev.lorenzz.libs:discord-api-spigot:.*"))
        }

        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
        exclude("META-INF/maven/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/LICENSE*", "META-INF/NOTICE*", "META-INF/DEPENDENCIES*")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/kotlin-*.json")
        exclude("META-INF/versions/9/**", "META-INF/versions/11/**")
        exclude("**/module-info.class")
        exclude("*.html", "**/*.txt", "about.html")
        exclude("**/*.kotlin_builtins")
        exclude("**/*.kotlin_metadata")

        relocate("co.aikar.commands", "$relocateGroup.acf")
        relocate("co.aikar.locales", "$relocateGroup.locales")
        relocate("com.cryptomorin.xseries", "$relocateGroup.xseries")

        mergeServiceFiles()
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = relocateGroup
            artifactId = artifact
            from(components["java"])
        }
    }
}
