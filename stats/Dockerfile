FROM fabric8/java-jboss-openjdk8-jdk:1.2.1

ENV JAVA_APP_JAR stars-crawler-stats-fat.jar
ENV JAVA_OPTIONS -Xmx256m -Djava.net.preferIPv4Stack=true
ENV OPENSHIFT_KUBE_PING_NAMESPACE stars-crawler

EXPOSE 8888

RUN chmod -R 777 /deployments/
ADD target/stars-crawler-stats-fat.jar /deployments/
