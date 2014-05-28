CC=g++ 
CFLAGS=-I $(JAVA_DIR)/include -I $(JAVA_DIR)/include/linux -I . -fPIC -std=c++11 -I /home/dinhtta/local/include/edb
LIBS = -lcrypto -lntl -lgmp -L/home/dinhtta/local/lib -ledbcrypto -ledbutil
OUT = libhryto.so
OBJS = prob.o search.o ope.o additive.o  multiplicative.o switchable.o 
 
EXE = test

%.o: %.c %.h  
	$(CC) -c $< $(CFLAGS)

%.o: %.cc
	$(CC) -c $< $(CFLAGS) -o $@

.PHONY: all
all: crypto exe
	$(CC) -o $(EXE) test.o $(OBJS) $(LIBS)
crypto: $(OBJS)
	$(CC) -shared -o $(OUT) $^ $(CFLAGS) $(LIBS)


clean: 
	rm -rf *.o *.so

exe: test.cc
	$(CC) -c $< $(CFLAGS)
