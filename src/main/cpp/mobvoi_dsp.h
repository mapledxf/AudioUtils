// Copyright 2017 Mobvoi Inc. All Rights Reserved.
// Author: jxli@mobvoi.com (Jiaxiang Li)
//         congfu@mobvoi.com (Cong Fu)

#ifndef SDK_PIPELINE_MOBVOI_DSP_H_
#define SDK_PIPELINE_MOBVOI_DSP_H_

#ifdef __cplusplus
extern "C" {
#endif

#ifndef __QCC5121__
#include <stdint.h>

#else
typedef int16 int16_t;
#endif


enum mobvoi_dsp_error_code {
  MOB_DSP_ERROR_NONE = 0,
  MOB_DSP_ERROR_CONFIG = 1,
  MOB_DSP_ERROR_RESULT = 2,
};

typedef enum {
  SET_UPLINK_CONFIG_DIR = 0,
  GET_UPLINK_CONFIG_DIR,
  SET_DE_PARAM,
  GET_DE_PARAM,
  SET_AEC_PARAM,
  GET_AEC_PARAM,
  SET_ES_PARAM,
  GET_ES_PARAM,
  SET_NE_PARAM,
  GET_NE_PARAM,
  SET_NR_PARAM,
  GET_NR_PARAM,
  SET_SIG_CPR_PARAM,
  GET_SIG_CPR_PARAM,
  SET_AGC_PARAM,
  GET_AGC_PARAM,
  SET_DOA_PARAM,
  GET_DOA_RESULT,
  SET_BF_PARAM,
  GET_BF_PARAM,
  SET_MULTI_BF_PARAM,
  SET_BF_POST_PROCESS,
  GET_BF_POST_PROCESS,
  SET_UPLINK_DRC_PARAM,
  GET_UPLINK_DRC_PARAM,
  SET_UPLINK_MULTI_DRC_PARAM,
  GET_UPLINK_MULTI_DRC_PARAM,
  SET_UPLINK_PARAM,
  GET_UPLINK_PARAM,
  STOP_DOA_SMOOTH,
  SET_WPE_PARAM,
  GET_WPE_PARAM,
  GET_AEC_INFO,
  SET_T60_ES_PARAM,
  GET_T60_ES_PARAM,
  SET_UPLINK_DUMP,
  RESUME_AEC,
  PAUSE_AEC,
  SET_UPLINK_DUMP_PARAM,
  SET_GWPE_PARAM,
  GET_GWPE_PARAM,
  UPLINK_CTRL_MAX
} mob_uplink_ctrl;

typedef enum {
  SET_DOWNLINK_CONFIG_DIR = 0,
  GET_DOWNLINK_CONFIG_DIR,
  SET_DOWNLINK_DRC_PARAM,
  GET_DOWNLINK_DRC_PARAM,
  SET_EQ_PARAM,
  GET_EQ_PARAM,
  SET_EQ_PARAM_EXT,
  GET_EQ_PARAM_EXT,
  SET_DOWNLINK_PARAM,
  GET_DOWNLINK_PARAM,
  DOWNLINK_CTRL_MAX
} mob_downlink_ctrl;

typedef enum {
  FORMAT_BLOCK = 0,   // AAABBBCCC
  FORMAT_CROSS        // ABCABCABC
} AudioFormat;

/******************** uplink algorithms **************************/

typedef struct {
  int mic_fix_gain;
  int spk_fix_gain;
  AudioFormat mic_in_format;
  AudioFormat spk_in_format;
  AudioFormat mic_out_format;
  int thread_num;
} mob_uplink_param;

typedef struct {
  int dump_on_start;
  char dump_folder[256];
} mob_dump_param;

typedef struct {
  int history_size;
  int lookahead;
  int use_fixed_delay;
} mob_de_param;

typedef enum {
  AEC_FILTER_NLMS,
  AEC_FILTER_APA,
  AEC_FILTER_MIX,
  AEC_FILTER_MULTI_NLMS
} aec_filter_type;

typedef struct {
  aec_filter_type ftype;
  int echo_len_h;
  int echo_len_l;
  float mu_h;
  float mu_l;
} mob_aec_param;

typedef struct {
  int dtd_flag;
  float erle;
} mob_aec_info;

typedef struct {
  int rt60;
  int floor_band;
  int ceil_band;
  int segments;
  int* bands;
  int* late_revbs;
} mob_wpe_param;

// TODO(chehl): Spline is not a type of DOA,
// it is a type of angle calculation method.
// Will choose a better method according to the DOA performance later.
typedef enum { DOA_LINEAR = 1, DOA_CIRCLE = 2, DOA_SPLINE = 3} DOAType;
typedef enum { SNR = 1, FREQ_FLUX = 2, FREQ_ENTROPY = 3 } VADType;

typedef struct {
  DOAType doa_type;
  int segment_len;
  int num_src_detect;
  float smooth_factor;
  float mic_interval;
  float pre_angle;
  int do_es;
  int pre_converge_es;
  int level_es;
  int flag_noise_fill;
  int do_nr;
  int level_nr;
  int do_vad;
  VADType vad_type;
} mob_doa_param;

typedef struct {
  int offset;
  float angle;
} mob_doa_result;

#define INVALID_ANGLE_INDEX (-1)
#define INVALID_ANGLE (-720)

typedef enum {
  SELECT_ONE = 0,
  MEAN = 1,
  FIXED_BEAM = 2,
  MULTI_FIXED_BEAM = 3,
  GSC = 4,
  MULTI_TRANSFUN = 5,
  DUAL_MULTI_FIXED_BEAM = 6
} BFType;

typedef struct {
  BFType bf_type;
  int   weights_num;
  int   enable;
  float tolerance;
  int   block_mat_size;
  float select_angle;
  float snr_filter;
  float mu;
  float gamma;
  float beam_width;
  float* angles;
  float* weights;
  float* block_mat_weights;
  int   weights_ext_mem;
} mob_bf_param;

typedef struct {
  int* enables;
  float* select_angles;
} mob_multi_bf_param;

typedef struct {
  float init_gain;
  float target_gain_min;
  float target_gain_max;
} mob_bf_post_pro_param;

typedef enum { ES_LEVEL_1 = 1, ES_LEVEL_2, ES_LEVEL_3 } ESLevel;

typedef struct {
  ESLevel level;
  int flag_pre_converge;  // 0 for ASR, 1 for bluetooth
  int flag_noise_fill;
} mob_es_param;

typedef enum { NE_SIMPLE = 1, NE_IMCRA = 2} NEType;

typedef struct {
  NEType type;
} mob_ne_param;

typedef enum { NR_LEVEL_1 = 1, NR_LEVEL_2, NR_LEVEL_3 } NRLevel;
typedef enum { NR_WIENER = 1, NR_OMLSA = 2} NRType;

typedef struct {
  NRType type;
  NRLevel level;
} mob_nr_param;

typedef struct {
  float threshold;
  int delay_ms;
  float desired_direction;
  float half_beamwidth;
} mob_sig_compress_param;

typedef struct {
  int granularity;
  float gain;
  float full_gain_pos;
  float threshold;
  float limit;
} mob_drc_param;

typedef struct {
  float* gains;
} mob_multi_drc_param;

typedef struct {
  float init_gain;
  float target_level;
  int min_frame_count;
  int min_energy_count;
} mob_agc_param;

typedef struct {
  int K;
  int delta;
  int step;
  float factor;
} mob_gwpe_param;

/******************** downlink algorithms **************************/

typedef struct {
  AudioFormat in_format;
  AudioFormat out_format;
} mob_downlink_param;

typedef struct {
  int bands_num;
  float* freqs;
  float* gain_db;
} mob_eq_param;

typedef struct {
  int coeff_weight_len;
  float* coeff_weight;
} mob_eq_param_ext;

/**
 * Mobvoi dsp function for setting global static memory.
 * @param base: Base address of global static memory.
 * @param total: Toatal size of the global memory.
 * @param align: The alignment size when allocating memory.
 */
void mobvoi_set_memory_base(void* base,
                            unsigned int total,
                            unsigned int align);

/**
 * Mobvoi dsp uplink processor initialization function.
 * @param inst: pointer to the instance pointer.
 * @param str: config string.
 * @return  The dsp processor instance.
 */
void mobvoi_uplink_init_with_config(void** inst, const char* str);

/**
 * Mobvoi dsp uplink processor initialization function.
 * @param frame_len: The frame len.
 * @param mic_sample_rate: The mic speech sample rate.
 * @param mic_num: micphone number.
 * @param spk_sample_rate: The reference speech sample rate.
 * @param spk_num: Reference signal number for aec.
 * @param pipeline_num: The instance number for bf and latter algorithms.
 * @return  The dsp processor instance.
 */
void* mobvoi_uplink_init(int frame_len,
                         int mic_sample_rate,
                         int mic_num,
                         int spk_sample_rate,
                         int spk_num,
                         int pipeline_num,
                         int drc_num);
/**
 * Mobvoi dsp uplink processor cleanup function.
 * @param instance: The dsp uplink processor instance.
 */
void mobvoi_uplink_cleanup(void* instance);

/**
 * Set or get the uplink algorithm parameters, if you have not set the
 * algorithm related parameter, the algorithm would be disabled.
 * @param instance: The dsp uplink processor instance.
 * @param type: The dsp uplink algorithm type.
 * @param ptr: The dsp uplink algorithm parameter.
 * @return  0 for success, others for error.
 */
int mobvoi_uplink_process_ctl(void* instance,
                              mob_uplink_ctrl type,
                              void* ptr);

/**
 * Pause the aec algorithm.
 * @param instance: The dsp processor instance.
 * @param type: Type 0, mic pause; type 1, speaker pause.
 */
void mobvoi_uplink_aec_pause(void* instance, int type);

/**
 * Pause the aec algorithm.
 * @param instance: The dsp processor instance.
 * @param type: Type 0, mic pause; type 1, speaker pause.
 */
void mobvoi_uplink_aec_resume(void* instance, int type);

/**
 * Get aec algorithm enabled status.
 * @param instance: The dsp processor instance.
 * @return 0, disabled; 1, enabled.
 */
int mobvoi_uplink_aec_is_enabled(void* instance);

/**
 * Send reference frames to aec processor.
 * @param instance: The dsp processor instance.
 * @param ref: The input speech data buffer. AAAABBBB mode for multi refs.
 * @param size: Buffer size in int16_t.
 * @param channel_num: Channel number of the buffer.
 * @param spk_delay: The delay between reference signal and speaker hardware.
 * @return The actually sent speech data size.
 */
int mobvoi_uplink_send_ref_frames(void* instance,
                                  const int16_t* ref,
                                  int size,
                                  int channel_num,
                                  int spk_delay_frame_num);

/**
 * Send reference frames to aec processor.
 * @param instance: The dsp processor instance.
 * @param ref: reference data, little endian, 16-bit, 16K/48K sample rate.
 * @param size: Buffer size in int16_t.
 * @param channel_index: Channel index.
 * @param ref_delay_frame_num: The delay before ref signal sent.
 * @return The actually sent speech data size.
 */
int mobvoi_uplink_send_ref_frames_per_channel(void* instance,
                                              const int16_t* ref,
                                              int size,
                                              int channel_index,
                                              int spk_delay_frame_num);
/**
 * Send microphone frames to aec processor.
 * @param instance: The dsp processor instance.
 * @param mic: micphone data, little endian, 16-bit, 16K/48K sample rate.
 * @param size: Buffer size in int16_t.
 * @param channel_index: Channel index.
 * @param mic_delay_frame_num: The delay after mic signal arrived.
 * @return The actually sent speech data size.
 */
int mobvoi_uplink_send_mic_frames_per_channel(void* instance,
                                              const int16_t* mic,
                                              int size,
                                              int channel_index,
                                              int mic_delay_frame_num);

/**
 * Uplink frames processing by enabled uplink algorithms.
 * @param instance: The dsp uplink processor instance.
 * @param in: The input speech data buffer. AAAABBBB mode for multi mics.
 * @param size: Buffer size in int16_t.
 * @param channel_num: Channel number of the buffer.
 * @param mic_delay_frame_num: The delay between reference and mic signal.
 * @param out: The processed data buffer.
 * @param out_channel_num: Channel number of the output buffer.
 * @return The actually sent speech data size.
 */
int mobvoi_uplink_process(void* instance,
                          const int16_t* in,
                          int size,
                          int channel_num,
                          int mic_delay_frame_num,
                          int16_t* out,
                          int out_channel_num);

/**
 * Mobvoi dsp downlink processor initialization function.
 * @param frame_len: The frame len.
 * @param sample_rate: The speech sample rate.
 * @param channel_num: The number of channels to process.
 * @return  The dsp downlink processor instance.
 */
void* mobvoi_downlink_init(int frame_len, int sample_rate, int channel_num);

/**
 * Set or get the downlink algorithm parameters, if you have not set the
 * algorithm related parameter, the algorithm would be disabled.
 * @param instance: The dsp downlink processor instance.
 * @param type: The dsp downlink algorithm type.
 * @param ptr: The dsp downlink algorithm parameter.
 * @return  0 for success, others for error.
 */
int mobvoi_downlink_process_ctl(void* instance,
                                mob_downlink_ctrl type,
                                void* ptr);

/**
 * Downlink frames processing by enabled uplink algorithms.
 * @param instance: The dsp downlink processor instance.
 * @param in: The input speech data buffer.
 * @param size: Buffer size in int16_t.
 * @param channel_num: Channel number of the buffer.
 * @param out: The processed data buffer.
 * @param out_channel_num: Channel number of the output buffer.
 * @return The actually sent speech data size
 */
int mobvoi_downlink_process(void* instance,
                            const int16_t* in,
                            int size,
                            int channel_num,
                            int16_t* out,
                            int out_channel_num);

/**
 * Mobvoi dsp downlink processor cleanup function.
 * @param instance: The dsp downlink processor instance.
 */
void mobvoi_downlink_cleanup(void* instance);

#ifdef __cplusplus
}
#endif

#endif  // SDK_PIPELINE_MOBVOI_DSP_H_
