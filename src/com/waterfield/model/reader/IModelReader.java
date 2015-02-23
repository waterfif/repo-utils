package com.waterfield.model.reader;

import java.nio.file.Path;

import com.waterfield.model.RepoModel;

public interface IModelReader {
	
	public RepoModel read(Path pathToModelDescriptor) throws ModelDescriptorException;

}
