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
    implementation("ch.qos.logback:logback-classic:1.2.3")
    implementation("com.uchuhimo:konf:0.22.1")
    implementation("com.ealva", "ealvatag", "0.4.3")
    implementation("org.jmdns:jmdns:3.5.5")

}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
