#include <stdio.h>
#include <stdlib.h>
#include <stddef.h>
#include <string.h>
#include <assert.h>
#include <math.h>

#include <jni.h>
#include <android/log.h>
#define  dbg_log(...)  __android_log_print( ANDROID_LOG_DEBUG, "video_log",   __VA_ARGS__)

#include "json-c/json.h"
#include "calc_score.h"

#define ERR_JSON_OBJ ((json_object*)-1)

S_LIST *get_head_node(S_LIST *plist, int start_time)
{
    S_LIST *pnode = plist->pnext;
    if (!pnode) return pnode;
    while(pnode->end_time < start_time)
    {
        plist->pnext = pnode->pnext;
        free(pnode);
        pnode = plist->pnext;
    }
    return pnode;
}

int build_context(json_object* jb, CalcCtx *pCtx)
{
    json_object *obj, *x_obj, *y_obj;
    int i, len;
    pCtx->avg_mid = 0;

    obj = json_object_object_get(jb, "max");
    pCtx->avg_max = json_object_get_double(obj);

    obj = json_object_object_get(jb, "min");
    pCtx->avg_min = json_object_get_double(obj);

    obj = json_object_object_get(jb, "mean");
    pCtx->avg_mid = json_object_get_double(obj);

    obj = json_object_object_get(jb, "maxframe");
    pCtx->max_frame = json_object_get_int(obj);

    x_obj = json_object_object_get(jb, "x");
    y_obj = json_object_object_get(jb, "y");
    assert(json_type_array == json_object_get_type(x_obj));
    assert(json_type_array == json_object_get_type(y_obj));

    len = json_object_array_length(x_obj);
    assert(len == json_object_array_length(y_obj));

    S_LIST *plist = (S_LIST *)malloc(sizeof(S_LIST));
    if (!plist)
    {
        printf("error malloc \n");
        return -1;
    }
    memset(plist, 0, sizeof(S_LIST));
    S_LIST *plast = plist;

    for (i = 0; i < len; i++)
    {
        S_LIST *pnode = (S_LIST *)malloc(sizeof(S_LIST));
        if (!pnode)
        {
            printf("error malloc \n");
            return -1;
        }
        memset(pnode, 0, sizeof(S_LIST));
        obj = json_object_array_get_idx(x_obj, i);
        pnode->end_time = json_object_get_double(obj);

        obj = json_object_array_get_idx(y_obj, i);
        pnode->avg_val = json_object_get_double(obj);

        pnode->pnext = NULL;

        plast->pnext = pnode;
        plast = pnode;
    }
    pCtx->total_score = 0;
    pCtx->cur_score   = 0;
    pCtx->idx_rec_num = 0;
    pCtx->total_rec_num = len;
    pCtx->map_list = plist;

    return 0;
}

CalcCtx *calc_init(char music_name[])
{
    json_object *jb = json_object_from_file(music_name);
    if (jb == ERR_JSON_OBJ)
    {
        printf("could not find the Muisic Data File\n");
        return NULL;
    }

    CalcCtx *pCtx = (CalcCtx*)malloc(sizeof(CalcCtx));
    if (!pCtx)
    {
        printf("error malloc \n");
        return NULL;
    }

    memset(pCtx, 0, sizeof(CalcCtx));
    int ret = build_context(jb, pCtx);
    if (ret)
    {
        printf("could not find the Muisic Data File\n");
        return NULL;
    }

    json_object_put(jb);

    return pCtx;
}

void calc_uninit(CalcCtx *pInCtx)
{
    // free list
    assert(NULL != pInCtx);
    S_LIST *pdel, *pnode = pInCtx->map_list;
    while(pnode->pnext)
    {
        pdel = pnode->pnext;
        dbg_log("slist node =%p\n", pdel);
        free(pnode);
        pnode = pdel;
    }

    if(pnode)
        free(pnode);

    free(pInCtx);
    return;
}

int bin_zation(double val, CalcCtx* pCtx)
{
    int binarys= 0, num_gap;
    double maximal = pCtx->avg_max;
    double mean    = pCtx->avg_mid;
    double minimal = pCtx->avg_min;
    double top, bottom, top_gap, bottom_gap; 

    top    = maximal - mean;
    bottom = mean - minimal;

    top_gap = top * 2 / BINARY_COUNT;
    bottom_gap = bottom * 2 / BINARY_COUNT;

    if (val > mean){
        num_gap = (int)((val - mean) / top_gap);
        binarys = BINARY_COUNT / 2 + num_gap + 1;
    }
    else{
        num_gap = (int)((mean - val) / bottom_gap);
        binarys = BINARY_COUNT / 2 - num_gap + 1;
    }

    return binarys;
}

int finish_calc(CalcCtx *pCtx)
{
    return pCtx->idx_rec_num >= pCtx->total_rec_num;
}

int calc_score(short frame_stream[], int len, double start_time, CalcCtx *pCtx)
{
    double sum=0, avg_val = 0;    
    double score = 0;
    int i;
    S_LIST *pHead = get_head_node(pCtx->map_list, start_time);
    if (pHead && start_time>pHead->end_time)
    {
        for(i=0;i<len;i++)
        {
            sum += fabs(1.0 * frame_stream[i])/pCtx->max_frame;
            /*avg_val = (avg_val*(i-1)+frame_stream[i])/i;*/
            /*avg_val = avg_val*(i-1)/i+frame_stream[i]/i;*/
        }
        avg_val = sum / len;
        //二值化数据1~10
        score = bin_zation(avg_val, pCtx);
        //和服务器端的计算结果比较
        printf("server=%lf,    client=%lf\n",pHead->avg_val, score);
        dbg_log("server=%lf,    client=%lf\n",pHead->avg_val, score);
        score = score/pHead->avg_val;
        if (score > 1) score = 1/score;
        //记录当前得分，并综合总得分
        pCtx->cur_score   = score;
        pCtx->total_score = \
            (pCtx->cur_score + pCtx->total_score*pCtx->idx_rec_num)/(pCtx->idx_rec_num+1);

        dbg_log("current score= %lf\n", pCtx->cur_score*100);
        dbg_log("total   score= %lf\n", pCtx->total_score*100);

        //维护上下文，方便下次计算
        pCtx->idx_rec_num ++;
        pCtx->map_list->pnext = pHead->pnext;
        free(pHead);
        return 1;
    }
    return 0;
}
