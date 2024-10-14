# Adoptium'dan Java 17 JDK imajını kullanıyoruz
FROM eclipse-temurin:17-jdk

# ARG ile JAR dosyasını belirliyoruz
ARG JAR_FILE=target/*.jar

# JAR dosyasını container'ın içine kopyalıyoruz
COPY ${JAR_FILE} BrokerageManagementAPI-0.0.1-SNAPSHOT.jar

# Çalıştırılacak portu belirtiyoruz
EXPOSE 8080

# Uygulamayı çalıştıracak komutu yazıyoruz
ENTRYPOINT ["java", "-jar", "/BrokerageManagementAPI-0.0.1-SNAPSHOT.jar"]
