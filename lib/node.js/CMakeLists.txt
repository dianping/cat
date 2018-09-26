project(nodecat)

# This CMakeLists.txt is only for development.
# Please input following commands to build a runnable version:
#   node-gyp configure
#   node-gyp build

cmake_minimum_required(VERSION 2.4)

macro(use_cxx11)
    include(CheckCXXCompilerFlag)
    CHECK_CXX_COMPILER_FLAG("-std=c++11" COMPILER_SUPPORTS_CXX11)
    CHECK_CXX_COMPILER_FLAG("-std=c++0x" COMPILER_SUPPORTS_CXX0X)
    if(COMPILER_SUPPORTS_CXX11)
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
    elseif(COMPILER_SUPPORTS_CXX0X)
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++0x")
    else()
        message(STATUS "The compiler ${CMAKE_CXX_COMPILER} has no C++11 support. Please use a different C++ compiler.")
    endif()
endmacro(use_cxx11)

use_cxx11()

if (APPLE)
    set(CMAKE_MACOSX_RPATH 1)
endif()

include(NodeJS.cmake)
nodejs_init()

include_directories(include)

set(
        SOURCE_FILES
        src/nodecat.cc
)

set(
        HEADER_FILES
        include/client.h
        src/debug.h
)

link_libraries(catclient)
add_nodejs_module(nodecat ${SOURCE_FILES} ${HEADER_FILES})