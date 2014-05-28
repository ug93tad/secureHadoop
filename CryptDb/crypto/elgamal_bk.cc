#include <iostream>
#include <NTL/ZZ.h>
#include <crypto/prng.hh>
#include <crypto/elgamal.hh>

using namespace std;
using namespace NTL;

ElGamal::ElGamal(const std::vector<ZZ> &key)
	: pK(key[0]), q(key[1]), g(key[2]), sK(key[3])
{
}

void
ElGamal::rand_gen(size_t niter, size_t nmax)
{
    if (rqueue.size() >= nmax)
        niter = 0;
    else
        niter = min(niter, nmax - rqueue.size());

    for (uint i = 0; i < niter; i++) {
        ZZ r = RandomLen_ZZ(1024) % q; //sorry for magic numbers will fix
        ZZ rq = PowerMod(g, q*r, q);
        rqueue.push_back(rq);
    }
}

vector<ZZ> ElGamal::keygen(PRNG* rng, long len, uint abits) {
	//generate a safe prime
	ZZ p = GenGermainPrime_ZZ(len, 80);
	ZZ prime = 2*p + 1; 
	
	//pick a generator in Z_q
	ZZ gen = rng->rand_zz_nbits(abits);
	
	while (!gen_check(p, prime, gen)) {
		gen = rng->rand_zz_nbits(abits);
	}
	
	ZZ x = rng->rand_zz_nbits(abits); //secret key
 
	ZZ h = PowerMod(gen, x, prime); // Calculates g^x (mod prime)
	return { h, prime, gen, x };
}

/*
* Check if gen is a generator mod q = 2p+1
*/
bool ElGamal::gen_check(ZZ p, ZZ q, ZZ gen) {
	if (PowerMod(gen, p, q) == 1) {
		return false;
	} else if(PowerMod(gen, 2, q) == 1) {
		return false;
	} 
	return true; 
}

ZZ ElGamal::encrypt(const ZZ &m){
	ZZ c1, c2;
	unsigned char c1p[96]; //768 bits = max size of q
	unsigned char c2p[96]; 

	ZZ k = RandomBnd(q); //random number in Z_q

	PowerMod(c1,g,k,q);		// c1 = g^k %q
	PowerMod(c2,pK,k,q);	// c2 = pK^k %q
	MulMod(c2,m,c2,q);	// c2 = m*pK^k %q

	//ZZ i3 = InvMod(to_ZZ(2),q);
	//MulMod(c2,i3,c2,q); 


	BytesFromZZ(c1p, c1, 96); 
	BytesFromZZ(c2p, c2, 96);

	unsigned char c[192];
	memcpy(c,c1p,96);
	memcpy(c+96,c2p,96);
	
	//return ZZFromBytes(c,192); 	
	return ZZFromBytes(c1p, 192); //turn concat of arrays into ZZ
}

ZZ ElGamal::decrypt(const ZZ &c){
	unsigned char cp[192];
	
	BytesFromZZ(cp, c, 192); 

	ZZ c1 = ZZFromBytes(cp, 96);
	ZZ c2 = ZZFromBytes((cp+96), 96);  
	
	ZZ s = PowerMod(c1,sK,q);
	InvMod(s,s,q);
	ZZ ret = MulMod(c2,s,q);
	return ret;
}

ZZ ElGamal::mul(ZZ a, ZZ b, ZZ q){
	unsigned char cpa[192];
	unsigned char cpb[192];
	
	BytesFromZZ(cpa, a, 192); 
	BytesFromZZ(cpb, b, 192); 

	ZZ ac1 = ZZFromBytes(cpa, 96);
	ZZ ac2 = ZZFromBytes((cpa+96), 96); 
	ZZ bc1 = ZZFromBytes(cpb, 96);
	ZZ bc2 = ZZFromBytes((cpb+96), 96); 

	ZZ c1 = MulMod(ac1, bc1, q);
	ZZ c2 = MulMod(ac2, bc2, q);

	c2 = c2*(to_ZZ(7)/to_ZZ(2)); 
	unsigned char c1p[96]; //768 bits = max size of q
	unsigned char c2p[96]; 

	BytesFromZZ(c1p, c1, 96); 
	BytesFromZZ(c2p, c2, 96);

	return ZZFromBytes(c1p, 192); //turn concat of arrays into ZZ
}

/*
int main(){
	ElGamal elgamal;
	urandom u;
	ZZ message;

	elgamal = ElGamal(ElGamal::keygen(&u,1000));

	message = 1000;
	cout << "Prime: " << elgamal.q << '\n';
	cout << "g: " << elgamal.g << '\n';
	cout << "sKey: " << elgamal.sK << '\n';
	cout << "pKey: " << elgamal.pK << '\n';
	
	cipher = elgamal.encrypt(message);
	cout << cipher << '\n';

	cout << elgamal.decrypt(cipher) << '\n';
	return 0;
}*/
