cmake_minimum_required(VERSION 2.8)

project(ccat)

# Get ccat version from file.
file(READ version version)
set(CCAT_VERSION ${version})
string(REGEX REPLACE "\n$" "" CCAT_VERSION "${CCAT_VERSION}")
message(CCAT_VERSION: ${version})

configure_file(${CMAKE_CURRENT_SOURCE_DIR}/src/version.h.in ${CMAKE_CURRENT_BINARY_DIR}/include/ccat/version.h)

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

macro(add_definitions_c def)
    set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${def}")
endmacro()

use_c99()

# set(BUILD_SCRIPT 1)

add_definitions("-Wno-format-security")

if (APPLE)
    add_definitions_c("-Wno-ignored-qualifiers")
    add_definitions_c("-Wno-compare-distinct-pointer-types")
else()
    add_definitions_c("-Wno-discarded-qualifiers")
    add_definitions_c("-Wno-incompatible-pointer-types")
endif()

include_directories(include)
include_directories(src)
include_directories(${CMAKE_CURRENT_BINARY_DIR}/include)

set(
        HEADER_FILES
        include/client.h
        src/lib/cat_atomic.h
        src/lib/cat_condition.h
        src/lib/cat_mutex.h
        src/lib/cat_semaphore.h
        src/lib/cat_test.h
        src/lib/cat_thread.h
        src/lib/headers.h
        src/lib/typedef.h
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

set(HEADER_FILES include/client.h ${HEADER_FILES})
add_library(catclient-static STATIC ${HEADER_FILES} ${SOURCE_FILES})
set_target_properties(catclient-static PROPERTIES OUTPUT_NAME catclient)
add_library(catclient-shared SHARED ${HEADER_FILES} ${SOURCE_FILES})
set_target_properties(catclient-shared PROPERTIES OUTPUT_NAME catclient)
install(TARGETS catclient-shared DESTINATION lib)
install(TARGETS catclient-static DESTINATION lib)

if (BUILD_SCRIPT)
    add_executable(test_client ${HEADER_FILES} ${SOURCE_FILES} scripts/cat_client_test.c)
    add_executable(test_fork ${HEADER_FILES} ${SOURCE_FILES} scripts/cat_fork_test.c)
    add_executable(test_mpsc ${HEADER_FILES} ${SOURCE_FILES} scripts/cat_mpsc_test.c)
    add_executable(test_stack ${HEADER_FILES} ${SOURCE_FILES} scripts/cat_stack_test.c)
endif()
