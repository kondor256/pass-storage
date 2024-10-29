FROM openjdk:17-jdk-slim

ARG PROJECTVER
ENV PROJECTVER=$PROJECTVER

ADD /target/*.jar /app/
#RUN mv /app/passStorage-$PROJECTVER.jar /app/passStorage.jar

WORKDIR /app
RUN cd /app

#run the spring boot application
#ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar","/app/passStorage.jar"]
ENTRYPOINT sh -c "java -Djava.security.egd=file:/dev/./urandom -jar /app/passStorage-${PROJECTVER}.jar"
EXPOSE 8080/tcp