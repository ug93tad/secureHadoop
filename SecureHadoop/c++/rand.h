#include<openssl/aes.h>
#include<crypto_Crypto.h>

#define AES_KEY_BITS 128

//deterministic encryption, using AES with CFB mode. 
AES_KEY aes_key;
unsigned char* iv; 

//init vector length must be 128 bit
void init(unsigned char* key, unsigned char* initVector); 
void encrypt_word(unsigned char* ptext, int length, unsigned char** ctext); 
void decrypt_word(unsigned char* ctext, int length, unsigned char** ptext); 
void encrypt_word_cbc_pad(unsigned char* ptext, int length, unsigned char** ctext); 
void decrypt_word_cbc_pad(unsigned char* ptext, int length, unsigned char** ctext); 

