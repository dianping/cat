cmake_minimum_required(VERSION 2.4)

project(ccat)

message(STATUS "Current OS: " ${CMAKE_SYSTEM_NAME})

macro(use_c99)
    if (CMAKE_VERSION VERSION_LESS "3.1")
        if (CMAKE_C_COMPILER_ID STREQUAL "GNU")
            set (CMAKE_C_FLAGS "--std=gnu99 ${CMAKE_C_FLAGS}")
        endif ()
    else ()
        set (CMAKE_C_STANDARD 99)
    endif ()
endmacro(use_c99)

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

macro(add_definitions_c def)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${def}")
endmacro()

macro(add_cxx_flags def)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${def}")
endmacro()

use_c99()
use_cxx11()

# set(BUILD_TYPE CPP)
# set(BUILD_TEST 1)
# set(BUILD_SCRIPT 1)

add_definitions("-Wno-format-security")
# add_definitions_c("-Wno-incompatible-pointer-types")

if (APPLE)
    add_definitions_c("-Wno-ignored-qualifiers")
else()
    add_definitions_c("-Wno-discarded-qualifiers")
endif()

include_directories(include)
include_directories(src)

set(
        HEADER_FILES
        src/lib/cat_atomic.h
        src/lib/cat_condition.h
        src/lib/cat_mutex.h
        src/lib/cat_semaphore.h
        src/lib/cat_thread.h
        src/lib/headers.h
        src/lib/typedef.h
        include/ccat/client.h
)

aux_source_directory(src/lib LIBRARY_FILES)
aux_source_directory(src/ccat SOURCE_FILES)

set(
        SOURCE_FILES
        ${SOURCE_FILES}
        ${LIBRARY_FILES}
)

if (APPLE)
elseif(UNIX)
    link_libraries(m)
    link_libraries(rt)
    link_libraries(pthread)
endif()

if (BUILD_TYPE STREQUAL CPP)
    set(HEADER_FILES include/cppcat/client.h ${HEADER_FILES})
    aux_source_directory(src/cppcat CPP_SOURCE_FILES)
    set(SOURCE_FILES ${SOURCE_FILES} ${CPP_SOURCE_FILES})
    add_library(catclient SHARED ${HEADER_FILES} ${SOURCE_FILES})
    install(TARGETS catclient DESTINATION lib)
else()
    set(HEADER_FILES include/ccat/client.h ${HEADER_FILES})
    add_library(catclient SHARED ${HEADER_FILES} ${SOURCE_FILES})
    install(TARGETS catclient DESTINATION lib)
endif()

if (BUILD_TEST)
    link_libraries(gtest)
    file(
            COPY
            tests/client.xml
            tests/client.json
            DESTINATION cat
    )

    aux_source_directory(tests/unit UNITTEST_FILES)
    add_executable(unittest tests/unittest.cpp ${UNITTEST_FILES} ${SOURCE_FILES})
endif()

if (BUILD_SCRIPT)
    if (BUILD_TYPE STREQUAL CPP)
        add_executable(test_client ${HEADER_FILES} ${SOURCE_FILES} benchmark/cat_client_test.cpp)
        add_executable(test_mpsc ${HEADER_FILES} ${SOURCE_FILES} benchmark/cat_mpsc_test.cpp)
    else()
        add_executable(test_client ${HEADER_FILES} ${SOURCE_FILES} benchmark/cat_client_test.c)
        add_executable(test_mpsc ${HEADER_FILES} ${SOURCE_FILES} benchmark/cat_mpsc_test.c)
    endif()
endif()
