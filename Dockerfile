# Also change in ImageServerTestContainer
FROM maven:3.8.7-eclipse-temurin-8
RUN apt-get update && apt-get install -y ghostscript graphicsmagick