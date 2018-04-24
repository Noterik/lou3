package org.springfield.lou.servlet;

import org.springfield.lou.model.Model;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import org.springfield.fs.FsPropertySet;

public class UploadProgressListener implements ProgressListener {
	private int onamazon=0;
	private Model model;
	private String publicurl;
	private String cfilename;
	private double cfilesize;
	String targetid;
	
	public UploadProgressListener(Model m,String u,String cfn,String cfs,String t) {
		model = m;
		publicurl = u;
		cfilename = cfn;
		targetid = t;
		try {
			cfilesize = Double.parseDouble(cfs);
		} catch(Exception e) {
		}
		
	}
	
	public void progressChanged(ProgressEvent progressEvent) {	
		onamazon +=progressEvent.getBytesTransferred();
		double perc = (onamazon/cfilesize)*100;
	//	System.out.println("size final : "+onamazon+" cfilesize="+cfilesize+" perc="+ (int)perc);
		
		
		FsPropertySet ps = new FsPropertySet();
		ps.setProperty("action","progress");
		ps.setProperty("progress",""+(int)perc);
		ps.setProperty("cfilename",cfilename);
		ps.setProperty("url",publicurl);

		model.setProperties("/screen/upload/"+targetid,ps);
	}

}
