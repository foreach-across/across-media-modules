# Also change in ImageServerTestContainer
FROM maven:3.5.0-jdk-8
RUN apt-get update && apt-get install -y ghostscript graphicsmagick