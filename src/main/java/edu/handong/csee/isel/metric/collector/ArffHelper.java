package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

public class ArffHelper {
	private String projectName;
	private String referencePath;

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}

	public File getArffFromDirectory(String bowDirectoryPath) {
		File arff = null;

		File bowDirectory = new File(bowDirectoryPath);
		TextDirectoryLoader directoryLoader = new TextDirectoryLoader();

		try {
			directoryLoader.setDirectory(bowDirectory);

			Instances dataRaw = directoryLoader.getDataSet();
			SnowballStemmer stemmer = new SnowballStemmer();
			StringToWordVector filter = new StringToWordVector();
			String option = "-R 1,2 -W 70000 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.SnowballStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer weka.core.tokenizers.WordTokenizer";
			filter.setOptions(option.split(" "));
			filter.setStemmer(stemmer);
			filter.setInputFormat(dataRaw);

			Instances dataFiltered = Filter.useFilter(dataRaw, filter);

			arff = new File(bowDirectoryPath + File.separator + projectName + ".arff");
			ArffSaver arffSaver = new ArffSaver();
			arffSaver.setInstances(dataFiltered);
			arffSaver.setFile(arff);
			arffSaver.writeBatch();

			return arff;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arff;
	}

	public File getMergedBOWArffBetween(BagOfWordsCollector bowCollector,
			CharacteristicVectorCollector cVectorCollector) {

		File cleanDirectory = getCleanDirectory();
		File buggyDirectory = getBuggyDirectory();
		if (cleanDirectory.exists()) {
			cleanDirectory.delete();
		}
		if (buggyDirectory.exists()) {
			buggyDirectory.delete();
		}
		
		// 1. get merged directory
		HashMap<String, File> bowFileCleanMap = new HashMap<>();
		HashMap<String, File> bowFileBuggyMap = new HashMap<>();
		HashMap<String, File> cVectorCleanFileMap = new HashMap<>();
		HashMap<String, File> cVectorBuggyFileMap = new HashMap<>();

		for (File file : bowCollector.getCleanDirectory().listFiles()) {
			bowFileCleanMap.put(file.getName(), file);
		}

		for (File file : bowCollector.getBuggyDirectory().listFiles()) {
			bowFileBuggyMap.put(file.getName(), file);
		}

		for (File file : cVectorCollector.getCleanDirectory().listFiles()) {
			cVectorCleanFileMap.put(file.getName(), file);
		}

		for (File file : cVectorCollector.getBuggyDirectory().listFiles()) {
			cVectorBuggyFileMap.put(file.getName(), file);
		}
		
		

		for (String fileName : cVectorCleanFileMap.keySet()) {

			File bowFile = bowFileCleanMap.get(fileName);
			File cVectorFile = cVectorCleanFileMap.get(fileName);
			String bow = null, cVector = null;
			try {
				
				if(!bowFile.exists()) {
					throw new Exception("Not exist: " + bowFile.getAbsolutePath());
				}
				if(!cVectorFile.exists()) {
					throw new Exception("Not exist: " + cVectorFile.getAbsolutePath());
				}
				
				bow = FileUtils.readFileToString(bowFile, "UTF-8");
				cVector = FileUtils.readFileToString(cVectorFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			String mergedContent = bow + "\n" + cVector;
			
			String mergedPath = getCleanDirectory() + File.separator + fileName;
			File mergedFile = new File(mergedPath);
			
			mergedFile.getParentFile().mkdirs();
			try {
				FileUtils.write(mergedFile, mergedContent,"UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		for (String fileName : cVectorBuggyFileMap.keySet()) {

			File bowFile = bowFileBuggyMap.get(fileName);
			File cVectorFile = cVectorBuggyFileMap.get(fileName);
			String bow = null, cVector = null;
			try {
				
				if(!bowFile.exists()) {
					throw new Exception("Not exist: " + bowFile.getAbsolutePath());
				}
				if(!cVectorFile.exists()) {
					throw new Exception("Not exist: " + cVectorFile.getAbsolutePath());
				}
				
				bow = FileUtils.readFileToString(bowFile, "UTF-8");
				cVector = FileUtils.readFileToString(cVectorFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			String mergedContent = bow + "\n" + cVector;
			
			String mergedPath = getBuggyDirectory() + File.separator + fileName;
			File mergedFile = new File(mergedPath);
			
			mergedFile.getParentFile().mkdirs();
			try {
				FileUtils.write(mergedFile, mergedContent,"UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("merged path:" + getMergedDirectoryPath());
		System.out.println("buggy path:" + getBuggyDirectory());
		System.out.println("clean path:" + getCleanDirectory());
		
		
		// 2. get merged arff
		File arff = this.getArffFromDirectory(getMergedDirectoryPath());

		return arff;
	}

	public void setReferencePath(String referencePath) {
		this.referencePath = referencePath;

	}

	public String getMergedDirectoryPath() {
		return referencePath + File.separator + projectName + "-merged-bow";
	}
	
	public File getBuggyDirectory() {

		String directoryPath = getMergedDirectoryPath();
		String path = directoryPath + File.separator + "buggy";
		return new File(path);
	}

	public File getCleanDirectory() {
		String directoryPath = getMergedDirectoryPath();
		String path = directoryPath + File.separator + "clean";
		return new File(path);
	}
}
