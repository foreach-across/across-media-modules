FROM maven:3.5.0-jdk-8

RUN apt-get update && apt-get install -y build-essential && apt-get install -y ghostscript && apt-get install -y graphicsmagick