//
// Created by Neil Connatty on 2016-10-26.
//

#include "messenger.h"
#include "pubnub.hpp"
#include "android/log.h"
#include "message_formatting_helper.h"
#include <exception>
#include <string>

#define PUBLISH_KEY "pub-c-ffee06f7-78b5-483c-b800-fae8f3a67f0c"
#define SUBSCRIBE_KEY "sub-c-3dc063c8-9bc0-11e6-814f-0619f8945a4f"
#define CHANNEL "bcch"
#define INITIAL_PUBLISH "Successfully connected to messaging service"

using namespace pubnub;
using namespace messaging;

pubnub::context* messenger::start_messenger()
{  try {
        pubnub::context *pb = new pubnub::context(PUBLISH_KEY, SUBSCRIBE_KEY);
        pubnub_res res;

        res = pb->subscribe(CHANNEL).await();
        if (PNR_OK == res) {
            __android_log_print(ANDROID_LOG_INFO, "messenger::start_messenger",
                                "Successfully subscribed");
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "messenger::start_messenger",
                                "Error on subscribe with code: %d", res);
            delete pb;
            return 0;
        }

        formatting_helper helper;
        res = pb->publish(CHANNEL, helper.format_message(INITIAL_PUBLISH)).await();
        if (PNR_OK == res) {
            return pb;
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "messenger::start_messenger",
                                "Error on initial publish with code: %d", res);
            delete pb;
            return 0;
        }
    } catch (std::exception &exc) {
        __android_log_print(ANDROID_LOG_ERROR, "messenger::start_messenger", "%s", exc.what());
    }
}

std::string messenger::get_latest_message(pubnub::context* pb)
{
    try {
        pubnub_res res = pb->subscribe(CHANNEL).await();
        if (PNR_OK == res) {
            return pb->get();
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "messenger::get_latest_message",
                                "Error on subscribe with code: %d", res);
            return "";
        }
    } catch (std::exception &exc) {
        __android_log_print(ANDROID_LOG_ERROR, "messenger::get_latest_message", "%s", exc.what());
    }
}