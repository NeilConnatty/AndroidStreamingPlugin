# Sets the minimum version of CMake required to build the native
# library. You should either keep the default value or only pass a
# value of 3.4.0 or lower.

cmake_minimum_required(VERSION 3.4.1)

set ( CPP src/main/cpp )

set ( PUBNUB_FILES
        ${CPP}/c-core/core/pubnub_coreapi.c
        ${CPP}/c-core/core/pubnub_coreapi_ex.c
        ${CPP}/c-core/core/pubnub_ccore.c
        ${CPP}/c-core/core/pubnub_netcore.c

        ${CPP}/c-core/lib/sockets/pbpal_sockets.c
        ${CPP}/c-core/lib/sockets/pbpal_resolv_and_connect_sockets.c
        ${CPP}/c-core/lib/sockets/pbpal_adns_sockets.c
        ${CPP}/c-core/lib/base64/pbbase64.c

        ${CPP}/c-core/core/pubnub_alloc_std.c
        ${CPP}/c-core/core/pubnub_assert_std.c
        ${CPP}/c-core/core/pubnub_generate_uuid.c
        ${CPP}/c-core/core/pubnub_blocking_io.c
        ${CPP}/c-core/core/pubnub_timers.c
        ${CPP}/c-core/core/pubnub_json_parse.c
        ${CPP}/c-core/core/pubnub_helper.c

        ${CPP}/c-core/posix/pubnub_version_posix.c
        ${CPP}/c-core/posix/pubnub_generate_uuid_posix.c
        ${CPP}/c-core/posix/pbpal_posix_blocking_io.c
        ${CPP}/c-core/posix/monotonic_clock_get_time_posix.c )
set ( CALLBACK_FILES
        ${CPP}/c-core/cpp/pubnub_futres_posix.cpp
        ${CPP}/c-core/core/pubnub_timer_list.c
        ${CPP}/c-core/posix/pubnub_ntf_callback_posix.c
        ${CPP}/c-core/posix/pubnub_get_native_socket.c )
set ( SYNC_FILES
        ${CPP}/c-core/cpp/pubnub_futres_sync.cpp
        ${CPP}/c-core/core/pubnub_ntf_sync.c )

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds it for you.
# Gradle automatically packages shared libraries with your APK.

add_library( # Sets the name of the library.
             native-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
             # Associated headers in the same location as their source
             # file are automatically included.
             ${CPP}/native-lib.cpp )

add_library (   StreamingPluginBridge
                SHARED
                ${CPP}/StreamingPluginBridge.cpp )

add_library (   MessagingService
                SHARED
                ${CPP}/messaging_bridge.cpp
                ${CPP}/messenger.cpp )

add_library (   PubNub
                SHARED
                ${PUBNUB_FILES}
                ${SYNC_FILES} )

add_definitions (
        -D PUBNUB_THREADSAFE
        -D PUBNUB_CALLBACK_API
)

include_directories (${CPP}/include )
include_directories(${CPP}/c-core/cpp)
include_directories(${CPP}/c-core/posix)
include_directories(${CPP}/c-core/core)
include_directories(${CPP}/c-core/lib/base64)
include_directories(${CPP}/c-core/lib/sockets)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because system libraries are included in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in the
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

target_link_libraries ( StreamingPluginBridge
                        ${log-lib} )

target_link_libraries ( MessagingService
                        ${log-lib} )

target_link_libraries ( MessagingService
                        ${PubNub} )