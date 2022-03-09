FROM maven:3.6.3-jdk-13
MAINTAINER <rroart@gmail.com>
RUN mkdir /app
WORKDIR /app
WORKDIR /app/stockstat
RUN mkdir webr iclij iclij/iclij-chatbot iclij/iclij-commom iclij/iclij-core iclij/evolution iclij/iclij-web iclij/iclij-webcore iclij/iclij-webr distribution
COPY main main
COPY input input
COPY common common
COPY iclij/pom.xml iclij
COPY iclij/iclij-chatbot iclij/iclij-chatbot
COPY iclij/iclij-common iclij/iclij-common
COPY iclij/iclij-core iclij/iclij-core
COPY iclij/iclij-evolution iclij/iclij-evolution
COPY iclij/iclij-web/pom.xml iclij/iclij-web/pom.xml
COPY iclij/iclij-webcore/pom.xml iclij/iclij-webcore/pom.xml
COPY iclij/iclij-webr/pom.xml iclij/iclij-webr/pom.xml
COPY eureka eureka
COPY web web
COPY webr/pom.xml webr
COPY distribution/pom.xml distribution
COPY pom.xml pom.xml
RUN mvn -pl iclij/iclij-core -am install -Dmaven.test.skip=true
RUN cp -p iclij/iclij-core/target/stockstat-iclij-core-0.6-SNAPSHOT.jar /usr/local/bin

FROM supinf/envsubst
ARG myconfig
ARG myenv
ENV MYCONFIG ${myconfig}
ENV MYENV ${myenv}
WORKDIR /tmp
COPY ${myconfig} .
COPY ${myenv} env.sh
RUN . env.sh; envsubst < iclij.xml.tmpl > iclij.xml

FROM openjdk:16-jdk
MAINTAINER <roart@nvg.ntnu.no>
ARG myserver
ARG myport
ARG mywebport
ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYWEBPORT ${mywebport}
ENV MYDBSERVER ${mydbserver}
ENV MYDBSERVERPORT ${mydbserverport}
ENV MYDBSERVERDB ${mydbserverdb}
RUN mkdir /usr/local/conf
WORKDIR /usr/local/bin
COPY --from=0 /usr/local/bin/stockstat-iclij-core-0.6-SNAPSHOT.jar .
COPY --from=1 /tmp/iclij.xml /usr/local/conf/
RUN ln -s /tmp /usr/local/logs
CMD java -Dserver.port=${MYWEBPORT} -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/stockstat-iclij-core-0.6-SNAPSHOT.jar --spring.profiles.active=docker

