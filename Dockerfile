FROM adoptopenjdk/openjdk11:alpine-jre
ADD https://github.com/microsoft/ApplicationInsights-Java/releases/download/3.2.3/applicationinsights-agent-3.2.3.jar /applicationinsights-agent.jar
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]

