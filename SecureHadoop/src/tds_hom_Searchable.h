/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class tds_hom_Searchable */

#ifndef _Included_tds_hom_Searchable
#define _Included_tds_hom_Searchable
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     tds_hom_Searchable
 * Method:    init
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_tds_hom_Searchable_init
  (JNIEnv *, jobject, jstring);

/*
 * Class:     tds_hom_Searchable
 * Method:    encrypt
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_tds_hom_Searchable_encrypt
  (JNIEnv *, jobject, jstring);

/*
 * Class:     tds_hom_Searchable
 * Method:    getToken
 * Signature: (Ljava/lang/String;)Ltds/hom/SearchToken;
 */
JNIEXPORT jobject JNICALL Java_tds_hom_Searchable_getToken
  (JNIEnv *, jobject, jstring);

/*
 * Class:     tds_hom_Searchable
 * Method:    match
 * Signature: ([BLtds/hom/SearchToken;)Z
 */
JNIEXPORT jboolean JNICALL Java_tds_hom_Searchable_match
  (JNIEnv *, jobject, jbyteArray, jobject);

#ifdef __cplusplus
}
#endif
#endif
