package com.waterfieldtech.model.reader;

import java.nio.file.Path;

import com.waterfieldtech.model.RepoModel;

public interface IModelReader {
	
	public RepoModel read(Path pathToModelDescriptor) throws ModelDescriptorException;

}
