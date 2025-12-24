dependencies {
    implementation(project(":common"))
    
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // WebClient for calling other services
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Testing
    testImplementation("org.mockito:mockito-core")
}

