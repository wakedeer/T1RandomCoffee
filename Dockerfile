FROM openjdk:11-jre-slim
ARG JAR_FILE=/build/libs/InnotechRandomCoffee-0.1.1.jar
COPY ${JAR_FILE} app.jar
CMD java $JAVA_OPTS -jar app.jar