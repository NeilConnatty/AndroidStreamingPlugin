//
// Created by Neil Connatty on 2016-10-26.
//

#ifndef STREAMINGPLUGIN_MESSENGER_H
#define STREAMINGPLUGIN_MESSENGER_H

#include <string>
#include "pubnub_common.hpp"
#include "c-core/core/pubnub_api_types.h"

namespace messaging
{
    class messenger {
    public:
        /** returns null on unsuccessful start */
        pubnub::context* start_messenger ();
        /** returns null on unsuccessful start */
        pubnub::context* start_input ();
        /** returns empty string if no message */
        void subscribe_to_messages (pubnub::context *pb,
                                   std::function<void(pubnub::context &, pubnub_res)> callback);
        void subscribe_to_input (pubnub::context *pb,
                                 std::function<void(pubnub::context &, pubnub_res)> callback);
    };
}

#endif //STREAMINGPLUGIN_MESSENGER_H
