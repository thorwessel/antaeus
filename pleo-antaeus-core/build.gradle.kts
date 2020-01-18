plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    compile(project(":pleo-antaeus-models"))
    implementation("joda-time:joda-time:2.10.5")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
}