package org.springfield.lou.image;

import java.io.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;

public class AmazonImage {
	private String key;
	private AmazonS3 s3Client;
	private String bucket;

	public AmazonImage(AmazonS3 c,String b,String k) {
		key = k;
		s3Client = c;
		bucket = b;
		
	}
	
	public String getKey() {
		return key;
	}
	
	public byte[] getBytes() {
		try {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();  
        
        S3Object obj = s3Client.getObject(bucket, key);
        S3ObjectInputStream stream = obj.getObjectContent();
        
        int nextValue = stream.read();  
        while (-1 != nextValue) {  
        	byteStream.write(nextValue);  
        	nextValue = stream.read();  
        }  
  
        byte[] blob = byteStream.toByteArray(); 
		
        obj.close();
        
		return blob;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean delete() {
        try {
        	s3Client.deleteObject(new DeleteObjectRequest(bucket, key));
        } catch (Exception e) {
        	return false;
		}
        return true;
	}
	
}


