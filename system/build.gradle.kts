plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.0"
}

group = "org.unstabledev"
version = "0.0.1-Alpha"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://raw.githubusercontent.com/jcefmaven/jcefmaven/master/repo")
}

val javafxVersion = "21"
val platform = "linux"

dependencies {
    //JavaFX.
    implementation("org.openjfx:javafx-base:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-web:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-swing:$javafxVersion:$platform")
    implementation("org.openjfx:javafx-media:$javafxVersion:$platform")
    //Kotlin utils.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    //Networking.
    implementation("io.ktor:ktor-client-core:2.3.6")
    implementation("io.ktor:ktor-client-cio:2.3.6")
    //Skiko.
    implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-x64:0.7.97")
    //implementation("org.jetbrains.skiko:skiko-awt-runtime-linux-aarch64:0.7.97")
    //MP3 playback support.
    implementation("com.googlecode.soundlibs:mp3spi:1.9.5.4")
    //OGG/Vorbis playback support.
    implementation("com.googlecode.soundlibs:vorbisspi:1.0.3.3")
    //FFMpeg video playback support.
    implementation("org.bytedeco:javacv-platform:1.5.10")
    //JCEF (browser+WebView).
    implementation("me.friwi:jcefmaven:146.0.10")
    //Projects implementation.
    testImplementation(kotlin("test"))
    implementation(project(":sdk"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}