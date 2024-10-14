# Using Java 17 JDK from Adoptium
FROM eclipse-temurin:17-jdk

# Specify the JAR file using ARG
ARG JAR_FILE=target/*.jar

# Copy the JAR file into the container
COPY ${JAR_FILE} BrokerageManagementAPI-0.0.1-SNAPSHOT.jar

# Specify the port that the application will run on
EXPOSE 8080

# Write the command to run the application
ENTRYPOINT ["java", "-jar", "/BrokerageManagementAPI-0.0.1-SNAPSHOT.jar"]