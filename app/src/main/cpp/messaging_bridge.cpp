//
// Created by Neil Connatty on 2016-10-26.
//
#include <jni.h>
#include <android/log.h>
#include <string>
#include "pubnub.hpp"
#include "messenger.h"

extern "C"
{
    pubnub::context* context = 0;
    messaging::messenger* messenger = 0;
    jobject j_messenger;
    jmethodID j_method;
    JavaVM* java_vm;

    jint JNI_OnLoad (JavaVM * vm, void* reserved)
    {
        java_vm = vm;
        return JNI_VERSION_1_6;
    }

    void send_message_to_activity (const char* str)
    {
        JNIEnv* jni_env = 0;
        java_vm->AttachCurrentThread(&jni_env, 0);

        jclass messenger_cls = jni_env->GetObjectClass(j_messenger);
        j_method = jni_env->GetMethodID(messenger_cls, "displayMessage", "(Ljava/lang/String;)V");
        jni_env->CallVoidMethod(j_messenger, j_method, jni_env->NewStringUTF(str));

        java_vm->DetachCurrentThread();
    }

    void on_subscribe (pubnub::context &pb, pubnub_res res)
    {
        if (PNR_OK == res) {
            send_message_to_activity(pb.get().c_str());
            messenger->get_latest_message(&pb, on_subscribe);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "on_subscribe", "error subscribing, error code %d", res);
        }
    }

/** return false on unsuccessful initialize */
    int Java_com_bcch_neilconnatty_streamingplugin_messaging_Messenger_initializeMessenger
            (JNIEnv* env, jobject callingObject)
    {
        j_messenger = env->NewGlobalRef(callingObject);
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
        __android_log_print(ANDROID_LOG_DEBUG, "messaging_bridge", "starting message loop");
        messenger->get_latest_message(context, on_subscribe);
        return 1;
    }

    void Java_com_bcch_neilconnatty_streamingplugin_messaging_Messenger_stopMessenger
            (JNIEnv* env, jobject)
    {
        delete messenger;
        delete context;
    }
}