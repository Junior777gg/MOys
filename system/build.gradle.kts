plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.0"
}

group = "org.unstabledev"
version = "0.0.1-Alpha"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google() // на всякий случай
}

val javafxVersion = "21"
val platform = "linux" // For WSL

dependencies {
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-web:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-swing:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-media:$javafxVersion:$platform")
    testImplementation(kotlin("test"))
    implementation(project(":sdk"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jsoup:jsoup:1.17.2")
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-cio:2.3.6")

    //Window handling.
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.97")

    //MP3 playback support.
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
    //OGG/Vorbis playback support.
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")

    implementation("org.bytedeco:javacv-platform:1.5.10")
    implementation("uk.co.caprica:vlcj:4.7.0")

}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}