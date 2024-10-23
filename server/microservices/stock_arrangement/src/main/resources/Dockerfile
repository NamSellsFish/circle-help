# Use Ubuntu as the base image
FROM ubuntu:22.04

# Install OpenJDK 21 and wget
RUN apt-get update && apt-get install -y wget openjdk-21-jre

# Set working directory
WORKDIR /app

# Download the JAR file from Google Drive
RUN wget -O app.jar "https://drive.google.com/file/d/1tJgthPviI0f1fO_X0gTtK21MPoQ1nYew/view?usp=sharing"

# Run the JAR file
CMD ["java", "-jar", "app.jar"]
