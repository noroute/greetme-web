FROM java:8-jre

EXPOSE 8080

ADD target/greetme-web-0.0.1-SNAPSHOT.jar /greetme-web.jar

CMD ["java", "-jar", "/greetme-web.jar"]