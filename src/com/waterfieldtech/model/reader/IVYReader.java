package com.waterfieldtech.model.reader;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.text.ParseException;
import java.util.Arrays;

import org.apache.ivy.core.module.descriptor.Artifact;
import org.apache.ivy.core.module.descriptor.ModuleDescriptor;
import org.apache.ivy.core.module.id.ModuleRevisionId;
import org.apache.ivy.core.settings.IvySettings;
import org.apache.ivy.plugins.parser.xml.XmlModuleDescriptorParser;

import com.waterfieldtech.model.RepoModel;

public class IVYReader implements IModelReader {
	
	private static XmlModuleDescriptorParser ivyParser = XmlModuleDescriptorParser.getInstance();

	@Override
	public RepoModel read(Path pathToModelDescriptor) throws ModelDescriptorException {
		
		RepoModel rm = new RepoModel();
		
		try {
			URL url = pathToModelDescriptor.toUri().toURL();
			ModuleDescriptor desc = ivyParser.parseDescriptor(new IvySettings(), url, false);
			
			rm = new RepoModel();
			Artifact[] arts = desc.getAllArtifacts();
			Arrays.asList(arts).forEach((Artifact a) -> a.getId());
			
			ModuleRevisionId mrevid = desc.getResolvedModuleRevisionId();
			
			rm.setPath(pathToModelDescriptor);
			rm.setGroup(mrevid.getOrganisation());
			rm.setId(mrevid.getName());
			rm.setVersion(mrevid.getRevision());
			rm.setUrl(desc.getHomePage());
			
			org.apache.ivy.core.module.descriptor.License[] lics = desc.getLicenses();
			
			StringBuilder licences = new StringBuilder();
			Arrays.asList(lics).forEach((org.apache.ivy.core.module.descriptor.License lic) -> licences.append(String.format("[Licence : name %s url %s] ", lic.getName(), lic.getUrl())));
			
			rm.setLicenses((lics != null)? licences.toString() : null);
			
		} catch (MalformedURLException e) {
			
			String error = "Could not find " + pathToModelDescriptor;
			throw new ModelDescriptorException(error, e);
		} catch (ParseException e) {
			String error = "Could not parse " + pathToModelDescriptor;
			throw new ModelDescriptorException(error, e);
		} catch (IOException e) {
			String error = "Read Failure on " + pathToModelDescriptor;
			throw new ModelDescriptorException(error, e);
		}
		
		return rm;
	}
}
