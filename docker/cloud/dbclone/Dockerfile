FROM openjdk:11-jdk
MAINTAINER <roart@nvg.ntnu.no>

ARG mydump

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y postgresql-client-11
COPY docker/cloud/dbclone/${mydump} /usr/local/bin/stockstat.sql
#PGPASSWORD='password' createdb -U stockstat -W -h postgresql-12-centos7 stockstat; 
CMD sleep 30; PGPASSWORD='password' psql -U stockstat -h postgresql-12-centos7 -d stockstat -f /usr/local/bin/stockstat.sql; while true; do sleep 3600; done 
