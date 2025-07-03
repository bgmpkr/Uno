FROM hseeberger/scala-sbt:8u222_1.3.5_2.13.1

# Use archived Debian sources and remove stretch-updates
RUN sed -i 's/deb.debian.org/archive.debian.org/g' /etc/apt/sources.list && \
    sed -i 's|security.debian.org|archive.debian.org|g' /etc/apt/sources.list && \
    sed -i '/stretch-updates/d' /etc/apt/sources.list && \
    echo 'Acquire::Check-Valid-Until "false";' > /etc/apt/apt.conf.d/99no-check-valid-until && \
    apt-get update && \
    apt-get install -y libxrender1 libxtst6 libxi6

WORKDIR /uno

ADD . /uno

CMD ["java", "-jar", "uno.jar"]
