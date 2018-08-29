FROM debian:stretch
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3-pip
RUN pip3 install tensorflow==1.5 pandas numpy Werkzeug Keras sklearn flask nameko

ADD files/*.py /tmp/
ADD files/*.sh /tmp/

#USER root
EXPOSE 8000

WORKDIR /tmp

CMD ./flasktf.sh