plugins {
    kotlin("jvm")
}

kotlinProject()

dependencies {
    implementation(project(":pleo-antaeus-data"))
    compile(project(":pleo-antaeus-models"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.3")
}