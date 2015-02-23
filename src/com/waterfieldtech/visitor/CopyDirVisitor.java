package com.waterfieldtech.visitor;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyDirVisitor extends SimpleFileVisitor<Path> { 
	
	private Path fromPath;
	
	private Path toPath;
	
	private StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;
	
	public CopyDirVisitor(Path from, Path to) {
		fromPath = from;
		toPath = to;
	}
	
    @Override
    public FileVisitResult visitFile(Path file,
                                   BasicFileAttributes attr) {
    	try {
    		
    		Path copyTo = toPath.resolve(fromPath.relativize(file));
			Path copyPath = Files.copy(file, copyTo, copyOption);
			System.out.println(String.format("Copied file %s to %s", file, copyPath));
		} catch (IOException e) {
			System.err.println(String.format("Failed to copy file %s to %s", fromPath, toPath));
		}
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
    	Path targetPath = toPath.resolve(fromPath.relativize(dir));
        if(!Files.exists(targetPath)){
            try {
				Files.createDirectories(targetPath);
			} catch (IOException e) {
				System.err.println(String.format("Failed to create directory %s", targetPath));
			}
        }
        return FileVisitResult.CONTINUE;
    }
}
