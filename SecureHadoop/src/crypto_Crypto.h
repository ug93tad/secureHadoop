/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class crypto_Crypto */

#ifndef _Included_crypto_Crypto
#define _Included_crypto_Crypto
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     crypto_Crypto
 * Method:    det_init
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_crypto_Crypto_det_1init
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     crypto_Crypto
 * Method:    encrypt_word
 * Signature: (Ljava/lang/String;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_crypto_Crypto_encrypt_1word
  (JNIEnv *, jobject, jstring);

/*
 * Class:     crypto_Crypto
 * Method:    decrypt_word
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_crypto_Crypto_decrypt_1word
  (JNIEnv *, jobject, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif