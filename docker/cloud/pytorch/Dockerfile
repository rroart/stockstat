FROM debian:bullseye
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3-pip
RUN pip3 install torch pandas numpy Werkzeug Keras sklearn flask nameko

ADD pytorch/ /tmp/

#USER root

WORKDIR /tmp

CMD ./flaskpt.sh