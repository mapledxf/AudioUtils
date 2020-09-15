#include <cstdlib>
#include <cassert>
#include <jni.h>
#include <cstring>
#include <pthread.h>
#include <cstdio>
#include "AndroidDebug.h"
#include "mobvoi_dsp.h"

#include <string>

#define kSampleRate (16000)
#define BYTE_SIZE (2)
#define CHANNEL_NUM (8)
#define MIC_NUM (1)
#define REF_NUM (1)

static void *dsp_ = nullptr;
static int16_t *mic_buffer_ = nullptr;
static int16_t *ref_buffer_ = nullptr;
static int16_t *out_buffer_ = nullptr;
static int out_size_ = 0;

static int DisableBeamforming(void *dsp) {
    mob_bf_param p;
    p.enable = 0;
    p.angles = nullptr;
    p.weights = nullptr;
    p.block_mat_weights = nullptr;
    int ret = mobvoi_uplink_process_ctl(dsp, SET_BF_PARAM, &p);
    return ret;
}

static int16_t *prepare_buffer(int size, int16_t **buffer) {
    if (*buffer == nullptr) {
        *buffer =
                reinterpret_cast<int16_t *>(calloc(sizeof(int16_t) * size + sizeof(int), 1));
        *(reinterpret_cast<int *>(*buffer)) = size;
    } else if (*(reinterpret_cast<int *>(*buffer)) < size) {
        free(*buffer);
        *buffer =
                reinterpret_cast<int16_t *>(calloc(sizeof(int16_t) * size + sizeof(int), 1));
        *(reinterpret_cast<int *>(*buffer)) = size;
    }

    return *buffer + sizeof(int) / sizeof(int16_t);
}

extern "C" {

JNIEXPORT void JNICALL
Java_com_ebo_voice_audio_process_jni_EcnrConnector_initDSP(JNIEnv *env, jclass jclz,
                                                           jstring configPath) {
    if (dsp_ != nullptr) {
        mobvoi_uplink_cleanup(dsp_);
    }

    delete[] mic_buffer_;

    delete[] ref_buffer_;

    delete[] out_buffer_;

    dsp_ = mobvoi_uplink_init(10, kSampleRate, MIC_NUM, kSampleRate,
                              REF_NUM, 1, 2);
    const char *cstr = env->GetStringUTFChars(configPath, nullptr);
    mobvoi_uplink_process_ctl(dsp_, SET_UPLINK_CONFIG_DIR, const_cast<char *>(cstr));
    env->ReleaseStringUTFChars(configPath, cstr);
    DisableBeamforming(dsp_);
    mobvoi_uplink_aec_resume(dsp_, 0);
    mobvoi_uplink_aec_resume(dsp_, 1);
}

JNIEXPORT void JNICALL
Java_com_ebo_voice_audio_process_jni_EcnrConnector_process(JNIEnv *env, jclass jclz,
                                                           jbyteArray audioData, jint realSize) {
    if (nullptr == audioData || realSize <= 0) {
        LOGE("malloc data error ,size is 0 ");
        out_size_ = 0;
        return;
    }
    auto *micDataArr = reinterpret_cast<jbyte *>(malloc(sizeof(jbyte) * realSize));
    env->GetByteArrayRegion(audioData, 0, realSize, micDataArr);
    int dataSize = realSize;

    int block_size = dataSize / (CHANNEL_NUM * BYTE_SIZE);
    int mic_size = block_size * MIC_NUM;
    int ref_size = block_size * REF_NUM;
    const auto *raw = (const int16_t *) micDataArr;

    int16_t *mic_buffer = prepare_buffer(mic_size, &mic_buffer_);
    int16_t *ref_buffer = prepare_buffer(ref_size, &ref_buffer_);

    for (int i = 0; i < block_size; i++) {
        mic_buffer[i] = raw[i * CHANNEL_NUM + 4];
        ref_buffer[i] = raw[i * CHANNEL_NUM];
    }

    mobvoi_uplink_send_ref_frames(dsp_, ref_buffer, ref_size, REF_NUM, 0);
    int16_t *out_buffer = prepare_buffer(block_size * 2, &out_buffer_);
    out_size_ = mobvoi_uplink_process(dsp_, mic_buffer, mic_size,
                                      MIC_NUM, 0, out_buffer, 1);

    free(micDataArr);
}

JNIEXPORT jbyteArray JNICALL
Java_com_ebo_voice_audio_process_jni_EcnrConnector_getStreamForEcnr(JNIEnv *env, jclass jclz) {
    auto *result = (jbyte *) (((const char *) out_buffer_) + sizeof(int));
    jbyteArray j_array = env->NewByteArray(out_size_);
    env->SetByteArrayRegion(j_array, 0, out_size_, result);
    return j_array;
}
}

