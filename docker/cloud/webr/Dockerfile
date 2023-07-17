FROM node:18
MAINTAINER <rroart@gmail.com>
ARG docroot
ARG mywebport
ARG myserver
ARG myport
ARG myiserver
ARG myiport
ARG myaserver
ARG myaport
ENV DOCROOT ${docroot}
ENV MYWEBPORT ${mywebport}
ENV REACT_APP_MYSERVER ${myserver}
ENV REACT_APP_MYPORT ${myport}
ENV REACT_APP_MYISERVER ${myiserver}
ENV REACT_APP_MYIPORT ${myiport}
ENV REACT_APP_MYASERVER ${myaserver}
ENV REACT_APP_MYAPORT ${myaport}
RUN mkdir /app
WORKDIR /app
COPY $DOCROOT docroot
RUN npm install -g http-server
WORKDIR /app

ENTRYPOINT npx react-inject-env set -d docroot && npx http-server docroot -p $MYWEBPORT
