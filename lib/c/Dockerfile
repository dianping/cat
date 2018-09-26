FROM alpine:latest

MAINTAINER Terence Fan <stdrickforce@gmail.com>

RUN mkdir -p /tmp/ccat

COPY . /tmp/ccat

WORKDIR /tmp

RUN apk add cmake make g++ linux-headers

ARG GTEST_PKG=release-1.8.1.tar.gz

RUN wget https://github.com/google/googletest/archive/$GTEST_PKG && \
        tar -xzf $GTEST_PKG && rm $GTEST_PKG && \
        cd googletest-release-1.8.1 && \
        mkdir -p cmake && cd cmake && cmake .. && make -j && make install && \
        cd .. && rm -rf googletest-release-1.8.1

WORKDIR /tmp/ccat

RUN mkdir -p cmake && cd cmake && \
        cmake -DBUILD_TEST .. && make -j && make install

WORKDIR /tmp/ccat/cmake

CMD ./unittest
