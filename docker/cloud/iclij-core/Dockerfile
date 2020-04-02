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
ARG myconfig
ARG myenv
ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYWEBPORT ${mywebport}
ENV MYCONFIG ${myconfig}
ENV MYENV ${myenv}
ENV MYDBSERVER ${mydbserver}
ENV MYDBSERVERPORT ${mydbserverport}
ENV MYDBSERVERDB ${mydbserverdb}
RUN mkdir /usr/local/conf
ADD ${myconfig} /usr/local/conf
ADD ${myenv} /usr/local/conf
WORKDIR /usr/local/bin
COPY --from=0 /usr/local/bin/stockstat-iclij-core-0.5-SNAPSHOT.jar .
RUN ln -s /tmp /usr/local/logs
CMD . ${MYENV}; envsubst < /usr/local/conf/iclij.xml.tmpl > /usr/local/conf/iclij.xml; java -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/stockstat-iclij-core-0.5-SNAPSHOT.jar

