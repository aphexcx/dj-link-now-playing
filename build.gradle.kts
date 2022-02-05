plugins {
    application
    kotlin("jvm") version "1.6.0"
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
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.0")
    implementation("org.deepsymmetry:beat-link:0.6.3")
    implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.1")
    implementation("com.uchuhimo:konf:1.1.2")
    implementation("com.ealva", "ealvatag", "0.4.3")
    implementation("org.jmdns:jmdns:3.5.7")

    val ver_rxjava = "3.1.3"
    implementation("io.reactivex.rxjava3:rxjava:$ver_rxjava")
    implementation("com.jakewharton.rxrelay3:rxrelay:3.0.1")

    val ver_okhttp = "4.9.3"
    val ver_retrofit = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$ver_retrofit")
    implementation("com.squareup.retrofit2:converter-gson:$ver_retrofit")
    implementation("com.github.akarnokd:rxjava3-retrofit-adapter:3.0.0")
    implementation("com.squareup.okhttp3:okhttp:$ver_okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor:$ver_okhttp")

}
