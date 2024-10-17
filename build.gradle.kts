plugins {
    id("com.autonomousapps.dependency-analysis") version "2.1.4"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
    id("org.owasp.dependencycheck") version "10.0.4"
    id("org.springframework.boot") version "3.3.4"
    jacoco
    kotlin("jvm") version "2.0.21"
    kotlin("plugin.jpa") version "2.0.21"
    kotlin("plugin.noarg") version "2.0.21"
    kotlin("plugin.spring") version "2.0.21"
}

group = "com.authumn"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

val bootstrapIconsVersion = "1.11.3"
val bootstrapVersion = "5.3.3"
val htmxThymeleafVersion = "3.5.1"
val htmxVersion = "2.0.3"
val mockitoKotlinVersion = "5.4.0"
val mustacheVersion = "4.2.0"
val openapiVersion = "2.6.0"
val thymeleafSpringSecurityVersion = "3.1.2.RELEASE"
val webjarsLocatorVersion = "0.52"

dependencies {
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:$htmxThymeleafVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.liquibase:liquibase-core")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$openapiVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.session:spring-session-jdbc")
    implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6:$thymeleafSpringSecurityVersion")
    implementation("org.webjars.npm:bootstrap-icons:$bootstrapIconsVersion")
    implementation("org.webjars.npm:htmx.org:$htmxVersion")
    implementation("org.webjars.npm:mustache:$mustacheVersion")
    implementation("org.webjars:bootstrap:$bootstrapVersion")
    implementation("org.webjars:webjars-locator:$webjarsLocatorVersion")

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    testImplementation("io.projectreactor:reactor-test")
    testImplementation("net.sourceforge.htmlunit:htmlunit")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.mockito.kotlin:mockito-kotlin:$mockitoKotlinVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework:spring-webflux") // for WebTestClient
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:postgresql")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.isFork = true
    options.isIncremental = true
}

tasks
    .withType<Test> {
        useJUnitPlatform()
    }.configureEach {
        // forkEvery = 100
        maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
        reports.html.required = false
        reports.junitXml.required = false
    }

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
    configure<JacocoTaskExtension> {
        excludes = listOf("com/gargoylesoftware/**")
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        csv.required = true
        html.outputLocation = layout.buildDirectory.dir("jacocoHtml")
        xml.required = false
    }
}

configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    android.set(false)
    coloredOutput.set(true)
    debug.set(false)
    verbose.set(false)
    version.set("1.3.1")
}

configure<org.owasp.dependencycheck.gradle.extension.DependencyCheckExtension> {
    format =
        org.owasp.dependencycheck.reporting.ReportGenerator.Format.HTML
            .toString()
}

dependencyCheck {
    // https://nvd.nist.gov/developers/request-an-api-key
    nvd.apiKey = System.getenv("NVD_APIKEY") ?: ""
}
