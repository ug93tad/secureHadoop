package tds.compute.examples;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;




/*************************************************************************
 *  Compilation:  javac KendallTau.java
 *  Execution:    java KendallTau N
 *
 *  Generate two random permutations of size N, then compute the
 *  Kendall tau distance between them.
 *
 *************************************************************************/


public class KendallDistance { 
	
	//<n> <max> <file 1> <file 2> 
    public static void main(String[] args) throws NumberFormatException, IOException { 
        int n = Integer.parseInt(args[0]); 
        int maxN = Integer.parseInt(args[1]); 
        
        int[] array1 = new int[n]; 
        int[] array2 = new int[n]; 
        
        BufferedReader reader1, reader2; 
        reader1 = new BufferedReader(new FileReader(new File(args[2]))); 
        reader2 = new BufferedReader(new FileReader(new File(args[3]))); 
        
        String line;
        int i=1; 
        int size = 0; 
        while ((line=reader1.readLine())!=null){
        	array1[n-i] = Integer.parseInt(line); 
        	i++; 
        	size++; 
        }
        
        i=1; 
        while ((line=reader2.readLine())!=null){
        	array2[n-i] = Integer.parseInt(line); 
        	i++; 
        }        

        // inverse of 2nd permutation
        int[] inv = new int[n];
        for (i=0; i<n; i++)
        	inv[i] = -1; 
        
        for (i = 0; i < maxN; i++)
            inv[array1[i]] = i;
        


        // calculate Kendall tau distance
        int tau = 0;
        int j=0; 
        for (i = 0; i < maxN; i++) {
            for (j = i+1; j < maxN; j++) {
                // check if p[i] and p[j] are inverted
            	if (inv[array2[i]]==-1 || inv[array2[j]]==-1)
            		continue; 
            	else
            		if (inv[array2[i]] > inv[array2[j]]){
            			tau++;            			 
            		}
            }
        }
        System.out.println("tau = "+ tau + ", normalized tau = "+(tau*2.0/(maxN*(maxN-1)))); 

    }
}

