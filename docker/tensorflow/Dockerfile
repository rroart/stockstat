FROM debian:stretch
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3-pip
RUN pip3 install pandas tensorflow numpy Werkzeug Keras sklearn flask

ADD files/*.py /usr/local/bin/
ADD files/*.sh /usr/local/bin/

EXPOSE 8000

WORKDIR /usr/local/bin

CMD flasktf.sh