/*
 * Copyright 2019-2023 Chair of Geoinformatics, Technical University of Munich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import com.github.benmanes.gradle.versions.updates.DependencyUpdatesTask
import org.jetbrains.dokka.gradle.DokkaMultiModuleTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

if (!JavaVersion.current().isJava11Compatible)
    logger.warn(
        "This build requires Java ${JavaVersion.VERSION_11}, " +
            "but version ${JavaVersion.current()} is currently in use."
    )

plugins {
    base
    java
    kotlin("jvm") version DependencyVersions.kotlin
    id(Plugins.versionChecker) version PluginVersions.versionChecker
    id(Plugins.ktlint) version PluginVersions.ktlint
    id(Plugins.dokka) version PluginVersions.dokka
    `maven-publish`
    signing
}

allprojects {
    group = Project.group
    version = Project.version
    val projectName = name

    apply(plugin = "java")
    apply(plugin = Plugins.ktlint)
    apply(plugin = "maven-publish")
    if (!project.hasProperty(BuildPropertyNames.skipSigning)) {
        apply(plugin = "signing")
    }

    repositories {
        mavenCentral()
        maven(url = MavenSources.sonatypeSnapshot)
        maven(url = MavenSources.jitpack)
    }

    dependencies {
        implementation(kotlin(Dependencies.kotlinStandardLibrary))
        implementation(Dependencies.kotlinCoroutines)

        implementation(Dependencies.arrowCore)
        implementation(Dependencies.arrowOptics)

        testImplementation(Dependencies.junit)
        testImplementation(Dependencies.assertj)
        testImplementation(Dependencies.mockk)
    }

    tasks.test {
        useJUnitPlatform()
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Javadoc> {
        options.encoding = "UTF-8"
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("0.45.2")
    }

    /*tasks.withType<DokkaTask>().configureEach {
        dokkaSourceSets {
            named("main") {
                moduleName.set("Dokka Gradle Example")
                includes.from("Module.md")
                sourceLink {
                    localDirectory.set(file("src/main/kotlin"))
                    remoteUrl.set(URL("https://github.com/Kotlin/dokka/tree/master/" +
                            "examples/gradle/dokka-gradle-example/src/main/kotlin"
                    ))
                    remoteLineSuffix.set("#L")
                }
            }
        }
    }*/

    publishing {
        apply(plugin = "maven-publish")

        publications {
            create<MavenPublication>("mavenJava") {
                groupId = Project.group
                artifactId = projectName
                version = Project.version

                from(components["java"])

                pom {
                    name.set(projectName)
                    description.set("Component '$projectName' of ${Project.name}.")
                    url.set("https://rtron.io")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                        }
                    }
                    developers {
                        developer {
                            id.set("b-schwab")
                            name.set("Benedikt Schwab")
                            email.set("benedikt.schwab@tum.de")
                        }
                    }
                    scm {
                        url.set("https://github.com/tum-gis/rtron")
                        connection.set("scm:git:git@github.com:tum-gis/rtron.git")
                        developerConnection.set("scm:git@github.com:tum-gis/rtron.git")
                    }
                }
            }
        }

        repositories {
            maven {
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl

                credentials {
                    username = System.getenv("MAVEN_USERNAME")
                    password = System.getenv("MAVEN_PASSWORD")
                }
            }
        }
    }

    if (!project.hasProperty(BuildPropertyNames.skipSigning)) {
        signing {
            sign(publishing.publications["mavenJava"])
        }
    }
}

dependencies {
    // make the root project archives configuration depend on every sub-project
    subprojects.forEach {
        implementation(it)
    }
    implementation(kotlin("stdlib-jdk8"))
}

tasks.withType<DependencyUpdatesTask> {
    rejectVersionIf {
        isNonStable(candidate.version)
    }
    checkForGradleUpdate = true
}
repositories {
    mavenCentral()
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "11"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "11"
}

fun configureDokka() {
    subprojects {
        plugins.apply("org.jetbrains.dokka")
    }

    tasks.withType<DokkaMultiModuleTask> {
    }
}

configureDokka()
