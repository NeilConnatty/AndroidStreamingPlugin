//
// Created by Neil Connatty on 2016-10-26.
//

#include "messenger.h"
#include "android/log.h"
#include <exception>
#include <thread>

#define PUBLISH_KEY "pub-c-ffee06f7-78b5-483c-b800-fae8f3a67f0c"
#define SUBSCRIBE_KEY "sub-c-3dc063c8-9bc0-11e6-814f-0619f8945a4f"
#define MSG_CHANNEL "bcch"
#define INPUT_CHANNEL "input"

using namespace messaging;

pubnub::context* messenger::start_messenger()
{
    try {
        pubnub::context *pb = new pubnub::context(PUBLISH_KEY, SUBSCRIBE_KEY);
        pubnub_res res;

        res = pb->subscribe(MSG_CHANNEL).await();
        if (PNR_OK == res) {
            __android_log_print(ANDROID_LOG_INFO, "messenger::start_messenger",
                                "Successfully subscribed");
            return pb;
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "messenger::start_messenger",
                                "Error on subscribe with code: %d", res);
            delete pb;
            return 0;
        }
    } catch (std::exception &exc) {
        __android_log_print(ANDROID_LOG_ERROR, "messenger::start_messenger", "%s", exc.what());
    }
}

pubnub::context* messenger::start_input()
{
    try {
        pubnub::context *pb = new pubnub::context(PUBLISH_KEY, SUBSCRIBE_KEY);
        pubnub_res res;

        res = pb->subscribe(INPUT_CHANNEL).await();
        if (PNR_OK == res) {
            __android_log_print(ANDROID_LOG_INFO, "messenger::start_input",
                                "Successfully subscribed");
            return pb;
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "messenger::start_input",
                                "Error on subscribe with code: %d", res);
            delete pb;
            return 0;
        }
    } catch (std::exception &exc) {
        __android_log_print(ANDROID_LOG_ERROR, "messenger::input", "%s", exc.what());
    }
}

void get_message (pubnub::context *pb, std::function<void(pubnub::context &, pubnub_res)> callback)
{
    pb->subscribe(MSG_CHANNEL).then(callback);
}


void messenger::subscribe_to_messages(pubnub::context *pb,
                                      std::function<void(pubnub::context &, pubnub_res)> callback)
{
    std::thread t(get_message, pb, callback);
    t.detach();
}

void get_input (pubnub::context *pb, std::function<void(pubnub::context &, pubnub_res)> callback)
{
    pb->subscribe(INPUT_CHANNEL).then(callback);
}

void messenger::subscribe_to_input(pubnub::context *pb,
                                   std::function<void(pubnub::context &, pubnub_res)> callback)
{
    std::thread t(get_input, pb, callback);
    t.detach();
}