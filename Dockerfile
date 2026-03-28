FROM openjdk:11.0.16-jre-slim
COPY /build/libs/*.jar app.jar
CMD java $JAVA_OPTS -jar app.jar
