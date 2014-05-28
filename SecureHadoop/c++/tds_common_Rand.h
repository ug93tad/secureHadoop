/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class tds_common_Rand */

#ifndef _Included_tds_common_Rand
#define _Included_tds_common_Rand
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     tds_common_Rand
 * Method:    init
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_tds_common_Rand_init
  (JNIEnv *, jobject, jstring);

/*
 * Class:     tds_common_Rand
 * Method:    encrypt_word_rnd
 * Signature: (Ljava/lang/String;[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_encrypt_1word_1rnd
  (JNIEnv *, jobject, jstring, jbyteArray);

/*
 * Class:     tds_common_Rand
 * Method:    decrypt_word_rnd
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_decrypt_1word_1rnd
  (JNIEnv *, jobject, jbyteArray, jbyteArray);

/*
 * Class:     tds_common_Rand
 * Method:    encrypt_word_cbc
 * Signature: (Ljava/lang/String;[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_encrypt_1word_1cbc__Ljava_lang_String_2_3B
  (JNIEnv *, jobject, jstring, jbyteArray);

/*
 * Class:     tds_common_Rand
 * Method:    encrypt_word_cbc
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_encrypt_1word_1cbc___3B_3B
  (JNIEnv *, jobject, jbyteArray, jbyteArray);

/*
 * Class:     tds_common_Rand
 * Method:    decrypt_word_cbc
 * Signature: ([B[B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_decrypt_1word_1cbc
  (JNIEnv *, jobject, jbyteArray, jbyteArray);

/*
 * Class:     tds_common_Rand
 * Method:    randomBytes
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_randomBytes
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif