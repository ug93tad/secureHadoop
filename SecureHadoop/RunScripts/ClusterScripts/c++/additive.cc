#include "additive.hh"
#include <iostream>
#include <stdio.h>
#include "tds_hom_Paillier.h"
HOM hom; 

void HOM::keygen(const char* pubf, const char* privf){
	urandom u; 
	auto sk = Paillier_priv::keygen(&u); 
	priv = new Paillier_priv(sk);
	auto pk = priv->pubkey();
	pub = new Paillier(pk); 

	FILE *fpub, *fpriv; 
	fpub = fopen(pubf, "wb");
	//writing pk[0], pk[1]
	int n = NumBytes(pk[0]); 
	fwrite(&n, sizeof(int), 1, fpub); 
	unsigned char* p1 = (unsigned char*)malloc(n); 
	BytesFromZZ(p1,pk[0],n); 
	fwrite(p1,1,n,fpub); 	

	n = NumBytes(pk[1]); 
	fwrite(&n,sizeof(int),1,fpub); 
	unsigned char* p2 = (unsigned char*)malloc(n); 
	BytesFromZZ(p2,pk[1],n); 	
	fwrite(p2,1,n,fpub); 
	fclose(fpub); 

	//writing sk[0], sk[1], sk[2], sk[3]
	fpriv = fopen(privf,"wb"); 
	n = NumBytes(sk[0]); 
	fwrite(&n,sizeof(int),1,fpriv); 
	unsigned char* p3 = (unsigned char*)malloc(n); 
	BytesFromZZ(p3,sk[0],n); 	
	fwrite(p3,1,n,fpriv); 

	n = NumBytes(sk[1]); 
	fwrite(&n,sizeof(int),1,fpriv); 
	unsigned char* p4 = (unsigned char*)malloc(n); 
	BytesFromZZ(p4,sk[1],n); 	
	fwrite(p4,1,n,fpriv); 

	n = NumBytes(sk[2]);
	fwrite(&n,sizeof(int),1,fpriv); 
	unsigned char* p5 = (unsigned char*)malloc(n); 
	BytesFromZZ(p5,sk[2],n); 	
	fwrite(p5,1,n,fpriv); 

	n = NumBytes(sk[3]); 
	fwrite(&n,sizeof(int),1,fpriv); 
	unsigned char* p6 = (unsigned char*)malloc(n); 
	BytesFromZZ(p6,sk[3],n); 	
	fwrite(p6,1,n,fpriv); 	
	fclose(fpriv); 
}


void HOM::init_pub(const char* pubFile){
	FILE *fpub;
	fpub = fopen(pubFile,"rb"); 
	if (fpub!=NULL){
		int n; 
		fread(&n, sizeof(int),1,fpub); 
		unsigned char* p0 = (unsigned char*)malloc(n); 
		fread(p0,1,n,fpub); 
		ZZ k0 = ZZFromBytes(p0,n); 

		fread(&n, sizeof(int),1,fpub); 
		unsigned char* p1 = (unsigned char*)malloc(n); 
		fread(p1,1,n,fpub); 
		ZZ k1 = ZZFromBytes(p1,n); 	
		fclose(fpub); 
		vector<ZZ> pubKs = {k0,k1};
		pub = new Paillier(pubKs); 
	}
	else{
		cout << "ERROR OPENNING PUB.KEY " << endl; 
	}
}


void HOM::init_priv(const char* privFile){
	FILE *fpriv; 
	fpriv = fopen(privFile, "rb"); 
	if (fpriv!=NULL){	
		int n; 
		fread(&n, sizeof(int),1,fpriv); 
		unsigned char* s0 = (unsigned char*)malloc(n); 
		fread(s0,1,n,fpriv); 
		ZZ k0 = ZZFromBytes(s0,n); 

		fread(&n, sizeof(int),1,fpriv); 
		unsigned char* s1 = (unsigned char*)malloc(n); 
		fread(s1,1,n,fpriv); 
		ZZ k1 = ZZFromBytes(s1,n); 	

		fread(&n, sizeof(int),1,fpriv); 
		unsigned char* s2 = (unsigned char*)malloc(n); 
		fread(s2,1,n,fpriv); 
		ZZ k2 = ZZFromBytes(s2,n); 	

		fread(&n, sizeof(int),1,fpriv); 
		unsigned char* s3 = (unsigned char*)malloc(n); 
		fread(s3,1,n,fpriv); 
		ZZ k3 = ZZFromBytes(s3,n); 		
		fclose(fpriv); 

		vector<ZZ> privKs = {k0,k1,k2,k3};
		priv = new Paillier_priv(privKs);
	}
	else{
		cout << "ERROR OPENNING PRIV.KEY " << endl; 
	}
}


ZZ HOM::encrypt(const ZZ& value){
	return pub->encrypt(value); 
}


ZZ HOM::decrypt(const ZZ& val){
	return priv->decrypt(val); 
}

ZZ HOM::add(const ZZ& val1, const ZZ &val2){
	return pub->add(val1,val2); 
}

ZZ HOM::subtract(const ZZ& val1, const ZZ &val2){
	return pub->substract(val1,val2); 
}

JNIEXPORT void JNICALL Java_tds_hom_Paillier_keygen
  (JNIEnv *env, jobject obj, jstring pubf, jstring privf){
	unsigned char* pf = (unsigned char*)env->GetStringUTFChars(pubf,NULL); 
	unsigned char* pvf = (unsigned char*)env->GetStringUTFChars(privf,NULL); 
	hom.keygen((const char*)pf, (const char*)pvf);	
}

JNIEXPORT void JNICALL Java_tds_hom_Paillier_init_1public_1key
  (JNIEnv *env, jobject obj, jstring pubf){
	unsigned char* pf = (unsigned char*)env->GetStringUTFChars(pubf,NULL); 
	hom.init_pub((const char*)pf); 
}

JNIEXPORT void JNICALL Java_tds_hom_Paillier_init_1private_1key
  (JNIEnv *env, jobject obj, jstring privf){
	unsigned char* pf = (unsigned char*)env->GetStringUTFChars(privf,NULL); 
	hom.init_priv((const char*)pf); 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Paillier_encrypt
  (JNIEnv *env, jobject obj, jint val){
	ZZ enc = hom.encrypt(to_ZZ((int)val)); 
	int size = NumBytes(enc); 
	if (size<PAILLIER_LEN_BYTES)
		size = PAILLIER_LEN_BYTES;

	unsigned char* bytes = (unsigned char*)malloc(size); 
	BytesFromZZ(bytes,enc,size); 

	jbyteArray ret = env->NewByteArray(size); 
	env->SetByteArrayRegion(ret,0,size, (jbyte*)bytes); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Paillier_add
  (JNIEnv *env, jobject obj, jbyteArray vals1, jbyteArray vals2){
	int size1 = env->GetArrayLength(vals1); 
	int size2 = env->GetArrayLength(vals2); 

	unsigned char* bytes1 = (unsigned char*)malloc(size1);
	unsigned char* bytes2 = (unsigned char*)malloc(size2);
	env->GetByteArrayRegion(vals1,0,size1, (jbyte*)bytes1);
	env->GetByteArrayRegion(vals2,0,size2, (jbyte*)bytes2); 

	ZZ enc1 = ZZFromBytes(bytes1, size1);
	ZZ enc2 = ZZFromBytes(bytes2, size2); 
	ZZ sum = hom.add(enc1,enc2); 	
	
	int sizes = NumBytes(sum); 
	if (sizes < PAILLIER_LEN_BYTES)
		sizes = PAILLIER_LEN_BYTES; 

	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,sum,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Paillier_subtract
  (JNIEnv *env, jobject obj, jbyteArray vals1, jbyteArray vals2){
	int size1 = env->GetArrayLength(vals1); 
	int size2 = env->GetArrayLength(vals2); 

	unsigned char* bytes1 = (unsigned char*)malloc(size1);
	unsigned char* bytes2 = (unsigned char*)malloc(size2);
	env->GetByteArrayRegion(vals1,0,size1, (jbyte*)bytes1);
	env->GetByteArrayRegion(vals2,0,size2, (jbyte*)bytes2); 

	ZZ enc1 = ZZFromBytes(bytes1, size1);
	ZZ enc2 = ZZFromBytes(bytes2, size2); 
	ZZ sum = hom.subtract(enc1,enc2); 	
	
	int sizes = NumBytes(sum); 
	if (sizes < PAILLIER_LEN_BYTES)
		sizes = PAILLIER_LEN_BYTES; 

	unsigned char* bytes3 = (unsigned char*)malloc(sizes); 
	BytesFromZZ(bytes3,sum,sizes); 
	jbyteArray ret = env->NewByteArray(sizes); 
	env->SetByteArrayRegion(ret,0,sizes,(jbyte*)bytes3); 
	return ret; 
}

JNIEXPORT jstring JNICALL Java_tds_hom_Paillier_decryptToString
  (JNIEnv *env, jobject obj, jbyteArray vals){
	int size = env->GetArrayLength(vals); 
	unsigned char* bytes = (unsigned char*)malloc(size);
	env->GetByteArrayRegion(vals,0,size,(jbyte*)bytes); 
	ZZ sum = ZZFromBytes(bytes,size); 
	ZZ dec = hom.decrypt(sum); 
	stringstream ss;
	ss << dec; 
	return env->NewStringUTF(ss.str().c_str()); 
}
