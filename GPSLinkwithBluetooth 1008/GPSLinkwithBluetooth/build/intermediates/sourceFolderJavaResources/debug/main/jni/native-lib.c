#include <jni.h>

JNIEXPORT jstring JNICALL
Java_com_example_gpslinkwithbluetooth_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO


    return (*env)->NewStringUTF(env, "HELLO");
}