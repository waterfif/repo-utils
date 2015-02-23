package com.waterfieldtech.visitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class FileVisitor extends SimpleFileVisitor<Path> { 
	
	private List<Path> pomDescriptorFiles = new ArrayList<Path>();
	
	private List<Path> ivyDescriptorFiles = new ArrayList<Path>();
	
    @Override
    public FileVisitResult visitFile(Path file,
                                   BasicFileAttributes attr) {
    	
        if (attr.isRegularFile()) {
            
            String name = file.toFile().getName();
            
            if (name.endsWith(".pom")) {
            	//System.out.format("Descriptor file: %s ", file);
            	pomDescriptorFiles.add(file);
            } else if ("ivy.xml".equals(name)) {
            	//System.out.format("Descriptor file: %s ", file);
            	ivyDescriptorFiles.add(file);
            } 
        } 
        
        return FileVisitResult.CONTINUE;
    }

    // Print each directory visited.
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        FileVisitResult result = FileVisitResult.CONTINUE;
        
        if (dir.getFileName().toFile().getName().startsWith(".")) {
        	result = FileVisitResult.SKIP_SUBTREE;
        	//System.out.format("Skipping Directory: %s%n", dir);
        } 
        
        return result;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException 
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                       IOException exc) {
        System.err.println(exc);
        return FileVisitResult.CONTINUE;
    }

	public List<Path> getPOMDescriptorFiles() {
		return pomDescriptorFiles;
	}
	
	public List<Path> getIVYDescriptorFiles() {
		return ivyDescriptorFiles;
	}
}
