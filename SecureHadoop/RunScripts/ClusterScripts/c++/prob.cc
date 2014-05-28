#include<stdio.h>
#include<stdint.h>
#include<stdlib.h>
#include<iostream>
#include<string.h>
#include<prob.hh>
#include<tds_common_Rand.h>
#include<openssl/rand.h>
#include "util/util.hh"
using namespace std; 

Prob rnd; 

void Prob::init(unsigned char* key){
	AES_set_encrypt_key((const unsigned char*)key, AES_BLOCK_SIZE*8, &aes_key); 
}


void Prob::encrypt_word(unsigned char* ptext, int length, unsigned char* randIV, unsigned char** ctext){
	int n=0;
        *ctext = (unsigned char*)malloc(length);
        AES_cfb128_encrypt((const unsigned char*)ptext,*ctext, length, &aes_key, randIV, &n, AES_ENCRYPT);
}

void Prob::encrypt_word_cbc(unsigned char* ptext, int length, unsigned char* randIV, unsigned char** ctext, int *paddedPLen){
	int n=0;
	unsigned char* paddedPtext; 
	pad(ptext,length,&paddedPtext,paddedPLen); 
       
	*ctext = (unsigned char*)malloc(*paddedPLen);
	
        AES_cfb128_encrypt((const unsigned char*)paddedPtext,*ctext, *paddedPLen, &aes_key, randIV, &n, AES_ENCRYPT);
}

void Prob::decrypt_word_cbc(unsigned char* ctext, int length, unsigned char* randIV, unsigned char** ptext, int *unpaddedLen){
	int n=0;
	unsigned char* paddedPtext = (unsigned char*)malloc(length); 
        AES_cfb128_encrypt((const unsigned char*)ctext,paddedPtext, length, &aes_key, randIV, &n, AES_DECRYPT);
	
	unpad(paddedPtext,length,ptext,unpaddedLen); 
}

void Prob::decrypt_word(unsigned char* ctext, int length, unsigned char* randIV, unsigned char** ptext){
	int n=0;
        *ptext = (unsigned char*)malloc(length);
        AES_cfb128_encrypt((const unsigned char*)ctext, *ptext, length, &aes_key, randIV, &n, AES_DECRYPT);
}

JNIEXPORT void JNICALL Java_tds_common_Rand_init
  (JNIEnv *env, jobject obj, jstring key){
	unsigned char* k = (unsigned char*)env->GetStringUTFChars(key,NULL); 
	rnd.init(k); 
}

JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_encrypt_1word_1rnd
  (JNIEnv *env, jobject obj, jstring word, jbyteArray randIV){
	unsigned char* ptext = (unsigned char*)env->GetStringUTFChars(word,NULL);
	int len = env->GetStringLength(word); 

	unsigned char* ivRand = (unsigned char*)malloc(AES_BLOCK_SIZE); 
	env->GetByteArrayRegion(randIV,0,AES_BLOCK_SIZE,(jbyte*)ivRand); 
	unsigned char* ct; 
	rnd.encrypt_word(ptext,len,ivRand,&ct); 
	
	jbyteArray ret = env->NewByteArray(len); 
	env->SetByteArrayRegion(ret,0,len,(jbyte*)ct); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_encrypt_1word_1cbc__Ljava_lang_String_2_3B
  (JNIEnv *env, jobject obj, jstring word, jbyteArray randIV){
	unsigned char* ptext = (unsigned char*)env->GetStringUTFChars(word,NULL);
	int len = env->GetStringLength(word); 

	unsigned char* ivRand = (unsigned char*)malloc(AES_BLOCK_SIZE); 
	env->GetByteArrayRegion(randIV,0,AES_BLOCK_SIZE,(jbyte*)ivRand); 
	unsigned char* ct; 
	int ctLen; 
	rnd.encrypt_word_cbc(ptext,len,ivRand,&ct, &ctLen); 
	
	jbyteArray ret = env->NewByteArray(ctLen); 
	env->SetByteArrayRegion(ret,0,ctLen,(jbyte*)ct); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_encrypt_1word_1cbc___3B_3B
  (JNIEnv *env, jobject obj, jbyteArray wordBytes, jbyteArray randIV){
	int len = env->GetArrayLength(wordBytes);
        unsigned char* ptext = (unsigned char*)malloc(len);
        env->GetByteArrayRegion(wordBytes,0,len,(jbyte*)ptext);

	unsigned char* ivRand = (unsigned char*)malloc(AES_BLOCK_SIZE); 
	env->GetByteArrayRegion(randIV,0,AES_BLOCK_SIZE,(jbyte*)ivRand); 
	unsigned char* ct; 
	int ctLen; 
	
	rnd.encrypt_word_cbc(ptext,len,ivRand,&ct, &ctLen); 
	
	jbyteArray ret = env->NewByteArray(ctLen); 
	env->SetByteArrayRegion(ret,0,ctLen,(jbyte*)ct); 
	return ret; 

}

JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_decrypt_1word_1cbc
  (JNIEnv *env, jobject obj, jbyteArray ctext, jbyteArray randIV){
	int cl = env->GetArrayLength(ctext);
        unsigned char* ct = (unsigned char*)malloc(cl);
        env->GetByteArrayRegion(ctext,0,cl,(jbyte*)ct);
	unsigned char* ivRand = (unsigned char*)malloc(AES_BLOCK_SIZE);
        env->GetByteArrayRegion(randIV,0,AES_BLOCK_SIZE,(jbyte*)ivRand);

        unsigned char* ptext;
	int pLen; 

        rnd.decrypt_word_cbc(ct,cl,ivRand,&ptext, &pLen);
        jbyteArray ret = env->NewByteArray(pLen);
        env->SetByteArrayRegion(ret,0,pLen,(jbyte*)ptext);
        return ret;

}


JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_decrypt_1word_1rnd
  (JNIEnv *env, jobject obj, jbyteArray ctext, jbyteArray randIV){
	int cl = env->GetArrayLength(ctext);
        unsigned char* ct = (unsigned char*)malloc(cl);
        env->GetByteArrayRegion(ctext,0,cl,(jbyte*)ct);
	unsigned char* ivRand = (unsigned char*)malloc(AES_BLOCK_SIZE);
        env->GetByteArrayRegion(randIV,0,AES_BLOCK_SIZE,(jbyte*)ivRand);

        unsigned char* ptext;
        rnd.decrypt_word(ct,cl,ivRand,&ptext);
        jbyteArray ret = env->NewByteArray(cl);
        env->SetByteArrayRegion(ret,0,cl,(jbyte*)ptext);
        return ret;

}

JNIEXPORT jbyteArray JNICALL Java_tds_common_Rand_randomBytes
  (JNIEnv *env, jobject obj, jint length){
	unsigned char* ret = (unsigned char*)malloc(length*sizeof(unsigned char)); 
	RAND_bytes(ret,length); 
	jbyteArray out = env->NewByteArray(length);
	env->SetByteArrayRegion(out,0,length,(jbyte*)ret); 
	return out; 
}

