FROM centos:6

MAINTAINER Terence Fan <stdrickforce@gmail.com>

RUN mkdir -p /tmp/ccat

COPY . /tmp/ccat

WORKDIR /tmp/ccat

ENV BUILD_ESSENTIALS="gcc gcc-c++ cmake"

RUN yum install $BUILD_ESSENTIALS -y && \
        mkdir -p cmake && cd cmake && \
        cmake .. && make -j && make install && \
        cd .. && rm -r cmake && \
        yum remove $BUILD_ESSENTIALS -y && yum clean all
