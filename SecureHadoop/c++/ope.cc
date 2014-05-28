#include "tds_hom_Ope.h"
#include "crypto/ope.hh"
#include <iostream>
#include <openssl/rand.h>

using namespace std;
using namespace NTL; 

OPE* ope; 
int counter = 0; 

JNIEXPORT void JNICALL Java_tds_hom_Ope_init
  (JNIEnv *env, jobject obj, jstring key, jint pLen, jint cLen){
	unsigned char* k = (unsigned char*)env->GetStringUTFChars(key,NULL); 
	ope = new OPE(string(reinterpret_cast<const char*>(k)), pLen*8, cLen*8); 

}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Ope_encrypt
  (JNIEnv *env, jobject obj, jbyteArray pt){
	int size = ope->getPlaintextLen()/8;  
	unsigned char *bytes = (unsigned char*)malloc(size); 	
	
	env->GetByteArrayRegion(pt,0,size, (jbyte*)bytes);
	ZZ toEncrypt= ZZFromBytes(bytes,size); 
	ZZ ct = ope->encrypt(toEncrypt); 
	
	int clen = ope->getCiphertextLen()/8;
	unsigned char* cbytes = (unsigned char*)malloc(clen); 

	BytesFromZZ(cbytes,ct,clen); 
	
	jbyteArray ret = env->NewByteArray(clen); 
	env->SetByteArrayRegion(ret,0,clen, (jbyte*)cbytes); 
	return ret; 

}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Ope_decrypt
  (JNIEnv *env, jobject obj, jbyteArray ctext){
	int size = ope->getCiphertextLen()/8;  
	unsigned char *bytes = (unsigned char*)malloc(size); 
	env->GetByteArrayRegion(ctext,0,size, (jbyte*)bytes);

	ZZ pt = ope->decrypt(ZZFromBytes(bytes,size)); 

	int plen = ope->getPlaintextLen()/8;
	unsigned char* pbytes = (unsigned char*)malloc(plen); 
	BytesFromZZ(pbytes,pt,plen); 
	jbyteArray ret = env->NewByteArray(plen); 
	env->SetByteArrayRegion(ret,0,plen, (jbyte*)pbytes); 
	return ret; 

}

JNIEXPORT jstring JNICALL Java_crypto_Ope_decryptDistance
  (JNIEnv *env, jobject obj, jbyteArray ctext){
	int size = ope->getCiphertextLen()/8;  
	unsigned char *bytes = (unsigned char*)malloc(size); 
	env->GetByteArrayRegion(ctext,0,size, (jbyte*)bytes);

	ZZ pt = ope->decrypt(ZZFromBytes(bytes,size)); 
	stringstream ss;
	ss << pt; 
	return env->NewStringUTF(ss.str().c_str()); 

}

JNIEXPORT jint JNICALL Java_crypto_Ope_compareBytes
  (JNIEnv *env, jobject obj, jbyteArray vals1, jbyteArray vals2){
	int size = env->GetArrayLength(vals1); 

	unsigned char *bytes = (unsigned char*)malloc(size); 
	env->GetByteArrayRegion(vals1,0,size, (jbyte*)bytes);

	ZZ v1 = ZZFromBytes(bytes,size); 
	
	size = env->GetArrayLength(vals2);
	bytes = (unsigned char*)malloc(size); 
	env->GetByteArrayRegion(vals2,0,size, (jbyte*)bytes);

	ZZ v2 = ZZFromBytes(bytes,size);

	if (v1<v2)
		return -1;
	else if (v1==v2)
		return 0;
	else
		return 1; 
}
