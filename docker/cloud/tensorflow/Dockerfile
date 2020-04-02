ARG UBUNTU_VERSION=18.04

FROM ubuntu:${UBUNTU_VERSION} as base

RUN apt-get update && apt-get install -y curl

ARG _PY_SUFFIX=3
ARG PYTHON=python${_PY_SUFFIX}
ARG PIP=pip${_PY_SUFFIX}
ARG myport
ENV MYPORT ${myport}

# See http://bugs.python.org/issue19846
ENV LANG C.UTF-8

RUN apt-get update && apt-get install -y \
    ${PYTHON} \
    ${PYTHON}-pip

RUN ${PIP} --no-cache-dir install --upgrade pip setuptools

# Some TF tools expect a "python" binary
RUN ln -s $(which ${PYTHON}) /usr/local/bin/python

ARG TF_PACKAGE=tensorflow
ARG TF_PACKAGE_VERSION=2.1
RUN ${PIP} install --no-cache-dir ${TF_PACKAGE}${TF_PACKAGE_VERSION:+==${TF_PACKAGE_VERSION}}

RUN pip install flask pandas keras

ADD tensorflow/ /usr/local/tensorflow

WORKDIR /usr/local/tensorflow

CMD ./flasktf.sh ${MYPORT}