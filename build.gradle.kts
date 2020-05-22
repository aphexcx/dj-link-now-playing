import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.3.72"
}

application {
    mainClass.set("cx.aphex.now_playing.MainKt")
}

group = "cx.aphex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.deepsymmetry:beat-link:0.6.2")
//    implementation("com.natpryce:konfig:1.6.10.0")
    implementation("com.uchuhimo:konf:0.22.1")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
