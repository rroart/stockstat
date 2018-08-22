FROM openjdk:8-jdk
MAINTAINER <roart@nvg.ntnu.no>
ADD files/stockstat-web-0.5-SNAPSHOT.war /usr/local/bin
WORKDIR /usr/local/bin
RUN ln -s /tmp /usr/local/logs
CMD java -jar jetty-runner-9.4.10.RC1.jar --port 8180 /usr/local/bin/stockstat-web-0.5-SNAPSHOT.war

