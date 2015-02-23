package com.waterfieldtech;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.waterfieldtech.model.RepoModel;
import com.waterfieldtech.model.reader.IModelReader;
import com.waterfieldtech.model.reader.IVYReader;
import com.waterfieldtech.model.reader.ModelDescriptorException;
import com.waterfieldtech.model.reader.POMReader;
import com.waterfieldtech.visitor.CopyDirVisitor;
import com.waterfieldtech.visitor.DeleteDirVisitor;
import com.waterfieldtech.visitor.FileVisitor;

/** 
 * Create a report that reads POM and IVY files from 1 or more storage directories.  
 * <BR>The class works in 2 modes: 
 * 
 * <LI>Single Storage location mode</LI>
 * <LI>Comparison mode</LI>
 * 
 * <BR/><BR/>
 * Single Storage location mode is invoked using the flag -baseStorageDir &lt;path&gt; and 
 * reads all *.pom and ivy.xml files in a storage directory. 
 * 
 * <BR/><BR/>
 * Comparison mode is invoke using the flags: 
 * 
 * <LI>-baseStorageDir &lt;path&gt; : representing a storage directory</LI>
 * <LI>-comparisonStorageDir &lt;path&gt; : representing a second storage directory</LI>
 * <LI>-outputDir &lt;path&gt; : representing an output directory</LI>
 * 
 * <BR/><BR/>
 * 
 * Comparison mode checks the contents of the comparison storage directory against the contents of the <BR>
 * base storage directory and copies all items that exist in the comparison directory but not the base directory<BR> 
 * to an output directory. In addition a content.txt file is added to the output directory indicating the items <BR>
 * written to the output directory.
 * 
 * @author waterfif
 *
 */
public class RepositoryReport {
	
	private static final String CONTENTS_TXT = "/contents.txt";

	private static final String OUTPUT_DIR = "outputDir";

	private static final String COMPARISON_STORAGE_DIR = "comparisonStorageDir";

	private static final String BASE_STORAGE_DIR = "baseStorageDir";

	private static RepositoryReport deltaReport = new RepositoryReport();
	
	// POM Reader 
	private static IModelReader pomReader = new POMReader();
	
	// IVY Reader
	private static IModelReader ivyReader = new IVYReader();
	
	private static CopyDirVisitor dirVisitor;
	
	// Map of a list of RepoModel beans keyed on artifact group id. 
	private Map<String, List<RepoModel>> repoModelMap = new HashMap<String, List<RepoModel>>();
	
	/** 
	 * Walk the file tree for a storage directory and read each POM and IVY file encountered.
	 * @param startingPoint storage directory to read.
	 */
	private void findRepoDescriptors(File startingPoint) {
		
		if (startingPoint != null) {
			
			FileVisitor v = new FileVisitor();
			
			try {
				Files.walkFileTree(startingPoint.toPath(), v);
				
				v.getIVYDescriptorFiles().forEach((Path p) -> read(p, ivyReader));
				v.getPOMDescriptorFiles().forEach((Path p) -> read(p, pomReader));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Read a given POM or IVY file using a model reader and store the results in a map. 
	 */
	private void read(Path p, IModelReader reader) {
		try {
			RepoModel model = reader.read(p);			
			addModel(model);
		} catch (ModelDescriptorException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}
	
	/** 
	 * Add a RepoModel to a map keyed on artifact group id.
	 * @param model RepoModel
	 */
	private void addModel(RepoModel model) {
		
		if (model.getGroup() == null) {
			model.toString();
		}
		
		List<RepoModel> models = repoModelMap.get(model.getGroup());
		
		
		if (models == null) {
			models = new ArrayList<RepoModel>();
			repoModelMap.put(model.getGroup(), models);
		}
		
		models.add(model);
	}
	
	/** 
	 * Output each model to Standard Out.
	 */
	private void outputRepoModels(String outputFile) {
		StringBuilder report = new StringBuilder();
		repoModelMap.forEach((g, l) -> outputModel(g, l, report));
		
		if (outputFile != null) {
			BufferedWriter writer = null;
			try {
				writer = Files.newBufferedWriter(Paths.get(outputFile), Charset.defaultCharset(), 
					                                                    StandardOpenOption.CREATE);
				writer.write(report.toString());
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (writer != null) writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			System.out.println(report.toString());
		}
	}
	
	/** 
	 * Output a list of RepoModel objects for a single artifact group to StandardOut.
	 * @param group artifact group
	 * @param list List<RepoModel> objects
	 */
	private void outputModel(String group, List<RepoModel> list, StringBuilder report) {
		list.forEach((RepoModel rm) -> report.append(rm.toString() + "\n"));
	}
	
	/** 
	 * Retrieve the repo model map.
	 * @return Map<String, List<RepoModel>>
	 */
	public Map<String, List<RepoModel>> getRepoModelMap() {
		return repoModelMap;
	}
	
	/** 
	 * Build a RepositoryReport object for a storage directory.
	 * @param dir storage directory.
	 * @return RepositoryReport
	 */
	private static RepositoryReport buildReport(String dir) {
		System.out.println(String.format("Beginning tree walk at directory %s", dir));
		
		RepositoryReport report = new RepositoryReport();
		report.findRepoDescriptors(new File(dir));
		
		return report;
	}
	
	/**
	 * Build a delta report.
	 * @param groupid group id being tested from the diff map.
	 * @param diffList List<RepoModel> list of models from the diff map for the given group id.
	 * @param mainMap main map of RepoModel - we are building a delta against this map.
	 * @param deltaReport RepositoryReport.
	 */
	private static void buildDelta(String groupid, List<RepoModel> diffList, Map<String, List<RepoModel>> mainMap) {
		
		List<RepoModel> mainModels = mainMap.get(groupid); 
		
		// If the mainMap does not contain the whole group then add it all to the DeltaReport
		if (mainModels == null) diffList.forEach((RepoModel model) -> deltaReport.addModel(model));
		else {
			// Check the list of models in the main map against the model list in the diff list.
			diffList.forEach((RepoModel model) -> checkDifferences(model, mainModels));
		}
	}
	
	/** 
	 * Check whether the RepoModel is contained within the list of models. If not, add the model to the deltas report.
	 * @param m RepoModel
	 * @param mainList List<RepoModel>
	 */
	private static void checkDifferences(RepoModel m, List<RepoModel> mainList) {
		if (!mainList.contains(m)) {
			deltaReport.addModel(m);
		}
	}
	
	/** 
	 * Write deltas to the output directory from the storage directory.
	 * @param storageDirectory Storage dir
	 * @param outputDirectory Output dir for all deltas
	 */
	private static void writeDeltas(String storageDirectory, String outputDirectory) {
		
		Path base = Paths.get(storageDirectory).getParent();
		Path output = Paths.get(outputDirectory);
		
		
		if (output.toFile().exists()) {
			DeleteDirVisitor deleteVisitor = new DeleteDirVisitor();
			try {
				Files.walkFileTree(output, deleteVisitor);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		output.toFile().mkdir();
		
		dirVisitor = new CopyDirVisitor(base, output);
		deltaReport.getRepoModelMap().forEach((k, v) -> writeFiles(v));
	}
	
	/** 
	 * Write the List<RepoModel> to the output directory.
	 * @param models List<RepoModel> 
	 */
	private static void writeFiles(List<RepoModel> models) {
		models.forEach((RepoModel m) -> writeFile(m));
	}
	
	/** 
	 * Write the RepoModel to the output directory.
	 * @param m RpoModel
	 */
	private static void writeFile(RepoModel m) {
		
		try {
			Files.walkFileTree(m.getPath().getParent(), dirVisitor);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	
	/** 
	 * Create Options.
	 * @return Options
	 */
	private static Options createOptions() {
		Options opts = new Options();
		
		Option base = OptionBuilder.withArgName("path")
			.hasArg()
			.isRequired(true)
			.withDescription("Path to the base storage directory against which another storage directory will be compared.")
			.create(BASE_STORAGE_DIR);
		
		opts.addOption(base);
		
		
		Option comp = OptionBuilder.withArgName("path")
				.hasArg()
				.isRequired(false)
				.withDescription("Path to the comparison storage directory. Anything that is in the comparison directory but not the base directory will be copied to the output folder.")
				.create(COMPARISON_STORAGE_DIR);
		
		Option delta = OptionBuilder.withArgName("path")
				.hasArg()
				.isRequired(false)
				.withDescription("Path to the directory into which all output files will be copied.")
				.create(OUTPUT_DIR);
		
		opts.addOption(comp);
		opts.addOption(delta);
		
		return opts;
	}

	/** 
	 * Main
	 * See Class Javadoc for further instructions.
	 * @param args String[]
	 */
	public static void main(String[] args) {
		
		Options opts = createOptions();
		
		CommandLineParser parser = new BasicParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(opts, args);
		} catch (ParseException e) {
			
			System.err.println("Failed to parse arguments ..." + e.getMessage());
			
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "RepositoryReport", opts );
			
			System.exit(1);
		} 
		
		String dir1 = cmd.getOptionValue(BASE_STORAGE_DIR);
		
		RepositoryReport r1 = buildReport(dir1);
		
		if (cmd.hasOption(COMPARISON_STORAGE_DIR) && cmd.hasOption(OUTPUT_DIR)) {
			String dir2 = cmd.getOptionValue(COMPARISON_STORAGE_DIR);
			RepositoryReport r2 = buildReport(dir2);
			
			Map<String, List<RepoModel>> mainMap = r1.getRepoModelMap();
			Map<String, List<RepoModel>> diffMap = r2.getRepoModelMap();
			
			// If the RepoModel(s) is not in the main map then add it to the delta
			diffMap.forEach((g, l) -> buildDelta(g, l, mainMap));
			
			String outputDirectory =  cmd.getOptionValue(OUTPUT_DIR);
			
			writeDeltas(dir2, outputDirectory);
			deltaReport.outputRepoModels(outputDirectory + CONTENTS_TXT);
		} else {
			r1.outputRepoModels(null);
		}
	}

}
