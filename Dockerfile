FROM openjdk:17

ARG PROJECTVER

ADD /target/*.jar /app/
RUN mv /app/passStorage-$PROJECTVER.jar /app/passStorage.jar

WORKDIR /app
RUN cd /app

#run the spring boot application
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar","/app/passStorage.jar"]
EXPOSE 8080/tcp