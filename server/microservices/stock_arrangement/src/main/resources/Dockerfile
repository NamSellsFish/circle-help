# Use an OpenJDK base image
FROM sapmachine/jdk:21

# Set working directory
WORKDIR /app

# Update package list and install wget
RUN apt-get update && apt-get install -y wget

# Download the JAR file from Google Drive
RUN wget -O app.jar "https://drive.google.com/file/d/1tJgthPviI0f1fO_X0gTtK21MPoQ1nYew/view?usp=sharing"

# Run the JAR file
CMD ["java", "-jar", "app.jar"]
