package tds;

import java.io.IOException;

public class CryptoOperationNotSupportedException extends IOException{
	public CryptoOperationNotSupportedException(String msg){
		super(msg); 
	}
}
