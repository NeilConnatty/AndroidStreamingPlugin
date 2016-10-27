cmake_minimum_required(VERSION 3.4.1)

set (CPP src/main/cpp)

set(MESSAGING_FILES
    ${CPP}/messaging_bridge.cpp
    ${CPP}/messenger.cpp
    ${CPP}/message_formatting_helper.cpp )

set (PUBNUB_FILES
        ${CPP}/c-core/cpp/pubnub.hpp
        ${CPP}/c-core/cpp/pubnub_common.hpp
        ${CPP}/c-core/cpp/pubnub_mutex.hpp

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
        ${CPP}/c-core/core/pubnub_coreapi.h

        ${CPP}/c-core/posix/pubnub_version_posix.c
        ${CPP}/c-core/posix/pubnub_generate_uuid_posix.c
        ${CPP}/c-core/posix/pbpal_posix_blocking_io.c
        ${CPP}/c-core/posix/monotonic_clock_get_time_posix.c )
set (CALLBACK_FILES
        ${CPP}/c-core/cpp/pubnub_futres_posix.cpp
        ${CPP}/c-core/core/pubnub_timer_list.c
        ${CPP}/c-core/posix/pubnub_ntf_callback_posix.c
        ${CPP}/c-core/posix/pubnub_get_native_socket.c )
set (SYNC_FILES
        ${CPP}/c-core/cpp/pubnub_futres_sync.cpp
        ${CPP}/c-core/core/pubnub_ntf_sync.c )

add_library (StreamingPluginBridge
             SHARED
             ${CPP}/StreamingPluginBridge.cpp )

add_library (MessagingService
             SHARED
             ${MESSAGING_FILES}
             ${PUBNUB_FILES}
             ${CALLBACK_FILES})

#add_library (PubNub
 #            SHARED
  #           ${PUBNUB_FILES}
   #          ${CALLBACK_FILES})

add_definitions (
        -D PUBNUB_THREADSAFE
        -D PUBNUB_CALLBACK_API
)

include_directories(${CPP}/include)
include_directories(${CPP}/c-core/cpp)
include_directories(${CPP}/c-core/posix)
include_directories(${CPP}/c-core/core)
include_directories(${CPP}/c-core/lib/base64)
include_directories(${CPP}/c-core/lib/sockets)

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

target_link_libraries (StreamingPluginBridge ${log-lib})

target_link_libraries (MessagingService ${log-lib})

#target_link_libraries (MessagingService ${PubNub})