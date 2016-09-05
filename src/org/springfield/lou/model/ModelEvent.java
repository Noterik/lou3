package org.springfield.lou.model;

import org.springfield.fs.FsNode;

public class ModelEvent {
	public String path;
	public Object target;
	public int eventtype;
	
	public FsNode getTargetFsNode() {
		return (FsNode)target;
	}
}
