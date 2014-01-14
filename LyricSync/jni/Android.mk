# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#LOCAL_PATH := $(call my-dir)

#include $(CLEAR_VARS)

#LOCAL_MODULE    := hello-jni
#LOCAL_SRC_FILES := hello-jni.c
#LOCAL_LDLIBS=-lm  -ldl -llog  -lz -g -shared

#include $(BUILD_SHARED_LIBRARY)

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE := json
LOCAL_SRC_FILES := json-c/printbuf.c json-c/linkhash.c json-c/json_tokener.c json-c/json_util.c json-c/json_object.c json-c/debug.c json-c/arraylist.c
LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/json-c
include $(BUILD_STATIC_LIBRARY)

include $(CLEAR_VARS)
LOCAL_MODULE    := calc_score
LOCAL_SRC_FILES := ktv_api.c calc_score.c
LOCAL_STATIC_LIBRARIES := json
LOCAL_LDLIBS=-lm  -ldl -llog  -lz -g -shared
include $(BUILD_SHARED_LIBRARY)
