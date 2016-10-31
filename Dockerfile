FROM fabric8/java-jboss-openjdk8-jdk:1.2.1

ENV JAVA_APP_JAR stars-crawler-fat.jar
ENV JAVA_OPTIONS -Xmx256m

EXPOSE 8080

RUN chmod -R 777 /deployments/
ADD target/stars-crawler-fat.jar /deployments/
