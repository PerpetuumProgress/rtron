import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id(Plugins.xjc) version PluginVersions.xjc
}

kotlinProject()

dependencies {
    // utility layer components
    implementation(project(ProjectComponents.standard))
    implementation(project(ProjectComponents.inputOutput))
    implementation(project(ProjectComponents.model))

    // standard libraries
    implementation(Dependencies.arrowCore)

    // object creation libraries
    implementation(Dependencies.jakartaXmlBindApi)
    implementation(Dependencies.sunJaxbImpl)
    xjc(Dependencies.jakartaXmlBindApi)
    xjc(Dependencies.jaxbRuntime)
    xjc(Dependencies.jaxbXjc)
    xjc(Dependencies.jakartaActivationApi)

    // object mapping libraries
    implementation(Dependencies.mapstruct)
    kapt(Dependencies.mapstructProcessor)

    // geo libraries
    implementation(Dependencies.citygml4j)
}

tasks.withType<KotlinCompile> {
    dependsOn("${ProjectComponents.readerWriter}:xjcGeneration")
}

xjcGeneration {

    defaultAdditionalXjcOptions = mapOf("encoding" to "UTF-8")

    schemas {
        create("opendrive11") {
            schemaDir = "opendrive11/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive11.xjb"
            javaPackageName = "org.asam.opendrive11"
        }

        create("opendrive12") {
            schemaDir = "opendrive12/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive12.xjb"
            javaPackageName = "org.asam.opendrive12"
        }

        create("opendrive13") {
            schemaDir = "opendrive13/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive13.xjb"
            javaPackageName = "org.asam.opendrive13"
        }

        create("opendrive14") {
            schemaDir = "opendrive14/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive14.xjb"
            javaPackageName = "org.asam.opendrive14"
        }

        create("opendrive15") {
            schemaDir = "opendrive15/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive15.xjb"
            javaPackageName = "org.asam.opendrive15"
        }

        create("opendrive16") {
            schemaDir = "opendrive16/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive16.xjb"
            javaPackageName = "org.asam.opendrive16"
        }

        create("opendrive17") {
            schemaDir = "opendrive17/"
            schemaRootDir = "$projectDir/src/main/resources/schemas/"
            bindingFile = "src/main/resources/schemas/opendrive17.xjb"
            javaPackageName = "org.asam.opendrive17"
        }
    }
}

tasks.named<Jar>("sourcesJar") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<Javadoc> {
    options {
        this as StandardJavadocDocletOptions
        // disabled due to auto generated code throwing warnings/errors
        addBooleanOption("Xdoclint:none", true)
    }
}
