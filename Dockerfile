FROM openjdk:11
WORKDIR /usr/app
EXPOSE 8080
ENTRYPOINT ["java", "-Xms64m", "-Xmx512m", "-jar", "jvs.jar"]
COPY target/jvs-*.jar jvs.jar