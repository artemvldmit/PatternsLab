FROM eclipse-temurin:17-jdk
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN apt-get update && apt-get install -y maven && mvn -q -f pom.xml package
EXPOSE 8080 8081
CMD ["java", "-jar", "target/shop-backend-1.0.0-jar-with-dependencies.jar"]
