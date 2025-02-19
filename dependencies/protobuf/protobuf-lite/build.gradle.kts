import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.io.File

plugins {
    `java-base`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "4.0.3" apply false
}

repositories {
    mavenCentral()
}

val relocatedProtobuf by configurations.creating
val relocatedProtobufSources by configurations.creating

val protobufVersion: String by rootProject.extra

val renamedSources = "$buildDir/renamedSrc/"
val outputJarsPath = "$buildDir/libs"

dependencies {
    relocatedProtobuf("com.google.protobuf:protobuf-javalite:$protobufVersion")
    relocatedProtobufSources("com.google.protobuf:protobuf-javalite:$protobufVersion:sources")
}

val prepare = tasks.register<ShadowJar>("prepare") {
    destinationDirectory.set(File(outputJarsPath))
    archiveVersion.set(protobufVersion)
    archiveClassifier.set("")
    from(
        provider {
            relocatedProtobuf.files.find { it.name.startsWith("protobuf-javalite") }?.canonicalPath
        }
    )

    relocate("com.google.protobuf", "org.jetbrains.kotlin.protobuf" ) {
        exclude("META-INF/maven/com.google.protobuf/protobuf-javalite/pom.properties")
    }
}

artifacts.add("default", prepare)

val relocateSources = task<Copy>("relocateSources") {
    from(
        provider {
            zipTree(relocatedProtobufSources.files.find { it.name.startsWith("protobuf-javalite") && it.name.endsWith("-sources.jar") }
                        ?: throw GradleException("sources jar not found among ${relocatedProtobufSources.files}"))
        }
    )

    into(renamedSources)

    filter { it.replace("com.google.protobuf", "org.jetbrains.kotlin.protobuf") }
}

val prepareSources = task<Jar>("prepareSources") {
    destinationDirectory.set(File(outputJarsPath))
    archiveVersion.set(protobufVersion)
    archiveClassifier.set("sources")
    from(relocateSources)
}

artifacts.add("default", prepareSources)

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(prepare)
            artifact(prepareSources)
        }
    }

    repositories {
        maven {
            url = uri("${rootProject.buildDir}/internal/repo")
        }

        maven {
            name = "kotlinSpace"
            url = uri("https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-dependencies")
            credentials(org.gradle.api.artifacts.repositories.PasswordCredentials::class)
        }
    }
}
