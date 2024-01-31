job("Build and publish") {
    container("Run gradle build", "eclipse-temurin:17") {
        kotlinScript { api ->
            api.gradlew("publish", "--stacktrace")
        }
    }
}