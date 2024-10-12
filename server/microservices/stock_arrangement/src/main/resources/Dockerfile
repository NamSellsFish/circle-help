FROM openjdk:21
ARG JAR_FILE=src/main/resources/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]