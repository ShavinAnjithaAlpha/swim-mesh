# Use a lightweight Java 21 image
FROM eclipse-temurin:21-jre-alpine

# Set working directory
WORKDIR /app

# Copy the build artifact (Run 'mvn package' first!)
COPY target/swim-simulation-1.0-SNAPSHOT.jar app.jar

# Define the entrypoint to run our specific DockerNode class
ENTRYPOINT ["java", "-Dio.netty.transport.noNative=true","-jar", "app.jar"]