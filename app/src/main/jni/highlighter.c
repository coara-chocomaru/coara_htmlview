#include <jni.h>
#include <regex.h>
#include <stdlib.h>
#include <string.h>
#include <stdbool.h>


#define TAG_COLOR 0xFF0000FF       // Blue
#define ATTRIBUTE_COLOR 0xFF008000 // Green
#define VALUE_COLOR 0xFFB22222     // Red

#define LARGE_TEXT_THRESHOLD 700


typedef struct {
    int start;
    int end;
    int color;
} Span;


static regex_t* compile_regex(const char* pattern, int flags) {
    regex_t* regex = (regex_t*)malloc(sizeof(regex_t));
    if (regex == NULL) {
        return NULL;
    }
    int ret = regcomp(regex, pattern, flags);
    if (ret != 0) {
        free(regex);
        return NULL;
    }
    return regex;
}


static bool add_span(Span** spans, size_t* count, size_t* capacity, int start, int end, int color) {
    if (*count >= *capacity) {
        size_t new_capacity = *capacity == 0 ? 16 : *capacity * 2;
        Span* new_spans = (Span*)realloc(*spans, new_capacity * sizeof(Span));
        if (new_spans == NULL) {
            return false;
        }
        *spans = new_spans;
        *capacity = new_capacity;
    }
    (*spans)[*count].start = start;
    (*spans)[*count].end = end;
    (*spans)[*count].color = color;
    (*count)++;
    return true;
}


JNIEXPORT jobject JNICALL
Java_com_coara_htmlview_htmlview_getHighlightSpansNative(JNIEnv *env, jobject obj, jstring jtext, jint scrollY, jint height, jintArray lineStarts, jintArray lineEnds, jintArray lineTops) {
    
    const char* text = (*env)->GetStringUTFChars(env, jtext, NULL);
    if (text == NULL) {
        return NULL;
    }
    size_t text_len = strlen(text);

    
    bool isLargeText = text_len > LARGE_TEXT_THRESHOLD;
    int visibleStart = 0;
    int visibleEnd = (int)text_len;
    if (isLargeText) {      
    }

    const char* subtext = text + visibleStart;
    size_t subtext_len = visibleEnd - visibleStart;


    regex_t* tag_regex = compile_regex("<[^>]+>", REG_EXTENDED);
    if (tag_regex == NULL) {
        (*env)->ReleaseStringUTFChars(env, jtext, text);
        return NULL;
    }
    regex_t* attr_regex = compile_regex("([a-zA-Z0-9-]+)=\\\"([^\\\"]*)\\\"", REG_EXTENDED);
    if (attr_regex == NULL) {
        regfree(tag_regex);
        free(tag_regex);
        (*env)->ReleaseStringUTFChars(env, jtext, text);
        return NULL;
    }

    
    Span* spans = NULL;
    size_t spans_count = 0;
    size_t spans_capacity = 0;


    regmatch_t tag_matches[1];
    size_t offset = 0;
    while (regexec(tag_regex, subtext + offset, 1, tag_matches, 0) == 0) {
        int tag_start = visibleStart + offset + (int)tag_matches[0].rm_so;
        int tag_end = visibleStart + offset + (int)tag_matches[0].rm_eo;
        if (!add_span(&spans, &spans_count, &spans_capacity, tag_start, tag_end, TAG_COLOR)) {
            goto cleanup;
        }

    
        const char* tag_text = subtext + offset + tag_matches[0].rm_so;
        size_t tag_text_len = tag_matches[0].rm_eo - tag_matches[0].rm_so;

        
        regmatch_t attr_matches[3];
        size_t attr_offset = 0;
        while (regexec(attr_regex, tag_text + attr_offset, 3, attr_matches, 0) == 0) {
        
            int attr_name_start = tag_start + attr_offset + (int)attr_matches[1].rm_so;
            int attr_name_end = tag_start + attr_offset + (int)attr_matches[1].rm_eo;
            if (!add_span(&spans, &spans_count, &spans_capacity, attr_name_start, attr_name_end, ATTRIBUTE_COLOR)) {
                goto cleanup;
            }

            
            int attr_value_start = tag_start + attr_offset + (int)attr_matches[2].rm_so;
            int attr_value_end = tag_start + attr_offset + (int)attr_matches[2].rm_eo;
            if (!add_span(&spans, &spans_count, &spans_capacity, attr_value_start, attr_value_end, VALUE_COLOR)) {
                goto cleanup;
            }

            attr_offset += attr_matches[0].rm_eo;
            if (attr_offset >= tag_text_len) break;
        }

        offset += tag_matches[0].rm_eo;
        if (offset >= subtext_len) break;
    }

    jclass int_array_class = (*env)->FindClass(env, "[I");
    if (int_array_class == NULL) {
        goto cleanup;
    }
    jobjectArray result = (*env)->NewObjectArray(env, (jsize)spans_count, int_array_class, NULL);
    if (result == NULL) {
        goto cleanup;
    }

    for (size_t i = 0; i < spans_count; i++) {
        jintArray inner = (*env)->NewIntArray(env, 3);
        if (inner == NULL) {
            goto cleanup;
        }
        jint vals[3] = {spans[i].start, spans[i].end, spans[i].color};
        (*env)->SetIntArrayRegion(env, inner, 0, 3, vals);
        (*env)->SetObjectArrayElement(env, result, (jsize)i, inner);
        (*env)->DeleteLocalRef(env, inner);
    }

cleanup:

    if (spans != NULL) free(spans);
    regfree(tag_regex);
    free(tag_regex);
    regfree(attr_regex);
    free(attr_regex);
    (*env)->ReleaseStringUTFChars(env, jtext, text);

    return result;
}
