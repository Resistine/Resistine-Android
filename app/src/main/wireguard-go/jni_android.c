//go:build android

/* SPDX-License-Identifier: Apache-2.0 */

#include <jni.h>
#include <stdint.h>
#include <stdlib.h>

struct go_string {
    const char *str;
    long n;
};

extern int resistineWgTurnOn(struct go_string ifname, int tun_fd,
                             int outbound_telemetry_fd, int inbound_telemetry_fd,
                             struct go_string settings);
extern uint64_t resistineWgTurnOff(int handle);
extern int resistineWgGetSocketV4(int handle);
extern int resistineWgGetSocketV6(int handle);
extern char *resistineWgGetConfig(int handle);
extern uint64_t resistineWgGetTelemetryDrops(int handle);
extern uint64_t resistineWgGetTelemetryQueueDepth(int handle);
extern uint64_t resistineWgGetTelemetryQueueHighWater(int handle);
extern char *resistineWgVersion(void);

JNIEXPORT jint JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgTurnOn(
        JNIEnv *env, jclass clazz, jstring ifname, jint tun_fd,
        jint outbound_telemetry_fd, jint inbound_telemetry_fd, jstring settings) {
    (void) clazz;
    const char *ifname_str = (*env)->GetStringUTFChars(env, ifname, 0);
    const char *settings_str = (*env)->GetStringUTFChars(env, settings, 0);
    const size_t ifname_len = (*env)->GetStringUTFLength(env, ifname);
    const size_t settings_len = (*env)->GetStringUTFLength(env, settings);
    const int result = resistineWgTurnOn(
            (struct go_string) {.str = ifname_str, .n = (long) ifname_len},
            tun_fd,
            outbound_telemetry_fd,
            inbound_telemetry_fd,
            (struct go_string) {.str = settings_str, .n = (long) settings_len});
    (*env)->ReleaseStringUTFChars(env, ifname, ifname_str);
    (*env)->ReleaseStringUTFChars(env, settings, settings_str);
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgTurnOff(
        JNIEnv *env, jclass clazz, jint handle) {
    (void) env;
    (void) clazz;
    return (jlong) resistineWgTurnOff(handle);
}

JNIEXPORT jint JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgGetSocketV4(
        JNIEnv *env, jclass clazz, jint handle) {
    (void) env;
    (void) clazz;
    return resistineWgGetSocketV4(handle);
}

JNIEXPORT jint JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgGetSocketV6(
        JNIEnv *env, jclass clazz, jint handle) {
    (void) env;
    (void) clazz;
    return resistineWgGetSocketV6(handle);
}

JNIEXPORT jstring JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgGetConfig(
        JNIEnv *env, jclass clazz, jint handle) {
    (void) clazz;
    char *config = resistineWgGetConfig(handle);
    if (config == NULL) {
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, config);
    free(config);
    return result;
}

JNIEXPORT jlong JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgGetTelemetryDrops(
        JNIEnv *env, jclass clazz, jint handle) {
    (void) env;
    (void) clazz;
    return (jlong) resistineWgGetTelemetryDrops(handle);
}

JNIEXPORT jlong JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgGetTelemetryQueueDepth(
        JNIEnv *env, jclass clazz, jint handle) {
    (void) env;
    (void) clazz;
    return (jlong) resistineWgGetTelemetryQueueDepth(handle);
}

JNIEXPORT jlong JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgGetTelemetryQueueHighWater(
        JNIEnv *env, jclass clazz, jint handle) {
    (void) env;
    (void) clazz;
    return (jlong) resistineWgGetTelemetryQueueHighWater(handle);
}

JNIEXPORT jstring JNICALL
Java_com_wireguard_android_backend_TelemetryGoBackend_wgVersion(
        JNIEnv *env, jclass clazz) {
    (void) clazz;
    char *version = resistineWgVersion();
    if (version == NULL) {
        return NULL;
    }
    jstring result = (*env)->NewStringUTF(env, version);
    free(version);
    return result;
}
