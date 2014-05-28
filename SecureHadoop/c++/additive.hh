#include "crypto/paillier.hh"
#include <string>
using namespace std; 
using namespace NTL; 
class HOM{
	public: 
		HOM(){};		

		//storing the generated keys to file
		void keygen(const char* pubFile, const char* privFile); 

		//reading public key from pubFile
		void init_pub(const char* pubFile);
		//reading private key from privFile
		void init_priv(const char* privFile); 


		ZZ encrypt(const ZZ& value); //for ad revenue < 100 cents		
		ZZ decrypt(const ZZ& val); 
		ZZ add(const ZZ& val1, const ZZ& val2); 
		ZZ subtract(const ZZ& val1, const ZZ& val2); 		
		ZZ negate(const ZZ& val); //return -x
		static void ZZ_to_bytes(const ZZ& val, unsigned char* vals, int *len); 
	private:	
		Paillier *pub;
		Paillier_priv *priv; 
};
