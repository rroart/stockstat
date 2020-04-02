FROM maven:3.6.3-jdk-13
MAINTAINER <rroart@gmail.com>
RUN mkdir /app
WORKDIR /app
COPY main main
COPY input input
COPY common common
COPY iclij iclij
COPY eureka eureka
COPY web web
COPY webr webr
COPY distribution distribution
COPY pom.xml pom.xml
RUN mvn -pl main/core -am install -Dmaven.test.skip=true
RUN cp -p main/core/target/stockstat-core-0.5-SNAPSHOT.jar /usr/local/bin

FROM openjdk:10-jdk
MAINTAINER <roart@nvg.ntnu.no>
ARG mywebport
ARG myconfig
ARG myenv
ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
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
COPY --from=0 /usr/local/bin/stockstat-core-0.5-SNAPSHOT.jar .
RUN ln -s /tmp /usr/local/logs
USER root
CMD . ${MYENV}; envsubst < /usr/local/conf/stockstat.xml.tmpl > /usr/local/conf/stockstat.xml; SPARK_DRIVER_HOST=`hostname -i` java -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/stockstat-core-0.5-SNAPSHOT.jar

