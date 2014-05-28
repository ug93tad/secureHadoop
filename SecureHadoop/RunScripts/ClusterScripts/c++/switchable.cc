#include<switchable.hh>
#include<string>
#include "tds_hom_switchable_Switchable.h"

using namespace std;

Switchable switchable; 

void Switchable::keygen(const char* paillierPub, const char* paillierPriv,
const char* elgamalPub, const char*elgamalPriv) {
	additive.keygen(paillierPub, paillierPriv);
	multiplicative.keygen(elgamalPub, elgamalPriv);  
} 

void Switchable::init(const char* paillierPub, const char* paillierPriv, const
char* elgamalPub, const char*elgamalPriv) {
	additive.init_pub(paillierPub); 
	additive.init_priv(paillierPriv); 	
	multiplicative.init_pub(elgamalPub);
	multiplicative.init_priv(elgamalPriv); 
}
void Switchable::init(const char* paillierPub, const char* paillierPriv, const char* elgamalPub, const char* elgamalPriv, string opeKey, int pLen, int cLen){
	additive.init_pub(paillierPub); 
	additive.init_priv(paillierPriv); 	
	multiplicative.init_pub(elgamalPub);
	multiplicative.init_priv(elgamalPriv); 
	ope = new OPE(opeKey, pLen*8, cLen*8); 
}


int Switchable::getOpeLen(){
	return ope->getCiphertextLen()/8; 
}
//switch from multiplicative to additive
ZZ Switchable::mulToAdd(const ZZ& ct){ 
	return additive.encrypt(multiplicative.decrypt(ct)); 	
}

ZZ Switchable::mulToOpe(const ZZ& ct){ 
	return ope->encrypt(multiplicative.decrypt(ct)); 	
}

//switch from additive to multiplicative
ZZ Switchable::addToMul(const ZZ& ct){
	return multiplicative.encrypt(additive.decrypt(ct)); 
}

ZZ Switchable::addToOpe(const ZZ& ct){
	return ope->encrypt(additive.decrypt(ct)); 
}

ZZ Switchable::opeToAdd(const ZZ& ct){
	return additive.encrypt(ope->decrypt(ct)); 
}

ZZ Switchable::opeToMul(const ZZ& ct){
	return multiplicative.encrypt(ope->decrypt(ct)); 
}

JNIEXPORT void JNICALL Java_tds_hom_switchable_Switchable_init__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2
  (JNIEnv *env, jobject obj, jstring paillierPub, jstring paillierPriv, jstring elgamalPub, jstring elgamalPriv){
	unsigned char* pp = (unsigned char*)env->GetStringUTFChars(paillierPub,NULL); 
	unsigned char* ppr = (unsigned char*)env->GetStringUTFChars(paillierPriv,NULL); 
	unsigned char* ep = (unsigned char*)env->GetStringUTFChars(elgamalPub,NULL); 
	unsigned char* epr = (unsigned char*)env->GetStringUTFChars(elgamalPriv,NULL); 
	switchable.init((const char*)pp, (const char*)ppr, (const char*)ep, (const char*)epr); 	

}

JNIEXPORT void JNICALL Java_tds_hom_switchable_Switchable_init__Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2Ljava_lang_String_2II
  (JNIEnv *env, jobject obj, jstring paillierPub, jstring paillierPriv, jstring elgamalPub, jstring elgamalPriv, jstring opeKey, jint pLen, jint cLen){
	unsigned char* pp = (unsigned char*)env->GetStringUTFChars(paillierPub,NULL); 
	unsigned char* ppr = (unsigned char*)env->GetStringUTFChars(paillierPriv,NULL); 
	unsigned char* ep = (unsigned char*)env->GetStringUTFChars(elgamalPub,NULL); 
	unsigned char* epr = (unsigned char*)env->GetStringUTFChars(elgamalPriv,NULL); 
	unsigned char* k = (unsigned char*)env->GetStringUTFChars(opeKey,NULL); 

	switchable.init((const char*)pp, (const char*)ppr, (const char*)ep, (const char*)epr, string(reinterpret_cast<const char*>(k)),pLen, cLen); 	
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_switchable_Switchable_addToMulNative
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ add = ZZFromBytes(bytes,size); 
	ZZ mul = switchable.addToMul(add);
 
	int sizes = NumBytes(mul); 
	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,mul,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_switchable_Switchable_addToOpeNative
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ add = ZZFromBytes(bytes,size); 
	ZZ mul = switchable.addToOpe(add);
 
	int sizes = switchable.getOpeLen(); //ope->getCiphertextLen()/8;
	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,mul,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_switchable_Switchable_mulToAddNative
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ mul = ZZFromBytes(bytes,size); 
	ZZ add = switchable.mulToAdd(mul);
 
	int sizes = NumBytes(add); 
	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,add,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_switchable_Switchable_mulToOpeNative
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ mul = ZZFromBytes(bytes,size); 
	ZZ add = switchable.mulToOpe(mul);
 
	int sizes = NumBytes(add); 
	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,add,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_switchable_Switchable_opeToAddNative
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ mul = ZZFromBytes(bytes,size); 
	ZZ add = switchable.opeToAdd(mul);
 
	int sizes = NumBytes(add); 
	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,add,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 

}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_switchable_Switchable_opeToMulNative
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ mul = ZZFromBytes(bytes,size); 
	ZZ add = switchable.opeToMul(mul);
 
	int sizes = NumBytes(add); 
	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,add,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}
