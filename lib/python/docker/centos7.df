FROM centos:7

MAINTAINER Terence Fan <stdrickforce@gmail.com>

RUN mkdir -p /tmp/pycat

COPY . /tmp/pycat

WORKDIR /tmp/pycat

ENV BUILD_ESSENTIALS=""

RUN curl https://bootstrap.pypa.io/get-pip.py -o get-pip.py && \
        python get-pip.py && rm get-pip.py && python setup.py install

WORKDIR /tmp/pycat/test
