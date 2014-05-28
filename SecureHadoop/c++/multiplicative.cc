#include<NTL/ZZ.h>
#include "multiplicative.hh"
#include "tds_hom_Elgamal.h"

using namespace std; 

MUL mult; 

void MUL::keygen(const char* elgamalPub, const char* elgamalPriv){
	urandom u;
	auto ks = ElGamal::keygen(&u); //h,q,g,x 
	//writing elgamal.h, elgamal.q and elgmal.g to pub file
	
	FILE *fpub, *fpriv;
	fpub = fopen(elgamalPub, "wb");
	fpriv = fopen(elgamalPriv, "wb"); 
	//h
	int n = NumBytes(ks[0]); 
	fwrite(&n,sizeof(int),1,fpub);
	unsigned char* p0 = (unsigned char*)malloc(n); 
	BytesFromZZ(p0,ks[0],n);
	fwrite(p0,1,n,fpub); 
	
	//q
	n = NumBytes(ks[1]);
	fwrite(&n, sizeof(int),1,fpub); 
	unsigned char* p1 = (unsigned char*)malloc(n); 
	BytesFromZZ(p1,ks[1],n);
	fwrite(p1,1,n,fpub); 

	//g
	n = NumBytes(ks[2]); 
	fwrite(&n, sizeof(int), 1, fpub);
	unsigned char* p2 = (unsigned char*)malloc(n);
	BytesFromZZ(p2,ks[2],n); 
	fwrite(p2,1,n,fpub); 


	//x
	n = NumBytes(ks[3]); 
	fwrite(&n, sizeof(int), 1, fpriv); 
	unsigned char* p3 = (unsigned char*)malloc(n); 
	BytesFromZZ(p3,ks[3],n);
	fwrite(p3,1,n,fpriv); 

	fclose(fpub);
	fclose(fpriv);

}

void MUL::init_pub(const char* elgamalPub){
	FILE *fpub;
	fpub = fopen(elgamalPub,"rb"); 
	int n; 
	fread(&n, sizeof(int), 1, fpub); 
	unsigned char* p0 = (unsigned char*)malloc(n); 
	fread(p0,1,n,fpub);
	h = ZZFromBytes(p0,n); 

	//q
	fread(&n, sizeof(int), 1, fpub);  
	unsigned char* p1 = (unsigned char*)malloc(n);
	fread(p1,1,n,fpub);
	q = ZZFromBytes(p1,n); 
	
	fread(&n, sizeof(int), 1, fpub); 
	unsigned char* p2 = (unsigned char*)malloc(n); 
	fread(p2,1,n,fpub); 
	g = ZZFromBytes(p2,n); 	

	fclose(fpub); 
}

void MUL::init_priv(const char* elgamalPriv){
	FILE *fpriv; 
	fpriv = fopen(elgamalPriv, "rb"); 
	int n;
	fread(&n, sizeof(int), 1, fpriv); 
	unsigned char* s0 = (unsigned char*)malloc(n); 
	fread(s0,1,n,fpriv); 
	x = ZZFromBytes(s0,n); 
	vector<ZZ> keys = {h,q,g,x};
	elgamal = new ElGamal(keys); 
}

ZZ MUL::encrypt(const ZZ& value){
	return elgamal->encrypt((value)); 	
} 

ZZ MUL::decrypt(const ZZ& value){
	return elgamal->decrypt(value); 
} 

ZZ MUL::multiply(const ZZ& val1, const ZZ& val2){
	return ElGamal::mul(val1, val2, q); 
} 

JNIEXPORT void JNICALL Java_tds_hom_Elgamal_keygen
  (JNIEnv *env, jobject obj, jstring pubFile, jstring privFile){
	unsigned char* pf = (unsigned char*)env->GetStringUTFChars(pubFile,NULL); 
	unsigned char* pvf = (unsigned char*)env->GetStringUTFChars(privFile,NULL); 
	mult.keygen((const char*)pf, (const char*)pvf);	
}

JNIEXPORT void JNICALL Java_tds_hom_Elgamal_init_1public_1key
  (JNIEnv *env, jobject obj, jstring pubFile){
	unsigned char* pf = (unsigned char*)env->GetStringUTFChars(pubFile,NULL); 
	mult.init_pub((const char*)pf); 
}

JNIEXPORT void JNICALL Java_tds_hom_Elgamal_init_1private_1key
  (JNIEnv *env, jobject obj, jstring privFile){
	unsigned char* pf = (unsigned char*)env->GetStringUTFChars(privFile,NULL); 
	mult.init_priv((const char*)pf); 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Elgamal_encrypt
  (JNIEnv *env, jobject obj, jint val){
	ZZ enc = mult.encrypt(to_ZZ((int)val)); 
	int size = NumBytes(enc); 
	//if (size<PAILLIER_LEN_BYTES)
	//	size = PAILLIER_LEN_BYTES;

	unsigned char* bytes = (unsigned char*)malloc(size); 
	BytesFromZZ(bytes,enc,size); 

	jbyteArray ret = env->NewByteArray(size); 
	env->SetByteArrayRegion(ret,0,size, (jbyte*)bytes); 
	return ret;	
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Elgamal_multiply
  (JNIEnv *env, jobject obj, jbyteArray vals1, jbyteArray vals2){
	int size1 = env->GetArrayLength(vals1); 
	int size2 = env->GetArrayLength(vals2); 

	unsigned char* bytes1 = (unsigned char*)malloc(size1);
	unsigned char* bytes2 = (unsigned char*)malloc(size2);
	env->GetByteArrayRegion(vals1,0,size1, (jbyte*)bytes1);
	env->GetByteArrayRegion(vals2,0,size2, (jbyte*)bytes2); 

	ZZ enc1 = ZZFromBytes(bytes1, size1);
	ZZ enc2 = ZZFromBytes(bytes2, size2); 
	ZZ sum = mult.multiply(enc1,enc2); 	
	
	int sizes = NumBytes(sum); 

	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,sum,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}

JNIEXPORT jlong JNICALL Java_tds_hom_Elgamal_decrypt
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ sum = ZZFromBytes(bytes,size); 
	ZZ dec = mult.decrypt(sum); 

	return to_long(dec); 	

}

JNIEXPORT jstring JNICALL Java_tds_hom_Elgamal_decryptToString
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ sum = ZZFromBytes(bytes,size); 
	ZZ dec = mult.decrypt(sum);
	
	stringstream ss;
	ss << dec; 
	return env->NewStringUTF(ss.str().c_str()); 
}

