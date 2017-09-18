FROM debian:stretch
MAINTAINER <roart@nvg.ntnu.no>

RUN apt-get update && apt-get upgrade
RUN apt-get install -y python3-pip
RUN pip3 install pandas tensorflow numpy nameko Werkzeug

ADD files/tfmain.py /usr/local/bin/tfmain.py

EXPOSE 8000

WORKDIR /usr/local/bin

CMD nameko run tfmain