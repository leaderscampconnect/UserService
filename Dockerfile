FROM eclipse-temurin:17
COPY target/user-service-0.0.1-SNAPSHOT.jar user-service.jar
EXPOSE 8110
ENTRYPOINT ["java","-jar","user-service.jar"]