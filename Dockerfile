FROM openjdk:21-jdk-slim

WORKDIR /fullstacktest

COPY /build/libs/fullstacktest-latest.jar app.jar

ENV JAVA_OPTS=""

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

#PS ./gradlew clean
#PS ./gradlew bootJar
#PS docker build --no-cache -t fullstacktest:latest -f E:\INZYNIERKA\APKI\WersjeTestowe\fullstacktest\Dockerfile .