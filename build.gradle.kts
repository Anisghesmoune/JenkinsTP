plugins {
    id 'java'
}

group = 'com.example'
version = '1.0'

repositories {
    mavenCentral()
}

dependencies {
    testImplementation 'io.cucumber:cucumber-java:6.0.0'
    testImplementation 'io.cucumber:cucumber-junit:6.0.0'
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
}

test {
    useJUnitPlatform()
}
