FROM node:18
MAINTAINER <rroart@gmail.com>
ARG dist
ARG mywebport
ARG myserver
ARG myport
ARG myiserver
ARG myiport
ARG myaserver
ARG myaport
ENV DIST ${dist}
ENV MYWEBPORT ${mywebport}
ENV MYSERVER ${myserver}
ENV MYPORT ${myport}
ENV MYISERVER ${myiserver}
ENV MYIPORT ${myiport}
ENV MYASERVER ${myaserver}
ENV MYAPORT ${myaport}
RUN apt update
RUN apt install -y gettext-base
RUN mkdir /app
WORKDIR /app
COPY $DIST dist
COPY $DIST/../scripts/genenv.sh /app
WORKDIR /app/dist
RUN npm install -g http-server
WORKDIR /app/dist
RUN envsubst < env.js.tmpl > env.js

CMD http-server -p $MYWEBPORT
