# Defaults for standard Node.js builds
set(NODEJS_DEFAULT_URL https://nodejs.org/download/release)
set(NODEJS_DEFAULT_VERSION installed)
set(NODEJS_VERSION_FALLBACK latest)
set(NODEJS_DEFAULT_NAME node)
set(NODEJS_DEFAULT_CHECKSUM SHASUMS256.txt)
set(NODEJS_DEFAULT_CHECKTYPE SHA256)

include(CMakeParseArguments)

# Find a path by walking upward from a base directory until the path is
# found. Sets the variable ${PATH} to False if the path can't
# be determined
function(find_path_parent NAME BASE PATH)
    set(ROOT ${BASE})
    set(${PATH} ${ROOT}/${NAME} PARENT_SCOPE)
    set(DRIVE "^[A-Za-z]?:?/$")
    while(NOT ROOT MATCHES ${DRIVE} AND NOT EXISTS ${ROOT}/${NAME})
        get_filename_component(ROOT ${ROOT} DIRECTORY)
        set(${PATH} ${ROOT}/${NAME} PARENT_SCOPE)
    endwhile()
    if(ROOT MATCHES ${DRIVE})
        set(${PATH} False PARENT_SCOPE)
    endif()
endfunction()

# Shortcut for finding standard node module locations
macro(find_nodejs_module NAME BASE PATH)
    find_path_parent(node_modules/${NAME} ${BASE} ${PATH})
endmacro()

# Download with a bit of nice output (without spewing progress)
function(download_file URL)
    message(STATUS "Downloading: ${URL}")
    file(APPEND ${TEMP}/download.log "Downloading: ${URL}\n")
    file(APPEND ${TEMP}/download.log "----------------------------------------\n")
    file(DOWNLOAD
        ${URL}
        ${ARGN}
        LOG DOWNLOAD_LOG
    )
    file(APPEND ${TEMP}/download.log ${DOWNLOAD_LOG})
    file(APPEND ${TEMP}/download.log "----------------------------------------\n")
endfunction()

# Embedded win_delay_load_hook file so that this file can be copied
# into projects directly (recommended practice)
function(nodejs_generate_delayload_hook OUTPUT)
    file(WRITE  ${OUTPUT} "")
    file(APPEND ${OUTPUT} "/*\n")
    file(APPEND ${OUTPUT} " * When this file is linked to a DLL, it sets up a delay-load hook that\n")
    file(APPEND ${OUTPUT} " * intervenes when the DLL is trying to load the main node binary\n")
    file(APPEND ${OUTPUT} " * dynamically. Instead of trying to locate the .exe file it'll just return\n")
    file(APPEND ${OUTPUT} " * a handle to the process image.\n")
    file(APPEND ${OUTPUT} " *\n")
    file(APPEND ${OUTPUT} " * This allows compiled addons to work when node.exe or iojs.exe is renamed.\n")
    file(APPEND ${OUTPUT} " */\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "#ifdef _MSC_VER\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "#ifndef DELAYIMP_INSECURE_WRITABLE_HOOKS\n")
    file(APPEND ${OUTPUT} "#define DELAYIMP_INSECURE_WRITABLE_HOOKS\n")
    file(APPEND ${OUTPUT} "#endif\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "#ifndef WIN32_LEAN_AND_MEAN\n")
    file(APPEND ${OUTPUT} "#define WIN32_LEAN_AND_MEAN\n")
    file(APPEND ${OUTPUT} "#endif\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "#include <windows.h>\n")
    file(APPEND ${OUTPUT} "#include <Shlwapi.h>\n")
    file(APPEND ${OUTPUT} "#include <delayimp.h>\n")
    file(APPEND ${OUTPUT} "#include <string.h>\n")
    file(APPEND ${OUTPUT} "#include <tchar.h>\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "static FARPROC WINAPI load_exe_hook(unsigned int event, DelayLoadInfo* info) {\n")
    file(APPEND ${OUTPUT} "  if (event != dliNotePreLoadLibrary) return NULL;\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  if (_stricmp(info->szDll, \"iojs.exe\") != 0 &&\n")
    file(APPEND ${OUTPUT} "      _stricmp(info->szDll, \"node.exe\") != 0 &&\n")
    file(APPEND ${OUTPUT} "      _stricmp(info->szDll, \"node.dll\") != 0)\n")
    file(APPEND ${OUTPUT} "    return NULL;\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  // Get a handle to the current process executable.\n")
    file(APPEND ${OUTPUT} "  HMODULE processModule = GetModuleHandle(NULL);\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  // Get the path to the executable.\n")
    file(APPEND ${OUTPUT} "  TCHAR processPath[_MAX_PATH];\n")
    file(APPEND ${OUTPUT} "  GetModuleFileName(processModule, processPath, _MAX_PATH);\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  // Get the name of the current executable.\n")
    file(APPEND ${OUTPUT} "  LPTSTR processName = PathFindFileName(processPath);\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  // If the current process is node or iojs, then just return the proccess \n")
    file(APPEND ${OUTPUT} "  // module.\n")
    file(APPEND ${OUTPUT} "  if (_tcsicmp(processName, TEXT(\"node.exe\")) == 0 ||\n")
    file(APPEND ${OUTPUT} "      _tcsicmp(processName, TEXT(\"iojs.exe\")) == 0) {\n")
    file(APPEND ${OUTPUT} "    return (FARPROC) processModule;\n")
    file(APPEND ${OUTPUT} "  }\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  // If it is another process, attempt to load 'node.dll' from the same \n")
    file(APPEND ${OUTPUT} "  // directory.\n")
    file(APPEND ${OUTPUT} "  PathRemoveFileSpec(processPath);\n")
    file(APPEND ${OUTPUT} "  PathAppend(processPath, TEXT(\"node.dll\"));\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  HMODULE nodeDllModule = GetModuleHandle(processPath);\n")
    file(APPEND ${OUTPUT} "  if(nodeDllModule != NULL) {\n")
    file(APPEND ${OUTPUT} "    // This application has a node.dll in the same directory as the executable,\n")
    file(APPEND ${OUTPUT} "    // use that.\n")
    file(APPEND ${OUTPUT} "    return (FARPROC) nodeDllModule;\n")
    file(APPEND ${OUTPUT} "  }\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "  // Fallback to the current executable, which must statically link to \n")
    file(APPEND ${OUTPUT} "  // node.lib\n")
    file(APPEND ${OUTPUT} "  return (FARPROC) processModule;\n")
    file(APPEND ${OUTPUT} "}\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "PfnDliHook __pfnDliNotifyHook2 = load_exe_hook;\n")
    file(APPEND ${OUTPUT} "\n")
    file(APPEND ${OUTPUT} "#endif\n")
endfunction()

# Sets up a project to build Node.js native modules
# - Downloads required dependencies and unpacks them to the build directory.
#   Internet access is required the first invocation but not after (
#   provided the download is successful)
# - Sets up several variables for building against the downloaded
#   dependencies
# - Guarded to prevent multiple executions, so a single project hierarchy
#   will only call this once
function(nodejs_init)
    # Prevents this function from executing more than once
    if(NODEJS_INIT)
        return()
    endif()

    # Regex patterns used by the init function for component extraction
    set(HEADERS_MATCH "^([A-Fa-f0-9]+)[ \t]+([^-]+)-(headers|v?[0-9.]+)-(headers|v?[0-9.]+)([.]tar[.]gz)$")
    set(LIB32_MATCH "(^[0-9A-Fa-f]+)[\t ]+(win-x86)?(/)?([^/]*)(.lib)$")
    set(LIB64_MATCH "(^[0-9A-Fa-f]+)[\t ]+(win-)?(x64/)(.*)(.lib)$")

    # Parse function arguments
    cmake_parse_arguments(nodejs_init
        "" "URL;NAME;VERSION;CHECKSUM;CHECKTYPE" "" ${ARGN}
    )

    # Allow the download URL to be overridden by command line argument
    # NODEJS_URL
    if(NODEJS_URL)
        set(URL ${NODEJS_URL})
    else()
        # Use the argument if specified, falling back to the default
        set(URL ${NODEJS_DEFAULT_URL})
        if(nodejs_init_URL)
            set(URL ${nodejs_init_URL})
        endif()
    endif()

    # Allow name to be overridden by command line argument NODEJS_NAME
    if(NODEJS_NAME)
        set(NAME ${NODEJS_NAME})
    else()
        # Use the argument if specified, falling back to the default
        set(NAME ${NODEJS_DEFAULT_NAME})
        if(nodejs_init_NAME)
            set(NAME ${nodejs_init_NAME})
        endif()
    endif()

    # Allow the checksum file to be overridden by command line argument
    # NODEJS_CHECKSUM
    if(NODEJS_CHECKSUM)
        set(CHECKSUM ${NODEJS_CHECKSUM})
    else()
        # Use the argument if specified, falling back to the default
        set(CHECKSUM ${NODEJS_DEFAULT_CHECKSUM})
        if(nodejs_init_CHECKSUM)
            set(CHECKSUM ${nodejs_init_CHECKSUM})
        endif()
    endif()

    # Allow the checksum type to be overriden by the command line argument
    # NODEJS_CHECKTYPE
    if(NODEJS_CHECKTYPE)
        set(CHECKTYPE ${NODEJS_CHECKTYPE})
    else()
        # Use the argument if specified, falling back to the default
        set(CHECKTYPE ${NODEJS_DEFAULT_CHECKTYPE})
        if(nodejs_init_CHECKTYPE)
            set(CHECKTYPE ${nodejs_init_CHECKTYPE})
        endif()
    endif()

    # Allow the version to be overridden by the command line argument
    # NODEJS_VERSION
    if(NODEJS_VERSION)
        set(VERSION ${NODEJS_VERSION})
    else()
        # Use the argument if specified, falling back to the default
        set(VERSION ${NODEJS_DEFAULT_VERSION})
        if(nodejs_init_VERSION)
            set(VERSION ${nodejs_init_VERSION})
        endif()
    endif()

    # "installed" is a special version that tries to use the currently
    # installed version (determined by running node)
    set(NODEJS_INSTALLED False CACHE BOOL "Node.js install status" FORCE)
    if(VERSION STREQUAL "installed")
        if(NOT NAME STREQUAL ${NODEJS_DEFAULT_NAME})
            message(FATAL_ERROR
                "'Installed' version identifier can only be used with"
                "the core Node.js library"
            )
        endif()
        # Fall back to the "latest" version if node isn't installed
        set(VERSION ${NODEJS_VERSION_FALLBACK})
        # This has all of the implications of why the binary is called nodejs in the first place
        # https://lists.debian.org/debian-devel-announce/2012/07/msg00002.html
        # However, with nvm/n, its nearly standard to have a proper 'node' binary now (since the
        # apt-based one is so out of date), so for now just assume that this rare binary conflict
        # case is the degenerate case. May need a more complicated solution later.
        find_program(NODEJS_BINARY NAMES node nodejs)
        if(NODEJS_BINARY)
            execute_process(
                COMMAND ${NODEJS_BINARY} --version
                RESULT_VARIABLE INSTALLED_VERSION_RESULT
                OUTPUT_VARIABLE INSTALLED_VERSION
                OUTPUT_STRIP_TRAILING_WHITESPACE
            )
            if(INSTALLED_VERSION_RESULT STREQUAL "0")
                set(NODEJS_INSTALLED True CACHE BOOL
                    "Node.js install status" FORCE
                )
                set(VERSION ${INSTALLED_VERSION})
            endif()
        endif()
    endif()

    # Create a temporary download directory
    set(TEMP ${CMAKE_CURRENT_BINARY_DIR}/temp)
    if(EXISTS ${TEMP})
        file(REMOVE_RECURSE ${TEMP})
    endif()
    file(MAKE_DIRECTORY ${TEMP})

    # Unless the target is special version "latest", the parameters
    # necessary to construct the root path are known
    if(NOT VERSION STREQUAL "latest")
        set(ROOT ${CMAKE_CURRENT_BINARY_DIR}/${NAME}/${VERSION})
        # Extract checksums from the existing checksum file
        set(CHECKSUM_TARGET ${ROOT}/CHECKSUM)
    endif()

    # If we're trying to determine the version or we haven't saved the
    # checksum file for this version, download it from the specified server
    if(VERSION STREQUAL "latest" OR
      (DEFINED ROOT AND NOT EXISTS ${ROOT}/CHECKSUM))
        if(DEFINED ROOT)
            # Clear away the old checksum in case the new one is different
            # and/or it fails to download
            file(REMOVE ${ROOT}/CHECKSUM)
        endif()
        file(REMOVE ${TEMP}/CHECKSUM)
        download_file(
            ${URL}/${VERSION}/${CHECKSUM}
            ${TEMP}/CHECKSUM
            INACTIVITY_TIMEOUT 10
            STATUS CHECKSUM_STATUS
        )
        list(GET CHECKSUM_STATUS 0 CHECKSUM_STATUS)
        if(CHECKSUM_STATUS GREATER 0)
            file(REMOVE ${TEMP}/CHECKSUM)
            message(FATAL_ERROR
                "Unable to download checksum file"
            )
        endif()
        # Extract checksums from the temporary file
        set(CHECKSUM_TARGET ${TEMP}/CHECKSUM)
    endif()

    # Extract the version, name, header archive and archive checksum
    # from the file. This first extract is what defines / specifies the
    # actual version number and name.
    file(STRINGS
        ${CHECKSUM_TARGET} HEADERS_CHECKSUM
        REGEX ${HEADERS_MATCH}
        LIMIT_COUNT 1
    )
    if(NOT HEADERS_CHECKSUM)
        file(REMOVE ${TEMP}/CHECKSUM)
        if(DEFINED ROOT)
            file(REMOVE ${ROOT}/CHECKSUM)
        endif()
        message(FATAL_ERROR "Unable to extract header archive checksum")
    endif()
    string(REGEX MATCH ${HEADERS_MATCH} HEADERS_CHECKSUM ${HEADERS_CHECKSUM})
    set(HEADERS_CHECKSUM ${CMAKE_MATCH_1})
    set(NAME ${CMAKE_MATCH_2})
    if(CMAKE_MATCH_3 STREQUAL "headers")
        set(VERSION ${CMAKE_MATCH_4})
    else()
        set(VERSION ${CMAKE_MATCH_3})
    endif()
    set(HEADERS_ARCHIVE
        ${CMAKE_MATCH_2}-${CMAKE_MATCH_3}-${CMAKE_MATCH_4}${CMAKE_MATCH_5}
    )
    # Make sure that the root directory exists, and that the checksum
    # file has been moved over from temp
    if(DEFINED ROOT)
        set(OLD_ROOT ${ROOT})
    endif()
    set(ROOT ${CMAKE_CURRENT_BINARY_DIR}/${NAME}/${VERSION})
    if(DEFINED OLD_ROOT AND NOT ROOT STREQUAL "${OLD_ROOT}")
        file(REMOVE ${TEMP}/CHECKSUM)
        file(REMOVE ${ROOT}/CHECKSUM)
        message(FATAL_ERROR "Version/Name mismatch")
    endif()
    file(MAKE_DIRECTORY ${ROOT})
    if(EXISTS ${TEMP}/CHECKSUM)
        file(REMOVE ${ROOT}/CHECKSUM)
        file(RENAME ${TEMP}/CHECKSUM ${ROOT}/CHECKSUM)
    endif()

    # Now that its fully resolved, report the name and version of Node.js being
    # used
    message(STATUS "NodeJS: Using ${NAME}, version ${VERSION}")

    # Download the headers for the version being used
    # Theoretically, these could be found by searching the installed
    # system, but in practice, this can be error prone. They're provided
    # on the download servers, so just use the ones there.
    if(NOT EXISTS ${ROOT}/include)
        file(REMOVE ${TEMP}/${HEADERS_ARCHIVE})
        download_file(
            ${URL}/${VERSION}/${HEADERS_ARCHIVE}
            ${TEMP}/${HEADERS_ARCHIVE}
            INACTIVITY_TIMEOUT 10
            EXPECTED_HASH ${CHECKTYPE}=${HEADERS_CHECKSUM}
            STATUS HEADERS_STATUS
        )
        list(GET HEADERS_STATUS 0 HEADERS_STATUS)
        if(HEADER_STATUS GREATER 0)
            file(REMOVE ${TEMP}/${HEADERS_ARCHIVE})
            message(FATAL_ERROR "Unable to download Node.js headers")
        endif()
        execute_process(
            COMMAND ${CMAKE_COMMAND} -E tar xfz ${TEMP}/${HEADERS_ARCHIVE}
            WORKING_DIRECTORY ${TEMP}
        )

        # This adapts the header extraction to support a number of different
        # header archive contents in addition to the one used by the
        # default Node.js library
        unset(NODEJS_HEADERS_PATH CACHE)
        find_path(NODEJS_HEADERS_PATH
            NAMES src include
            PATHS
                ${TEMP}/${NAME}-${VERSION}-headers
                ${TEMP}/${NAME}-${VERSION}
                ${TEMP}/${NODEJS_DEFAULT_NAME}-${VERSION}-headers
                ${TEMP}/${NODEJS_DEFAULT_NAME}-${VERSION}
                ${TEMP}/${NODEJS_DEFAULT_NAME}
                ${TEMP}
            NO_DEFAULT_PATH
        )
        if(NOT NODEJS_HEADERS_PATH)
            message(FATAL_ERROR "Unable to find extracted headers folder")
        endif()

        # Move the headers into a standard location with a standard layout
        file(REMOVE ${TEMP}/${HEADERS_ARCHIVE})
        file(REMOVE_RECURSE ${ROOT}/include)
        if(EXISTS ${NODEJS_HEADERS_PATH}/include/node)
            file(RENAME ${NODEJS_HEADERS_PATH}/include/node ${ROOT}/include)
        elseif(EXISTS ${NODEJS_HEADERS_PATH}/src)
            file(MAKE_DIRECTORY ${ROOT}/include)
            if(NOT EXISTS ${NODEJS_HEADERS_PATH}/src)
                file(REMOVE_RECURSE ${ROOT}/include)
                message(FATAL_ERROR "Unable to find core headers")
            endif()
            file(COPY ${NODEJS_HEADERS_PATH}/src/
                DESTINATION ${ROOT}/include
            )
            if(NOT EXISTS ${NODEJS_HEADERS_PATH}/deps/uv/include)
                file(REMOVE_RECURSE ${ROOT}/include)
                message(FATAL_ERROR "Unable to find libuv headers")
            endif()
            file(COPY ${NODEJS_HEADERS_PATH}/deps/uv/include/
                DESTINATION ${ROOT}/include
            )
            if(NOT EXISTS ${NODEJS_HEADERS_PATH}/deps/v8/include)
                file(REMOVE_RECURSE ${ROOT}/include)
                message(FATAL_ERROR "Unable to find v8 headers")
            endif()
            file(COPY ${NODEJS_HEADERS_PATH}/deps/v8/include/
                DESTINATION ${ROOT}/include
            )
            if(NOT EXISTS ${NODEJS_HEADERS_PATH}/deps/zlib)
                file(REMOVE_RECURSE ${ROOT}/include)
                message(FATAL_ERROR "Unable to find zlib headers")
            endif()
            file(COPY ${NODEJS_HEADERS_PATH}/deps/zlib/
                DESTINATION ${ROOT}/include
            )
        endif()
        file(REMOVE_RECURSE ${NODEJS_HEADERS_PATH})
        unset(NODEJS_HEADERS_PATH CACHE)
    endif()

    # Only download the libraries on windows, since its the only place
    # its necessary. Note, this requires rerunning CMake if moving
    # a module from one platform to another (should happen automatically
    # with most generators)
    if(WIN32)
        # Download the win32 library for linking
        file(STRINGS
            ${ROOT}/CHECKSUM LIB32_CHECKSUM
            LIMIT_COUNT 1
            REGEX ${LIB32_MATCH}
        )
        if(NOT LIB32_CHECKSUM)
            message(FATAL_ERROR "Unable to extract x86 library checksum")
        endif()
        string(REGEX MATCH ${LIB32_MATCH} LIB32_CHECKSUM ${LIB32_CHECKSUM})
        set(LIB32_CHECKSUM ${CMAKE_MATCH_1})
        set(LIB32_PATH     win-x86)
        set(LIB32_NAME     ${CMAKE_MATCH_4}${CMAKE_MATCH_5})
        set(LIB32_TARGET   ${CMAKE_MATCH_2}${CMAKE_MATCH_3}${LIB32_NAME})
        if(NOT EXISTS ${ROOT}/${LIB32_PATH})
            file(REMOVE_RECURSE ${TEMP}/${LIB32_PATH})
            download_file(
               ${URL}/${VERSION}/${LIB32_TARGET}
               ${TEMP}/${LIB32_PATH}/${LIB32_NAME}
               INACTIVITY_TIMEOUT 10
               EXPECTED_HASH ${CHECKTYPE}=${LIB32_CHECKSUM}
               STATUS LIB32_STATUS
            )
            list(GET LIB32_STATUS 0 LIB32_STATUS)
            if(LIB32_STATUS GREATER 0)
                message(FATAL_ERROR
                    "Unable to download Node.js windows library (32-bit)"
                )
            endif()
            file(REMOVE_RECURSE ${ROOT}/${LIB32_PATH})
            file(MAKE_DIRECTORY ${ROOT}/${LIB32_PATH})
            file(RENAME
                ${TEMP}/${LIB32_PATH}/${LIB32_NAME}
                ${ROOT}/${LIB32_PATH}/${LIB32_NAME}
            )
            file(REMOVE_RECURSE ${TEMP}/${LIB32_PATH})
        endif()

        # Download the win64 library for linking
        file(STRINGS
            ${ROOT}/CHECKSUM LIB64_CHECKSUM
            LIMIT_COUNT 1
            REGEX ${LIB64_MATCH}
        )
        if(NOT LIB64_CHECKSUM)
            message(FATAL_ERROR "Unable to extract x64 library checksum")
        endif()
        string(REGEX MATCH ${LIB64_MATCH} LIB64_CHECKSUM ${LIB64_CHECKSUM})
        set(LIB64_CHECKSUM ${CMAKE_MATCH_1})
        set(LIB64_PATH     win-x64)
        set(LIB64_NAME     ${CMAKE_MATCH_4}${CMAKE_MATCH_5})
        set(LIB64_TARGET   ${CMAKE_MATCH_2}${CMAKE_MATCH_3}${LIB64_NAME})
        if(NOT EXISTS ${ROOT}/${LIB64_PATH})
            file(REMOVE_RECURSE ${TEMP}/${LIB64_PATH})
            download_file(
               ${URL}/${VERSION}/${LIB64_TARGET}
               ${TEMP}/${LIB64_PATH}/${LIB64_NAME}
               INACTIVITY_TIMEOUT 10
               EXPECTED_HASH ${CHECKTYPE}=${LIB64_CHECKSUM}
               STATUS LIB64_STATUS
            )
            list(GET LIB64_STATUS 0 LIB64_STATUS)
            if(LIB64_STATUS GREATER 0)
                message(FATAL_ERROR
                    "Unable to download Node.js windows library (64-bit)"
                )
            endif()
            file(REMOVE_RECURSE ${ROOT}/${LIB64_PATH})
            file(MAKE_DIRECTORY ${ROOT}/${LIB64_PATH})
            file(RENAME
                ${TEMP}/${LIB64_PATH}/${LIB64_NAME}
                ${ROOT}/${LIB64_PATH}/${LIB64_NAME}
            )
            file(REMOVE_RECURSE ${TEMP}/${LIB64_PATH})
        endif()
    endif()

    # The downloaded headers should always be set for inclusion
    list(APPEND INCLUDE_DIRS ${ROOT}/include)

    # Look for the NAN module, and add it to the includes
    find_nodejs_module(
        nan
        ${CMAKE_CURRENT_SOURCE_DIR}
        NODEJS_NAN_DIR
    )
    if(NODEJS_NAN_DIR)
        list(APPEND INCLUDE_DIRS ${NODEJS_NAN_DIR})
    endif()

    # Under windows, we need a bunch of libraries (due to the way
    # dynamic linking works)
    if(WIN32)
        # Generate and use a delay load hook to allow the node binary
        # name to be changed while still loading native modules
        set(DELAY_LOAD_HOOK ${CMAKE_CURRENT_BINARY_DIR}/win_delay_load_hook.c)
        nodejs_generate_delayload_hook(${DELAY_LOAD_HOOK})
        set(SOURCES ${DELAY_LOAD_HOOK})

        # Necessary flags to get delayload working correctly
        list(APPEND LINK_FLAGS
            "-IGNORE:4199"
            "-DELAYLOAD:iojs.exe"
            "-DELAYLOAD:node.exe"
            "-DELAYLOAD:node.dll"
        )

        # Core system libraries used by node
        list(APPEND LIBRARIES
            kernel32.lib user32.lib gdi32.lib winspool.lib comdlg32.lib
            advapi32.lib shell32.lib ole32.lib oleaut32.lib uuid.lib
            odbc32.lib Shlwapi.lib DelayImp.lib
        )

        # Also link to the node stub itself (downloaded above)
        if(CMAKE_CL_64)
            list(APPEND LIBRARIES ${ROOT}/${LIB64_PATH}/${LIB64_NAME})
        else()
            list(APPEND LIBRARIES ${ROOT}/${LIB32_PATH}/${LIB32_NAME})
        endif()
    else()
        # Non-windows platforms should use these flags
        list(APPEND DEFINITIONS _LARGEFILE_SOURCE _FILE_OFFSET_BITS=64)
    endif()

    # Special handling for OSX / clang to allow undefined symbols
    # Define is required by node on OSX
    if(APPLE)
        list(APPEND LINK_FLAGS "-undefined dynamic_lookup")
        list(APPEND DEFINITIONS _DARWIN_USE_64_BIT_INODE=1)
    endif()

    # Export all settings for use as arguments in the rest of the build
    set(NODEJS_VERSION ${VERSION} PARENT_SCOPE)
    set(NODEJS_SOURCES ${SOURCES} PARENT_SCOPE)
    set(NODEJS_INCLUDE_DIRS ${INCLUDE_DIRS} PARENT_SCOPE)
    set(NODEJS_LIBRARIES ${LIBRARIES} PARENT_SCOPE)
    set(NODEJS_LINK_FLAGS ${LINK_FLAGS} PARENT_SCOPE)
    set(NODEJS_DEFINITIONS ${DEFINITIONS} PARENT_SCOPE)

    # Prevents this function from executing more than once
    set(NODEJS_INIT TRUE PARENT_SCOPE)
endfunction()

# Helper function for defining a node module
# After nodejs_init, all of the settings and dependencies necessary to do
# this yourself are defined, but this helps make sure everything is configured
# correctly. Feel free to use it as a model to do this by hand (or to
# tweak this configuration if you need something custom).
function(add_nodejs_module NAME)
    # Validate name parameter (must be a valid C identifier)
    string(MAKE_C_IDENTIFIER ${NAME} ${NAME}_SYMBOL_CHECK)
    if(NOT "${NAME}" STREQUAL "${${NAME}_SYMBOL_CHECK}")
        message(FATAL_ERROR
            "Module name must be a valid C identifier. "
            "Suggested alternative: '${${NAME}_SYMBOL_CHECK}'"
        )
    endif()
    # Make sure node is initialized (variables set) before defining the module
    if(NOT NODEJS_INIT)
        message(FATAL_ERROR
            "Node.js has not been initialized. "
            "Call nodejs_init before adding any modules"
        )
    endif()
    # In order to match node-gyp, we need to build into type specific folders
    # ncmake takes care of this, but be sure to set CMAKE_BUILD_TYPE yourself
    # if invoking CMake directly
    if(NOT CMAKE_CONFIGURATION_TYPES AND NOT CMAKE_BUILD_TYPE)
        message(FATAL_ERROR
            "Configuration type must be specified. "
            "Set CMAKE_BUILD_TYPE or use a different generator"
        )
    endif()

    # A node module is a shared library
    add_library(${NAME} SHARED ${NODEJS_SOURCES} ${ARGN})
    # Add compiler defines for the module
    # Two helpful ones:
    # MODULE_NAME must match the name of the build library, define that here
    # ${NAME}_BUILD is for symbol visibility under windows
    string(TOUPPER "${NAME}_BUILD" ${NAME}_BUILD_DEF)
    target_compile_definitions(${NAME}
        PRIVATE MODULE_NAME=${NAME}
        PRIVATE ${${NAME}_BUILD_DEF}
        PUBLIC ${NODEJS_DEFINITIONS}
    )
    # This properly defines includes for the module
    target_include_directories(${NAME} PUBLIC ${NODEJS_INCLUDE_DIRS})

    # Add link flags to the module
    target_link_libraries(${NAME} ${NODEJS_LIBRARIES})

    # Set required properties for the module to build properly
    # Correct naming, symbol visiblity and C++ standard
    set_target_properties(${NAME} PROPERTIES
        OUTPUT_NAME ${NAME}
        PREFIX ""
        SUFFIX ".node"
        MACOSX_RPATH ON
        C_VISIBILITY_PRESET hidden
        CXX_VISIBILITY_PRESET hidden
        POSITION_INDEPENDENT_CODE TRUE
        CMAKE_CXX_STANDARD_REQUIRED TRUE
        CXX_STANDARD 11
    )

    # Handle link flag cases properly
    # When there are link flags, they should be appended to LINK_FLAGS with space separation
    # If the list is emtpy (true for most *NIX platforms), this is a no-op
    foreach(NODEJS_LINK_FLAG IN LISTS NODEJS_LINK_FLAGS)
        set_property(TARGET ${NAME} APPEND_STRING PROPERTY LINK_FLAGS " ${NODEJS_LINK_FLAG}")
    endforeach()

    # Make sure we're buiilding in a build specific output directory
    # Only necessary on single-target generators (Make, Ninja)
    # Multi-target generators do this automatically
    # This (luckily) mirrors node-gyp conventions
    if(NOT CMAKE_CONFIGURATION_TYPES)
        set_property(TARGET ${NAME} PROPERTY
            LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BUILD_TYPE}
        )
    endif()
endfunction()
