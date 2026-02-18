plugins {
    java
    application
}

group = "com.dc3"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.3")
    // Ensure JUnit Platform launcher is available at runtime
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.3")
}

application {
    // Use the desktop launcher MainLauncher
    mainClass.set("com.dc3.applet.LEDSign.MainLauncher")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }
}

tasks.withType<JavaCompile> {
    // Use UTF-8 encoding (standard for modern Java projects)
    options.encoding = "UTF-8"
}

tasks.test {
    useJUnitPlatform()
}
