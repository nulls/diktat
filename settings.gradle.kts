rootProject.name = "diktat"

dependencyResolutionManagement {
    repositories {
        file("$rootDir/build/diktat-snapshot")
            .takeIf { it.exists() }
            ?.run {
                maven {
                    url = this@run.toURI()
                }
            }
        mavenCentral()
    }
}

pluginManagement {
    repositories {
        file("$rootDir/build/diktat-snapshot")
            .takeIf { it.exists() }
            ?.run {
                maven {
                    url = this@run.toURI()
                }
            }
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.14"
    // starting from Gradle 8, it's needed to configure a repo from which to take Java for a toolchain
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}

includeBuild("gradle/plugins")
include("diktat-api")
include("diktat-common")
include("diktat-ktlint-engine")
include("diktat-gradle-plugin")
include("diktat-maven-plugin")
include("diktat-rules")
include("diktat-ruleset")
include("diktat-test-framework")
include("diktat-dev-ksp")
include("diktat-cli")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

gradleEnterprise {
    @Suppress("AVOID_NULL_CHECKS")
    if (System.getenv("CI") != null) {
        buildScan {
            publishAlways()
            termsOfServiceUrl = "https://gradle.com/terms-of-service"
            termsOfServiceAgree = "yes"
        }
    }
}