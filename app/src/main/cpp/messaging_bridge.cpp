//
// Created by Neil Connatty on 2016-10-26.
//
#include <jni.h>
#include <android/log.h>
#include <string>
#include "pubnub.hpp"
#include "messenger.h"

pubnub::context* context = 0;
messaging::messenger* messenger = 0;

extern "C"
{
    /** return false on unsuccessful initialize */
    int Java_com_bcch_neilconnatty_streamingplugin_activities_MainActivity_initializeMessenger
            (JNIEnv* env, jobject)
    {
        messenger = new messaging::messenger;
        if (messenger == 0) {
            __android_log_print(ANDROID_LOG_ERROR, "messaging_bridge", "error creating messenger");
            return 0;
        }

        context = messenger->start_messenger();
        if (context == 0) {
            __android_log_print(ANDROID_LOG_ERROR, "messaging_bridge", "error creating context");
            delete messenger;
            return  0;
        }

        return 1;
    }

    /** return empty string on no new messages or on error */
    jstring Java_com_bcch_neilconnatty_streamingplugin_activities_MainActivity_retrieveNewMessage
            (JNIEnv* env, jobject /* this */)
    {
        __android_log_print(ANDROID_LOG_INFO, "messaging_bridge", "retrieveNewMessage() called");
        std::string str = messenger->get_latest_message(context);
        return env->NewStringUTF(str.c_str());
    }


    void Java_com_bcch_neilconnatty_streamingplugin_activities_MainActivity_stopMessenger
            (JNIEnv* env, jobject)
    {
        delete messenger;
        delete context;
    }
}