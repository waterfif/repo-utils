package com.waterfield.model.reader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;

import org.apache.maven.model.Developer;
import org.apache.maven.model.License;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import com.waterfield.model.RepoModel;

public class POMReader implements IModelReader {
	
	private static MavenXpp3Reader reader = new MavenXpp3Reader();

	@Override
	public RepoModel read(Path pathToModelDescriptor) throws ModelDescriptorException {
		
		RepoModel rm = new RepoModel();
		
		try {
			Model m = reader.read(new FileReader(pathToModelDescriptor.toFile()));
			
			StringBuilder licences = new StringBuilder();
			m.getLicenses().forEach((License lic) -> licences.append(String.format("[Licence : name %s url %s ] ", lic.getName(), lic.getUrl())));
			
			
			rm.setPath(pathToModelDescriptor);
			rm.setGroup((m.getGroupId() != null)? m.getGroupId() : m.getParent().getGroupId());
			rm.setId(m.getArtifactId());
			rm.setVersion(m.getVersion());
			rm.setLicenses((m.getLicenses() != null)? licences.toString() : null);
			rm.setDevelopers(getDevelopers(m));
			
		} catch (FileNotFoundException e) {
			String error = "Could not find " + pathToModelDescriptor;
			throw new ModelDescriptorException(error, e);
		} catch (IOException e) {
			String error = "Failed to read " + pathToModelDescriptor;
			throw new ModelDescriptorException(error, e);
		} catch (XmlPullParserException e) {
			String error = "Could not parse xml in " + pathToModelDescriptor;
			throw new ModelDescriptorException(error, e);
		}
		return rm;
	}
	
	private String getDevelopers(Model m) {
		
		StringBuilder b = new StringBuilder();
		
		m.getDevelopers().forEach((Developer dev) -> b.append(getDeveloper(dev)).append("|"));
		
		return b.toString();
	}
	
	private String getDeveloper(Developer dev) {
		String devStr = String.format("[Developer name=%s email=%s org=%s url=%s]", dev.getName(), dev.getEmail(), dev.getOrganization(), dev.getUrl());
				
		return devStr;
	}

}
