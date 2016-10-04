//
// Created by Neil Connatty on 2016-10-03.
//

#include <stdlib.h>
#include <jni.h>
#include <android/log.h>

extern "C"
{

    JavaVM *        java_vm;
    jobject         StreamingPlugin;
    jmethodID       StartStreamingPlugin;

    jint JNI_OnLoad (JavaVM * vm, void* reserved)
    {
        __android_log_print(ANDROID_LOG_INFO, "StreamingPluginBridge", "[%s] Creating java link vm = %08x\n", __FUNCTION__, vm);
        java_vm = vm;


        JNIEnv* jni_env = 0;
        java_vm->AttachCurrentThread(&jni_env, 0);
        __android_log_print(ANDROID_LOG_INFO, "StreamingPluginBridge", "[%s] JNI Environment is = %08x\n", __FUNCTION__, jni_env);

        jclass      cls_Activity = jni_env->FindClass ("com/unity3d/player/UnityPlayer");
        jfieldID    fid_Activity = jni_env->GetStaticFieldID (cls_Activity, "currentActivity", "Landroid/app/Activity;");
        jobject     obj_Activity = jni_env->GetStaticObjectField (cls_Activity, fid_Activity);
        __android_log_print(ANDROID_LOG_INFO, "StreamingPluginBridge", "[%s] Current activity = %08x\n", __FUNCTION__, obj_Activity);

        jclass      cls_Plugin = jni_env->FindClass ("com/bcch/neilconnatty/libstreamingplugin/StreamingPlugin");
        jmethodID   mid_Plugin = jni_env->GetMethodID (cls_Plugin, "<init>", "(Landroid/app/Activity;)V");
        jobject     obj_Plugin = jni_env->NewObject (cls_Plugin, mid_Plugin, obj_Activity);
        __android_log_print(ANDROID_LOG_INFO, "StreamingPluginBridge", "[%s] Plugin object = %08x\n", __FUNCTION__, obj_Plugin);

        StreamingPlugin = jni_env->NewGlobalRef (obj_Plugin);
        StartStreamingPlugin = jni_env->GetMethodID (cls_Plugin, "StartStreamingPlugin", "()V");
        __android_log_print(ANDROID_LOG_INFO, "StreamingPluginBridge", "[%s] JavaClass global ref = %08x\n", __FUNCTION__, StreamingPlugin);
        __android_log_print(ANDROID_LOG_INFO, "StreamingPluginBridge", "[%s] JavaClass method id = %08x\n", __FUNCTION__, StartStreamingPlugin);

        return JNI_VERSION_1_6;
    }


    void startPlugin ()
    {
        JNIEnv* jni_env = 0;
        java_vm->AttachCurrentThread (&jni_env, 0);

        __android_log_print(ANDROID_LOG_INFO, "StreamingPluginBridge", "[%s] called, attached to %08x\n", __FUNCTION__, jni_env);

        jni_env->CallVoidMethod (StreamingPlugin, StartStreamingPlugin);

        return;
    }

} // extern "C"
