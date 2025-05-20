FROM openjdk:25-jdk-slim
EXPOSE 80:80
RUN mkdir /app
COPY build/libs/*.jar /app/saymyname.jar
ENTRYPOINT ["java","-jar","/app/saymyname.jar"]
