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
ARG mydbserver
ENV MYWEBPORT ${mywebport}
ENV MYCONFIG ${myconfig}
ENV MYDBSERVER ${mydbserver}
RUN mkdir /usr/local/conf
ADD docker/cloud/core/${myconfig} /usr/local/conf
WORKDIR /usr/local/bin
COPY --from=0 /usr/local/bin/stockstat-core-0.5-SNAPSHOT.jar .
RUN ln -s /tmp /usr/local/logs
USER root
CMD SPARK_DRIVER_HOST=`hostname -i` java -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:5432/stockstat -jar /usr/local/bin/stockstat-core-0.5-SNAPSHOT.jar

