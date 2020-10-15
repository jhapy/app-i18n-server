FROM openjdk:15-jdk-slim-buster

MAINTAINER jHapy Lead Dev <jhapy@jhapy.org>

RUN apt-get update -y && \
    apt-get install -y wget curl && \
    apt-get autoclean

ENV JAVA_OPTS=""
ENV APP_OPTS=""

ADD devgcp.crt /tmp/
RUN $JAVA_HOME/bin/keytool -importcert -file /tmp/devgcp.crt -alias devgcp -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt
ADD ilemtest.crt /tmp/
RUN $JAVA_HOME/bin/keytool -importcert -file /tmp/ilemtest.crt -alias ilemtest -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

ADD target/app-i18n-server.jar /app/

ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Xverify:none -Djava.security.egd=file:/dev/./urandom -Dpinpoint.agentId=$(date | md5sum | head -c 24) -jar /app/app-i18n-server.jar $APP_OPTS"]

HEALTHCHECK --interval=30s --timeout=30s --retries=10 CMD curl -f http://localhost:9106/management/health || exit 1

EXPOSE 9006 9106