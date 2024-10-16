FROM eclipse-temurin:21-jre-alpine AS builder
WORKDIR /extracted
COPY ./build/libs/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract
FROM eclipse-temurin:21-jre-alpine
RUN apk --no-cache add curl
WORKDIR /application
COPY --from=builder extracted/dependencies/ ./
COPY --from=builder extracted/spring-boot-loader/ ./
COPY --from=builder extracted/snapshot-dependencies/ ./
COPY --from=builder extracted/application/ ./
EXPOSE 9000
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
