plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "MOys"
include("sdk1")
include("system1")
include("systemClasses")
include("sdk")