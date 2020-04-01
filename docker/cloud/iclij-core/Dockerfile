FROM maven:3.6.3-jdk-13
MAINTAINER <rroart@gmail.com>
RUN mkdir /app
WORKDIR /app
WORKDIR /app/stockstat
COPY main main
COPY input input
COPY common common
COPY iclij iclij
COPY eureka eureka
COPY web web
COPY webr webr
COPY distribution distribution
COPY pom.xml pom.xml
RUN mvn -pl iclij/iclij-core -am install -Dmaven.test.skip=true
RUN cp -p iclij/iclij-core/target/stockstat-iclij-core-0.5-SNAPSHOT.jar /usr/local/bin

FROM openjdk:11-jdk
MAINTAINER <roart@nvg.ntnu.no>
ARG myserver
ARG myport
ARG mywebport
ARG myiconfig
ARG mydbserver
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYWEBPORT ${mywebport}
ENV MYICONFIG ${myiconfig}
ENV MYDBSERVER ${mydbserver}
RUN mkdir /usr/local/conf
ADD docker/cloud/iclij-core/${myiconfig} /usr/local/conf
WORKDIR /usr/local/bin
COPY --from=0 /usr/local/bin/stockstat-iclij-core-0.5-SNAPSHOT.jar .
RUN ln -s /tmp /usr/local/logs
CMD java -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:5432/stockstat -jar /usr/local/bin/stockstat-iclij-core-0.5-SNAPSHOT.jar

