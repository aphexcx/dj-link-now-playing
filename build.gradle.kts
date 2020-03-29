import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.61"
}

group = "cx.aphex"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.deepsymmetry:beat-link:0.6.1")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}