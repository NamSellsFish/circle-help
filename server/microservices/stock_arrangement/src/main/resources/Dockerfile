FROM openjdk:21

# Set working directory
WORKDIR /app

# Download the JAR file from Google Drive
RUN apt-get update && apt-get install -y wget \
    && wget -O app.jar "https://drive.google.com/file/d/1tJgthPviI0f1fO_X0gTtK21MPoQ1nYew/view?usp=sharing"

ENTRYPOINT ["java","-jar","/app.jar"]