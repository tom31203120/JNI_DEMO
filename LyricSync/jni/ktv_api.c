#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include <android/log.h>
#include "calc_score.h"
#include "ktv_api.h"
#define  dbg_log(...)  __android_log_print( ANDROID_LOG_DEBUG, "video_log",   __VA_ARGS__)

//==========================================================================================================
CalcCtx *g_calc_ctx = NULL;

void Java_com_thunderstone_ktv_KtvApi_NativeCalcInit(JNIEnv* env, jobject thiz ,jstring music_data)
{
    if (g_calc_ctx){
        dbg_log("g_calc_ctx  =%p\n", g_calc_ctx);
        dbg_log("Init failed,already inited\n");
        /*calc_uninit(g_calc_ctx);*/
        return;
    }

    char *str = (char *) (*env)->GetStringUTFChars(env, music_data, 0);
    g_calc_ctx = calc_init(str);
    dbg_log("g_calc_ctx  =%p\n", g_calc_ctx);
    dbg_log("g_calc_ctx->min  =%lf\n", g_calc_ctx->avg_min);
    dbg_log("g_calc_ctx->mid  =%lf\n", g_calc_ctx->avg_mid);
    dbg_log("g_calc_ctx->max  =%lf\n", g_calc_ctx->avg_max);
    dbg_log("g_calc_ctx->max_frame  =%d\n", g_calc_ctx->max_frame);
    S_LIST *pHead = g_calc_ctx->map_list;
    dbg_log("list->head =%p\n", pHead);
    pHead = pHead->pnext;
    dbg_log("list =%p\n", pHead);
    dbg_log("list->avg_val =%lf\n", pHead->avg_val);
    dbg_log("list->end_time=%lf\n", pHead->end_time);
    pHead = pHead->pnext;
    dbg_log("list =%p\n", pHead);
    dbg_log("list->avg_val =%lf\n", pHead->avg_val);
    dbg_log("list->end_time=%lf\n", pHead->end_time);

    dbg_log("file name =%s\n", str);
}

jint Java_com_thunderstone_ktv_KtvApi_NativeCalcScore(JNIEnv* env, jobject thiz , jbyteArray framedata, jdouble start_time)
{
    /*dbg_log("in NativeCalcScore\n");*/
    /*dbg_log("start_time=%lf\n", start_time);*/
    jbyte *byte_data = (*env)->GetByteArrayElements(env, framedata, 0);
    short *frame_stream = (short *)(char *)byte_data;
    /*dbg_log("frame_stream = %p\n", frame_stream);*/

    int len = (int)(*env)->GetArrayLength(env, framedata);
    /*dbg_log("len=%d\n", len);*/
    /*char *tmp = (char*)frame_stream;*/
    
    /*dbg_log("char = %x\n", tmp[0]);*/
    /*dbg_log("char = %x\n", tmp[1]);*/
    /*dbg_log("char = %x\n", tmp[2]);*/
    /*dbg_log("char = %x\n", tmp[3]);*/

    /*dbg_log("short = %x\n", frame_stream[0]);*/
    /*dbg_log("short = %x\n", frame_stream[1]);*/

    calc_score(frame_stream, len/2, start_time, g_calc_ctx);

    /*dbg_log("cur_score =%d\n", (int)(g_calc_ctx->cur_score*100));*/

    (*env)->ReleaseByteArrayElements(env, framedata, byte_data, 0);
    return (jint)(g_calc_ctx->total_score * 100);
}

void Java_com_thunderstone_ktv_KtvApi_NativeCalcUnInit(JNIEnv* env, jobject thiz)
{
    dbg_log("in NativeCalcUnInit\n");
    if (g_calc_ctx)
        calc_uninit(g_calc_ctx);
    g_calc_ctx = NULL;
}

