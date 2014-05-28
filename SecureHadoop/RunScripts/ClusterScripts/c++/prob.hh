#include<openssl/aes.h>

class Prob{
public:
	void init(unsigned char* key); 
	void encrypt_word(unsigned char* ptext, int length, unsigned char* randIV, unsigned char** ctext); 
	void decrypt_word(unsigned char* ctext, int length, unsigned char* randIV, unsigned char** ptext); 

	//with padding
	void encrypt_word_cbc(unsigned char* ptext, int length, unsigned char* randIV, unsigned char** ctext, int *len); 
	void decrypt_word_cbc(unsigned char* ctext, int length, unsigned char* randIV, unsigned char** ptext, int *len); 

private:
	AES_KEY aes_key;
}; 
