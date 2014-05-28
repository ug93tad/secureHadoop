#include <crypto/elgamal.hh>

using namespace NTL;
class MUL{
	public:
		void keygen(const char* elgamalPub, const char* elgamalPriv); 
		
		//only read q
		void init_pub(const char* elgamalPub);
		//read all keys
		void init_priv(const char* elgamalPriv);
		
		ZZ encrypt(const ZZ& value); 
		ZZ decrypt(const ZZ& value); 
		ZZ multiply(const ZZ& val1, const ZZ& val2); 
	private:
		ZZ h,q,g,x;
		ElGamal *elgamal;  
};
