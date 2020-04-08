package org.springfield.lou.image;

import java.io.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

public class AmazonImage {
	private String key;
	private S3ObjectInputStream stream;

	public AmazonImage(String k,S3ObjectInputStream s) {
		stream = s;
		key = k;
	}
	
	public String getKey() {
		return key;
	}
	
	public byte[] getBytes() {
		try {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();  
        int nextValue = stream.read();  
        while (-1 != nextValue) {  
        	byteStream.write(nextValue);  
        	nextValue = stream.read();  
        }  
  
        byte[] blob = byteStream.toByteArray(); 
		
		return blob;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
}


