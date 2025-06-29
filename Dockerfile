FROM hseeberger/scala-sbt:11.0.16_3.3.1_1.9.9
WORKDIR /uno
ADD . /uno
CMD sbt test