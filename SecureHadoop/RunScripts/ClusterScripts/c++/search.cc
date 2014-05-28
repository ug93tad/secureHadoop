#include<stdio.h>
#include<stdint.h>
#include<stdlib.h>
#include<iostream>
#include<string.h>
#include "crypto/SWPSearch.hh"
#include "tds_hom_Searchable.h"
using namespace std; 

JNIEXPORT void JNICALL Java_tds_hom_Searchable_init
  (JNIEnv *env, jobject obj, jstring key){
	unsigned char* k = (unsigned char*)env->GetStringUTFChars(key,NULL); 
	SWP::fixedKey = string(reinterpret_cast<const char*>(k)); 
}

JNIEXPORT jbyteArray JNICALL Java_tds_hom_Searchable_encrypt
  (JNIEnv *env, jobject obj, jstring word){
	unsigned char* k = (unsigned char*)env->GetStringUTFChars(word,NULL); 
	string pt(reinterpret_cast<char*>(k)); 
	string ctext = SWP::encrypt(SWP::fixedKey,pt); 
	jbyteArray ret = env->NewByteArray(ctext.length()); 
	env->SetByteArrayRegion(ret,0,ctext.length(), (jbyte*)ctext.c_str()); 
	return ret; 
}

JNIEXPORT jobject JNICALL Java_tds_hom_Searchable_getToken
  (JNIEnv *env, jobject obj, jstring word){
	string w(reinterpret_cast<char*>((unsigned char*)env->GetStringUTFChars(word,NULL))); 
	Token token = SWP::token(SWP::fixedKey, w);

	jclass cls = env->FindClass("tds/hom/SearchToken"); 
	jmethodID constructorId = env->GetMethodID(cls,"<init>","([B[B)V"); 
	
	jbyteArray ciphs = env->NewByteArray(token.ciph.length()); 
	jbyteArray wKs = env->NewByteArray(token.wordKey.length()); 
	env->SetByteArrayRegion(ciphs,0,token.ciph.length(),(jbyte*)token.ciph.c_str()); 
	env->SetByteArrayRegion(wKs,0,token.wordKey.length(),(jbyte*)token.wordKey.c_str()); 	


	jobject newObj = env->NewObject(cls,constructorId,ciphs,wKs); 
	return newObj; 
}

JNIEXPORT jboolean JNICALL Java_tds_hom_Searchable_match
  (JNIEnv *env, jobject obj, jbyteArray ciphertext, jobject token){
	int size = env->GetArrayLength(ciphertext); 
	unsigned char *cb = (unsigned char*)malloc(size); 
	env->GetByteArrayRegion(ciphertext,0,size, (jbyte*)cb);
	string cipher((const char*)cb,size); 	

	//token
	jclass cls = env->FindClass("tds/hom/SearchToken");
	jfieldID cipherId = env->GetFieldID(cls,"ciphertext", "[B"); 
	jfieldID wordKeyId = env->GetFieldID(cls,"wordKey", "[B"); 
	jbyteArray c = (jbyteArray)env->GetObjectField(token,cipherId); 
	jbyteArray w = (jbyteArray)env->GetObjectField(token,wordKeyId); 
	int cs = env->GetArrayLength(c); 
	unsigned char *cbs = (unsigned char*)malloc(cs); 
	env->GetByteArrayRegion(c,0,cs, (jbyte*)cbs);
	string tciph((const char*)cbs,cs); 
	
	int ws = env->GetArrayLength(w); 
	unsigned char *cws = (unsigned char*)malloc(ws); 
	env->GetByteArrayRegion(w,0,ws, (jbyte*)cws);
	string twordKey((const char*)cws,ws); 	
	
	Token t;
	t.ciph=tciph; 
	t.wordKey=twordKey; 
	return SWP::match(t, cipher);  
}

