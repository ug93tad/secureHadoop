#include <stdlib.h>

class Object{
	public:
		Object(){ val = 0;}
		Object(int va){ val = va;}  
		int getValue(); 
	private:
		int val; 
};
