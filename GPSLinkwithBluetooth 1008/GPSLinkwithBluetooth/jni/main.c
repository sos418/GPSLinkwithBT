//
// Created by 沈冠宇 on 2016/10/27.
//

#include <jni.h>
#include "rtklib.h"
#include <android/log.h>

JNIEXPORT jstring JNICALL
Java_com_example_gpslinkwithbluetooth_MainActivity_stringFromJNI(JNIEnv *env, jobject instance) {

    // TODO
    rtcm_t rtcm;
    rtcm_t *Prtcm = &rtcm;

    FILE *file;

    char s[10];

    file = fopen("/sdcard/GPSData/test6.txt","rb");

    init_rtcm(Prtcm);
    input_rtcm2f(Prtcm,file);


//    rtcmfile = fopen("/sdcard/GPSData/rtcm.txt","w");
//    const char *text = "SSSS";
//    fprintf(rtcmfile,"HELLO\n");
//    fprintf(rtcmfile,"SSAAA%s\n",text);

    fclose(file);

    return (*env)->NewStringUTF(env, s);
}

