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
    pubnub::context* msg_context = 0;
    pubnub::context* input_context = 0;
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

    void send_input_to_activity (const char* str)
    {
        JNIEnv* jni_env = 0;
        java_vm->AttachCurrentThread(&jni_env, 0);

        jclass messenger_cls = jni_env->GetObjectClass(j_messenger);
        j_method = jni_env->GetMethodID(messenger_cls, "receiveInput", "(Ljava/lang/String;)V");
        jni_env->CallVoidMethod(j_messenger, j_method, jni_env->NewStringUTF(str));

        java_vm->DetachCurrentThread();
    }

    void on_subscribe (pubnub::context &pb, pubnub_res res)
    {
        if (PNR_OK == res) {
            send_message_to_activity(pb.get().c_str());
            messenger->subscribe_to_messages(&pb, on_subscribe);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "on_subscribe", "error subscribing, error code %d", res);
            messenger->subscribe_to_messages(&pb, on_subscribe);
        }
    }

    void on_input (pubnub::context &pb, pubnub_res res)
    {
        if (PNR_OK == res) {
            send_input_to_activity(pb.get().c_str());
            messenger->subscribe_to_input(&pb, on_input);
        } else {
            __android_log_print(ANDROID_LOG_ERROR, "on_input", "error subscribing, error code %d", res);
            messenger->subscribe_to_input(&pb, on_input);
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

        msg_context = messenger->start_messenger();
        if (msg_context == 0) {
            __android_log_print(ANDROID_LOG_ERROR, "messaging_bridge", "error creating messaging context");
            delete messenger;
            return  0;
        }

        input_context = messenger->start_input();
        if (input_context == 0) {
            __android_log_print(ANDROID_LOG_ERROR, "messaging_bridge",
                                "error creating input context");
            delete messenger;
            delete msg_context;
            return 0;
        }

        __android_log_print(ANDROID_LOG_DEBUG, "messaging_bridge", "starting message loop");
        messenger->subscribe_to_messages(msg_context, on_subscribe);
        __android_log_print(ANDROID_LOG_DEBUG, "messaging_bridge", "starting input loop");
        messenger->subscribe_to_input(input_context, on_input);

        return 1;
    }

    void Java_com_bcch_neilconnatty_streamingplugin_messaging_Messenger_stopMessenger
            (JNIEnv* env, jobject)
    {
        delete messenger;
        delete msg_context;
    }
}