plugins {
    kotlin("jvm")
}

group = "org.unstabledev"
version = "0.0.1-Alpha"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

kotlin {
    jvmToolchain(17)
}

tasks.test {
    useJUnitPlatform()
}