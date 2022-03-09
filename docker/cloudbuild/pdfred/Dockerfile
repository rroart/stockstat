FROM maven:3.6.3-jdk-13
MAINTAINER <rroart@gmail.com>
RUN mkdir /app
WORKDIR /app
RUN mkdir webr iclij iclij/iclij-chatbot iclij/iclij-commom iclij/iclij-core iclij/evolution iclij/iclij-web  iclij/iclij-webcore iclij/iclij-webr distribution
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
COPY distribution distribution
COPY pom.xml pom.xml
RUN mvn -pl input -am install -Dmaven.test.skip=true
RUN cp -p input/target/stockstat-input-0.6-SNAPSHOT-jar-with-dependencies.jar /usr/local/bin

#FROM debian:stretch
FROM openjdk:16-jdk
MAINTAINER <roart@nvg.ntnu.no>

ARG mydbserver
ARG mydbserverport=5432
ARG mydbserverdb=stockstat
ENV MYDBSERVER ${mydbserver}
ENV MYDBSERVERPORT ${mydbserverport}
ENV MYDBSERVERDB ${mydbserverdb}

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3 python3-pip
#RUN apt-get install -y postgresql-client-10
RUN pip3 install pandas_datareader
RUN mkdir /usr/local/pdfred
ADD python/pdfred/*.py /usr/local/pdfred/
COPY --from=0 /usr/local/bin/stockstat-input-0.6-SNAPSHOT-jar-with-dependencies.jar /usr/local/bin/

WORKDIR /usr/local/pdfred

#ENV SPARK_MASTER_HOST 127.0.0.1

CMD /usr/bin/python3 /usr/local/pdfred/generatexml.py; \
    java -Dconnection.url=jdbc:postgresql://${MYDBSERVER}:${MYDBSERVERPORT}/${MYDBSERVERDB} -jar /usr/local/bin/stockstat-input-0.6-SNAPSHOT-jar-with-dependencies.jar /tmp/fred.xml; \
    echo finished; \
    while true; do sleep 3600; done 
