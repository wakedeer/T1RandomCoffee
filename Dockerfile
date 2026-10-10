FROM eclipse-temurin:25.0.4_7-jre-alpine
COPY /build/libs/*.jar app.jar
CMD java $JAVA_OPTS -jar app.jar
