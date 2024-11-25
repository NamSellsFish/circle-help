plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.3.3"
  //id("org.springframework.boot.aot") version "3.4.0"
  //id("org.graalvm.buildtools.native") version "0.10.3"
  id("io.spring.dependency-management") version "1.1.6"
  kotlin("plugin.jpa") version "1.9.25"
}

group = "circlehelp.server"
version = "0.0.1-SNAPSHOT"

java {

  toolchain {
    languageVersion = JavaLanguageVersion.of(21)
  }
}

allOpen {
  annotation("jakarta.persistence.Entity")
  annotation("jakarta.persistence.MappedSuperclass")
  annotation("jakarta.persistence.Embeddable")
}

configurations {
  compileOnly {
    extendsFrom(configurations.annotationProcessor.get())
  }
}

repositories {
  mavenCentral()
  maven ("https://repo.spring.io/milestone")
}

dependencies {

  implementation("org.springframework.boot:spring-boot-starter-actuator")

  implementation("jakarta.transaction:jakarta.transaction-api")
  implementation("io.reactivex.rxjava3:rxkotlin:3.0.1")
  implementation("io.reactivex.rxjava3:rxjava")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.session:spring-session-jdbc")
  implementation("org.springframework.session:spring-session-core")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.17.+")
  implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("org.springframework.boot:spring-boot-starter-web")
  //implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.jetbrains.kotlin:kotlin-reflect")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  compileOnly("org.projectlombok:lombok")

  runtimeOnly("com.mysql:mysql-connector-j")

  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

  testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.register<Exec>("cds") {
  environment("BP_JVM_CDS_ENABLED", "true")
}