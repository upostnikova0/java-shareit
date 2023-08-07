FROM amazoncorretto:11-alpine-jdk
ENV TZ="Asia/Almaty"
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
