package org.wekit.security;

import java.io.UnsupportedEncodingException;



public  class RC4CipherEntity 
{
   public  static String code(String aInput,String aKey) throws UnsupportedEncodingException 
    {
	     
    	 int[] iS = new int[256]; 
         byte[] iK = new byte[256]; 
         
         for (int i=0;i<256;i++) 
             iS[i]=i; 
             
         int j = 1; 
         
         for (short i= 0;i<256;i++) 
         { 
             iK[i]=(byte)aKey.charAt((i % aKey.length())); 
         } 
         
         j=0; 
         
         for (int i=0;i<255;i++) 
         { 
             j=(j+iS[i]+iK[i]) % 256; 
             int temp = iS[i]; 
             iS[i]=iS[j]; 
             iS[j]=temp; 
         } 
     
     
         int i=0; 
         j=0; 
         char[] iInputChar = aInput.toCharArray(); 
         char[] iOutputChar = new char[iInputChar.length]; 
         for(short x = 0;x<iInputChar.length;x++) 
         { 
             i = (i+1) % 256; 
             j = (j+iS[i]) % 256; 
             int temp = iS[i]; 
             iS[i]=iS[j]; 
             iS[j]=temp; 
             int t = (iS[i]+(iS[j] % 256)) % 256; 
             int iY = iS[t]; 
             char iCY = (char)iY; 
             iOutputChar[x] =(char)( iInputChar[x] ^ iCY) ;    
         } 
         
         return new String(iOutputChar);
    }    
}