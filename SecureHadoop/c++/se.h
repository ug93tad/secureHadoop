#include<det.h>

#define HALF_BLOCK_SIZE 8 

#this implementation is modified from CryptDb
#use the same key for encrypting word and generating k for F_k

extern AES_KEY seKey; 

void PRP(unsigned char* input, int lenIn, unsigned char** output, int lenOut); 

void detEncrypt(unsigned char* word, int lenIn, unsigned char** output, int lenOut); 

void seEncrypt(unsigned char* word, int lenIn, unsigned char** output, int lenOut); 

#search, using 
bool search(unsigned char* ctext, int lenCt, unsigned char* si, int lenSi, unsigned char* fsi, int lenFsi); 

