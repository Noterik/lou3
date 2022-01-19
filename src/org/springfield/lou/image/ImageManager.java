package org.springfield.lou.image;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;



import com.amazonaws.auth.EnvironmentVariableCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;


public class ImageManager {
	
	private static ImageManager instance;
	private static HashMap<String, String> scriptcommands = null;
	private static HashMap<String, String> positivecache = new HashMap<String, String>();

	private ImageManager() {
		if (scriptcommands == null) {
			scriptcommands = readCommandList();
		}
	}
	
    public static ImageManager instance(){
    	if(instance==null) instance = new ImageManager();
    	return instance;
    }
    
    public List<AmazonImage> getAmazonS3Dir(String path) {
    	String bucket = "springfield-private-storage";
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new EnvironmentVariableCredentialsProvider()).build();
        ObjectListing images = s3Client.listObjects(bucket,path); 
        //System.out.println("K="+path);
        
        List<S3ObjectSummary> list = images.getObjectSummaries();
        List<AmazonImage> results = new ArrayList<AmazonImage>();
        for(S3ObjectSummary image: list) {
            S3Object obj = s3Client.getObject(bucket, image.getKey());
            

            String key=obj.getKey();
            //System.out.println("K="+path+" "+key);
            if (key.indexOf(".jpg")!=-1) { // kind of a mistake but needed for now
            	AmazonImage ni = new AmazonImage(s3Client,bucket,key);
            	results.add(ni);            	
            }
            try {
            	obj.close();
            } catch (IOException e) {
            	e.printStackTrace();
            }
        }
    	return results;
    }
    
    
     public String getAmazonS3Path(String image,String predir,String script) {
	   	 // do we have this in positivecache ?
 		String cimage = image;
		String cr = positivecache.get(cimage+","+predir+","+script);
		if (cr!=null) {
   		 	return cr;
   	 	}
		
		
		String commands[] = null;
		String eext = null;
		
    	//String script = request.getParameter("script");
		image = image.substring(image.indexOf("/external/")+10);
		int pos  = image.lastIndexOf(".");
		if (pos!=-1) {
			eext = image.substring(pos);
			int pos2 = eext.indexOf("?");
			if (pos2!=-1) {
				eext = eext.substring(0, pos2);
			}
			image = image.substring(0,pos);
		}
		
		
    	if (script!=null) {
    		commands = applyScript(script);
    	}
	
    	if (commands==null) { //Apply default command = thumbnail
    		commands = applyScript("thumbnail");
    	}
			
    	String result = "https://s3-eu-west-1.amazonaws.com/springfield-storage/"+predir+"/";
    	String cleanresult = predir+"/";
    	
    	result += image;
    	cleanresult += image;
    	
    	String params = "";
		for (int i=0;i<commands.length;i++) {
			params+="-"+commands[i];
			
		}
		String cleanparams = params;
		try {
			params = URLEncoder.encode(params, "UTF-8");
		} catch(Exception e) {}
		
		if (eext!=null) {
			result +=params+eext+".jpg";
			cleanresult += cleanparams+eext+".jpg";
		} else {
			result +=params+".jpg";
			cleanresult += cleanparams+".jpg";
		}

		AmazonS3 s3Client = AmazonS3ClientBuilder.standard().withCredentials(new EnvironmentVariableCredentialsProvider()).build();
		if (s3Client.doesObjectExist("springfield-storage", cleanresult)) {
	    	positivecache.put(cimage+","+predir+","+script,result);
	    	return result;
		} else {
			return null;
		}
    }
	
	private static HashMap<String, String> readCommandList() {
		String filename = "/springfield/edna/config/cmdlist.xml";
		HashMap<String, String> cmdList = new HashMap<String, String>();

		try {
			DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(true);
			DocumentBuilder builder = domFactory.newDocumentBuilder();
			Document doc = builder.parse(new File(filename));
			doc.getDocumentElement().normalize();
			XPath xpath = XPathFactory.newInstance().newXPath();

			Object result = xpath.evaluate("//fsxml/cmdlist", doc,
					XPathConstants.NODESET);
			NodeList nodes = (NodeList) result;

			for (int i = 0; i < nodes.getLength(); i++) {
				Node cmdNode = (Node) xpath.evaluate("properties/name",
						nodes.item(i), XPathConstants.NODE);
				NodeList cmdSteps = (NodeList) xpath.evaluate("cmdstep/properties", nodes.item(i),
						XPathConstants.NODESET);
				String cmdline = "";
				for (int j = 0; j < cmdSteps.getLength(); j++) {
					Node nNode = cmdSteps.item(j);
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
						Element eElement = (Element) nNode;
						if (!cmdline.equals(""))
							cmdline += ",";
						cmdline += getTagValue("key", eElement) + "="
								+ getTagValue("value", eElement);
					}
				}
				cmdList.put(cmdNode.getTextContent(), cmdline);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cmdList;
	}
	
	private static String getTagValue(String sTag, Element eElement) {
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		Node nValue = nlList.item(0);
		return nValue.getNodeValue();
	}
	
	private String[] applyScript(String name) {
		String script = scriptcommands.get(name);
		if (script != null) {
				return script.split(",");
		}
		return null;
	}

	private String getOutputName(String filename, String[] commands) {

		String basedir = "/springfield/edna/outputimages";
		int pos = filename.lastIndexOf("/");
		String imagepath = filename.substring(0,pos);
		
		String cmdstring = filename.substring(pos+1);
		int pos2 = cmdstring.indexOf(".");
		String extension = cmdstring.substring(pos2);
		cmdstring = cmdstring.substring(0,pos2);
		
		for (int i=0;i<commands.length;i++) {
			cmdstring+="-"+commands[i];
		}
		
		return basedir+imagepath+"/"+cmdstring+extension;
	}

}
