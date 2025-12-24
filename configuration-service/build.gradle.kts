dependencies {
    implementation(project(":common"))
    
    // Spring Boot Web
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // MongoDB for storing configurations
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    
    // WebClient for calling catalog-service
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    
    // Testing
    testImplementation("org.testcontainers:mongodb:1.20.4")
    testImplementation("org.testcontainers:junit-jupiter:1.20.4")
}

