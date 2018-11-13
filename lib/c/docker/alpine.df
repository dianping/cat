FROM alpine:latest

MAINTAINER Terence Fan <stdrickforce@gmail.com>

RUN mkdir -p /tmp/ccat

COPY . /tmp/ccat

WORKDIR /tmp/ccat

ENV BUILD_ESSENTIALS="gcc g++ make cmake linux-headers"

RUN apk add $BUILD_ESSENTIALS && \
        mkdir -p cmake && cd cmake && \
        cmake .. && make -j && make install && \
        cd .. && rm -r cmake && \
        apk del $BUILD_ESSENTIALS
