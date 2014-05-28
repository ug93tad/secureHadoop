#include <iostream>
#include <string>
#include "crypto/SWPSearch.hh"
#include "crypto/ope.hh"
#include <NTL/ZZ.h>
#include <openssl/rand.h>
#include <time.h>
#include "crypto/paillier.hh"
#include "switchable.hh"
#include <sstream>
using namespace std; 
using namespace NTL; 
const string key = "hello world ande";
const string iv = "dontusethisinput";

void test_search(){
	SWP swp; 
	cout << SWP::canDecrypt << endl;	
	list<string>* words = new list<string>(); 
	words->push_back("01234"); 
	words->push_back("012345"); 
	words->push_back("0123456789abcdefghijklm"); 
	words->push_back("01234"); 
	words->push_back("01234"); 
	words->push_back("0123456789abcdefg");
	list<string>* ctexts = SWP::encrypt(key, *words); 
	for (list<string>::iterator it = ctexts->begin(); it!=ctexts->end(); it++)
		cout << "cipher: "<< *it << endl; 
	Token t = SWP::token(key, "0123456789abcdefg"); 
	list<unsigned int> *idx = SWP::search(t,*ctexts); 
	for (list<unsigned int>::iterator it = idx->begin(); it!=idx->end() ; it++)
		cout << "FOUND AT " << *it << endl; 
	cout << "fixed key = " << SWP::fixedKey << endl; 
}

void test_ope(int val){
	cout << "val = " << val << endl; 
	string key = "secret aes key!!"; 
	OPE ope(key, 80,160);
	NTL::ZZ valz, valz1; 
	unsigned char* r = (unsigned char*) malloc(10); 
	unsigned char* ctBytes = (unsigned char*) malloc(20); 
	NTL::ZZ ct; 
	RAND_bytes(r,10);
	clock_t t1, t2; 	
	t1=clock(); 
	ZZFromBytes(valz,r,10); 
	ope.encrypt(valz); 
	t2=clock();
	cout << "first = " << (t2-t1) << endl; 
	for (int i=0; i<val; i++){
		RAND_bytes(r,10); 
	t1=clock(); 
		ZZFromBytes(valz1,r,10); 
		valz=valz+2;
		ct = ope.encrypt(valz1); 
		stringstream ss;
		ss << ct; 
		cout << "OPE size = " << ss.str().length() << endl; 
	t2=clock();
		//BytesFromZZ(ctBytes, ct, 20); 
	}
}

int test_elgamal(){
	MUL mult; 
	mult.init_pub("pub.key"); 
	mult.init_priv("priv.key");
	ZZ val1 = mult.encrypt(to_ZZ(3)); 
	cout << "decrytp val1 = " << mult.decrypt(val1) << endl; 
	ZZ val2 = mult.encrypt(to_ZZ(26)); 
	cout << "decrypt val2 = " << mult.decrypt(val2) << endl; 
	ZZ prod = mult.multiply(val1,val2); 

	cout << "decrypt elgamal = " << mult.decrypt(prod) << endl; 
}

int test_switchable(){
	
	Switchable sw;
	string key = "secret aes key!!"; 

	sw.init("/home/dinhtta/paillier.pub.key", "/home/dinhtta/paillier.priv.key", "/home/dinhtta/elgamal.pub.key", "/home/dinhtta/elgamal.priv.key", key, 80, 160); 

	HOM hom; 
	hom.init_pub("/home/dinhtta/paillier.pub.key"); 
	hom.init_priv("/home/dinhtta/paillier.priv.key");
	
	MUL mul;
	mul.init_pub("/home/dinhtta/elgamal.pub.key");
	mul.init_priv("/home/dinhtta/elgamal.priv.key"); 
	
	ZZ c1 = hom.encrypt(to_ZZ(6)); 
	ZZ c2 = hom.encrypt(to_ZZ(5)); 
	ZZ add = hom.add(c1,c2); 
	cout <<"6+5 = "<<add << endl; 
	cout <<"to OPE = "<<sw.addToOpe(add) << endl; 
	ZZ c5 = hom.encrypt(to_ZZ(12)); 
	ZZ add2 = hom.add(c1,c5);
	cout <<"to OPE 6+12 = " << sw.addToOpe(add2) << endl; 
	//ZZ mp = mul.multiply(m1, mul.encrypt(to_ZZ(6))); 
	//cout << "decrypt = " << mul.decrypt(mp) << endl; 
	//ZZ s1 = sw.mulToAdd(mp); 
	//ZZ ss = hom.add(s1, hom.encrypt(to_ZZ(7))); 
	//cout << "decrypt = " << hom.decrypt(ss) << endl; 
}

int test_paillier(){
	HOM hom; 
	hom.init_pub("pub.key");
	ZZ val1 = hom.encrypt(to_ZZ(72)); 	
	ZZ val2 = hom.encrypt(to_ZZ(127)); 
	
	ZZ mul = hom.subtract(val2,val1); 
	cout << mul << endl; 

	HOM hom1; 
	hom1.init_priv("priv.key"); 
	cout << "decrypt = " << hom1.decrypt(mul) << endl; 	

}


int main(int argc, char** argv){
	cout << "hello world" << endl;
	test_ope(atoi(argv[1])); 
	//test_search();
	//test_paillier(); 
	//HOM hom; 
	//hom.keygen("pub.key","priv.key"); 
	//test_switchable(); 
	
	//MUL mult; 
	//mult.keygen("pub.key", "priv.key"); 
	//test_elgamal(); 
	
	//HOM hom;
	//hom.keygen("add.pub.key", "add.priv.key"); 
	//MUL mult;
	//mult.keygen("mul.pub.key", "mul.priv.key"); 	

	//test_switchable(); 
	//ZZ x = to_ZZ(1234567); 
	//for (int i=0; i<30; i++)
	//	mul(x,x,to_ZZ(2345678)); 
	//cout << "result = " << x << endl;  
	//ZZ u = to_ZZ(7); 
	//ZZ v = to_ZZ(3); 
	//cout << " div = " << u/v << endl; 
	return 0; 
}
