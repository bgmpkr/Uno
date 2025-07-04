FROM openjdk:17-slim

RUN apt-get update && apt-get install -y \
    libxrender1 libxtst6 libxi6 libgtk-3-0 libx11-6 \
    libgl1-mesa-glx libglib2.0-0

WORKDIR /uno

ADD . /uno

COPY resources/ /resources/
COPY target/scala-3.3.1/Uno-assembly-0.1.0-SNAPSHOT.jar uno.jar

CMD ["java", "-jar", "uno.jar"]
