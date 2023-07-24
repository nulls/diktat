@Suppress("DSL_SCOPE_VIOLATION", "RUN_IN_SCRIPT")  // https://github.com/gradle/gradle/issues/22797
plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-signing-default-configuration")
    alias(libs.plugins.kotlin.ksp)
    idea
}

project.description = "The main diktat ruleset"

dependencies {
    api(projects.diktatCommon)
    api(projects.diktatApi)
    implementation(libs.kotlin.stdlib.jdk8)
    implementation(libs.kotlin.compiler.embeddable)
    // guava is used for string case utils
    implementation(libs.guava)
    implementation(libs.kotlin.logging)
    testImplementation(projects.diktatTestFramework)
    testImplementation(projects.diktatKtlintEngine)
    testImplementation(libs.log4j2.slf4j2)
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.junit.platform.suite)
    testImplementation(libs.assertj.core)
    testImplementation(libs.mockito)
    // is used for simplifying boolean expressions
    implementation(libs.jbool.expressions)

    // generating
    compileOnly(projects.diktatDevKsp)
    ksp(projects.diktatDevKsp)
    testImplementation(libs.kotlin.reflect)
}

project.afterEvaluate {
    tasks.named("kspKotlin") {
        // not clear issue that :kspKotlin is up-to-date, but generated files are missed
        outputs.upToDateWhen { false }
    }
    tasks.named("test") {
        dependsOn(tasks.named("kspKotlin"))
    }
}