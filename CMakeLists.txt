cmake_minimum_required(VERSION 3.6.0)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11 ")

add_library(ecnr_lib
        SHARED
        src/main/cpp/jni_ecnr.cc)

add_library(dsp_lib
        SHARED
        IMPORTED)

set_target_properties(dsp_lib
        PROPERTIES  IMPORTED_LOCATION
        ${CMAKE_CURRENT_SOURCE_DIR}/libs/${ANDROID_ABI}/libmobvoidsp.so)

target_link_libraries(
        ecnr_lib
        android
        log
        dsp_lib)
