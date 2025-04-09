#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define READ_CHUNK_SIZE 8192

#define UNUSED(x) (void)(x)

char* readFileContent(const char* filePath) {
    FILE* fp = fopen(filePath, "r");
    if (!fp) return NULL;

    size_t capacity = READ_CHUNK_SIZE;
    char* buffer = (char*) malloc(capacity);
    if (buffer == NULL) {
        fclose(fp);
        return NULL;
    }

    size_t totalRead = 0;
    size_t bytesRead = 0;

    while ((bytesRead = fread(buffer + totalRead, 1, READ_CHUNK_SIZE, fp)) > 0) {
        totalRead += bytesRead;

        if (totalRead + READ_CHUNK_SIZE > capacity) {
            capacity *= 2;
            char* newBuffer = (char*) realloc(buffer, capacity);
            if (newBuffer == NULL) {
                free(buffer);
                fclose(fp);
                return NULL;
            }
            buffer = newBuffer;
        }
    }
    buffer[totalRead] = '\0';
    fclose(fp);
    return buffer;
}


JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcVersion(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/version");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/version");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcCPUInfo(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/cpuinfo");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/cpuinfo");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcMemInfo(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/meminfo");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/meminfo");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}



JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfStatus(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/status");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/status");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfMaps(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/maps");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/maps");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfMountinfo(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/mountinfo");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/mountinfo");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfMounts(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/mounts");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/mounts");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfMountstats(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/mountstats");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/mountstats");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfIO(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/io");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/io");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfLimits(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/limits");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/limits");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfOomScore(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/oom_score");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/oom_score");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfOomAdj(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/oom_adj");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/oom_adj");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfOomScoreAdj(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/oom_score_adj");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/oom_score_adj");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSched(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/sched");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/sched");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSchedBoost(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/sched_boost");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/sched_boost");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSchedBoostPeriodMs(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/sched_boost_period_ms");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/sched_boost_period_ms");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSchedGroupId(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/sched_group_id");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/sched_group_id");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSchedInitTaskLoad(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/sched_init_task_load");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/sched_init_task_load");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSchedWakeUpIdle(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/sched_wake_up_idle");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/sched_wake_up_idle");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSchedstat(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/schedstat");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/schedstat");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}

JNIEXPORT jstring JNICALL Java_com_coara_proc_ProcInfoNative_getProcSelfSmap(JNIEnv *env, jclass clazz) {
    UNUSED(clazz);
    char *tmp = readFileContent("/proc/self/smap");
    if (!tmp) return (*env)->NewStringUTF(env, "Error reading /proc/self/smap");
    jstring result = (*env)->NewStringUTF(env, tmp);
    free(tmp);
    return result;
}