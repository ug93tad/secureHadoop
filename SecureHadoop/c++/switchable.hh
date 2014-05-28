#include<NTL/ZZ.h>
#include "additive.hh"
#include "multiplicative.hh"
#include "crypto/ope.hh"

using namespace NTL; 
class Switchable{
	public:
		void keygen(const char* paillierPub, const char* paillierPriv, const char* elgamalPub, const char* elgamalPriv); 
		void init(const char* paillierPub, const char* paillierPriv, const char* elgamalPub, const char* elgamaPriv);
		void init(const char* paillierPub, const char* paillierPriv, const char* elgamalPub, const char* elgamaPriv, string opeKey, int pLen, int cLen);
	
		ZZ addToMul(const ZZ& ct); 
		ZZ addToOpe(const ZZ& ct); 
		ZZ mulToAdd(const ZZ& ct); 
		ZZ mulToOpe(const ZZ& ct); 
		ZZ opeToAdd(const ZZ& ct);
		ZZ opeToMul(const ZZ& ct); 
		int getOpeLen(); 
		 		 
	private:
		HOM additive; 
		MUL multiplicative; 		
		OPE* ope; 
};
