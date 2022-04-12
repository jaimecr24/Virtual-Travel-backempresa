FROM openjdk:11
COPY ./backempresa/target/backempresa-0.0.1-SNAPSHOT.jar /usr/local/lib/backempresa.jar
ENTRYPOINT ["java","-jar","/usr/local/lib/backempresa.jar"]