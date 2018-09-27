cmake_minimum_required(VERSION 2.4)

project(cppcat)

set(CPPCAT_VERSION 3.0.0)

configure_file(${CMAKE_CURRENT_SOURCE_DIR}/src/version.h.in ${CMAKE_CURRENT_BINARY_DIR}/include/ccat/version.h)

message(STATUS "Current OS: " ${CMAKE_SYSTEM_NAME})

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

use_cxx11()

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
include_directories(${CMAKE_CURRENT_BINARY_DIR}/include)
include_directories(../c/include)

aux_source_directory(src/lib SOURCE_FILES)
aux_source_directory(src/ccat SOURCE_FILES)
aux_source_directory(src/cppcat SOURCE_FILES)

if (APPLE)
elseif(UNIX)
    link_libraries(m)
    link_libraries(rt)
    link_libraries(pthread)
endif()

set(HEADER_FILES include/client.hpp)
add_library(catclient SHARED ${HEADER_FILES} ${SOURCE_FILES})
install(TARGETS catclient DESTINATION lib)

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
    add_executable(test_client ${SOURCE_FILES} scripts/cat_client_test.cpp)
    add_executable(test_mpsc ${SOURCE_FILES} scripts/cat_mpsc_test.cpp)
endif()
