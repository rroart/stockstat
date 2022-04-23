FROM debian:bullseye
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade -y
RUN apt-get install -y python3-pip
RUN pip3 install tensorflow pandas numpy Werkzeug Keras sklearn flask nameko

ADD tensorflow/ /tmp/

WORKDIR /tmp

CMD ./flasktf.sh