FROM public.ecr.aws/docker/library/openjdk:11-jre-slim
COPY /build/libs/*.jar app.jar
CMD java $JAVA_OPTS -jar app.jar
