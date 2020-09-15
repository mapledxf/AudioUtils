#ifndef NATIVE_AUDIO_ANDROID_DEBUG_H_H
#define NATIVE_AUDIO_ANDROID_DEBUG_H_H
#include <android/log.h>


#if !defined(NDEBUG)

#if !defined(LOG_TAG)
#define LOG_TAG "NATIVE-AUDIO"
#endif

#define LOGV(...) \
  __android_log_print(ANDROID_LOG_VERBOSE, LOG_TAG, __VA_ARGS__)
#define LOGD(...) \
  __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__)
#define LOGI(...) \
  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGW(...) \
  __android_log_print(ANDROID_LOG_WARN, LOG_TAG, __VA_ARGS__)
#define LOGE(...) \
  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGF(...) \
  __android_log_print(ANDROID_LOG_FATAL, LOG_TAG, __VA_ARGS__)

#else

#define LOGV(...)
#define LOGD(...)
#define LOGI(...)
#define LOGW(...)
#define LOGE(...)
#define LOGF(...)
#endif

#endif  // NATIVE_AUDIO_ANDROID_DEBUG_H_H