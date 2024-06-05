FROM public.ecr.aws/docker/library/openjdk:11
COPY /build/libs/*.jar app.jar
CMD java $JAVA_OPTS -jar app.jar
