FROM eclipse-temurin:21-jdk  # or use 17-jdk if needed
WORKDIR /work/
COPY target/quarkus-app/lib/ /work/lib/
COPY target/quarkus-app/quarkus/ /work/quarkus/
COPY target/quarkus-app/app/ /work/app/
COPY target/quarkus-app/quarkus-run.jar /work/quarkus-run.jar
EXPOSE 8080
CMD ["java", "-jar", "quarkus-run.jar"]
