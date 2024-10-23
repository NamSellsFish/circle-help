# Use an OpenJDK base image
FROM sapmachine/jdk:21

# Set working directory
WORKDIR /app

# Update package list and install wget
RUN apt-get update && apt-get install -y wget

# Download the JAR file from Google Drive
RUN wget -O app.jar "https://drive.google.com/uc?export=download&id=YOUR_FILE_ID"

# Run the JAR file
CMD ["java", "-jar", "app.jar"]
