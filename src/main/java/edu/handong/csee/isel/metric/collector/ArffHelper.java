package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import weka.core.Instances;
import weka.core.WekaPackageManager;
import weka.core.converters.ArffSaver;
import weka.core.converters.TextDirectoryLoader;
import weka.core.stemmers.SnowballStemmer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import weka.core.Utils;

public class ArffHelper {
	private String projectName;
	private String referencePath;
	private String outPath;
	private static String firstCommitTime = null;
	private static String firstCommitKey = null;

	private final static String attributeNumPatternStr = "(\\{|,)(\\d+)\\s";
	private final static Pattern attributeNumPattern = Pattern.compile(attributeNumPatternStr);

	private final static String dataKeyPatternStr = "\\{.+\\,\\d+\\s(.+)\\}";
	private final static Pattern dataKeyPattern = Pattern.compile(dataKeyPatternStr);
	
	private final static String firstCommitTimePatternStr = ".+\\{'(\\d+-\\d+-\\d+\\s+\\d+:\\d+:\\d+)',.+\\}";
	private final static Pattern firstCommitTimePattern = Pattern.compile(firstCommitTimePatternStr);
	
	private final static String firstCommitKeyPatternStr = "\\{([\\w|\\d|\\-]+.java)\\,(.+)\\}";
	private final static Pattern firstCommitKeyPattern = Pattern.compile(firstCommitKeyPatternStr);
	
	private final static String commitTimePatternStr = "\\{.+\\,\\d+\\s'(.+)'";
	private final static Pattern commitTimePattern = Pattern.compile(commitTimePatternStr);

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	

	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}


	public File getArffFromDirectory(String bowDirectoryPath) {
		File arff = null;

		System.out.println("[            getArffFromDirectory         ]\n");
		File bowDirectory = new File(bowDirectoryPath);
		TextDirectoryLoader directoryLoader = new TextDirectoryLoader();

		try {
			directoryLoader.setDirectory(bowDirectory);

			Instances dataRaw = directoryLoader.getDataSet();
//			SnowballStemmer stemmer = new SnowballStemmer();
			StringToWordVector filter = new StringToWordVector();
//			String option = "-R 1,2 -W 70000 -prune-rate -1.0 -N 0 -stemmer weka.core.stemmers.SnowballStemmer -stopwords-handler weka.core.stopwords.Null -M 1 -tokenizer weka.core.tokenizers.WordTokenizer";
//			filter.setOptions(option.split(" "));
//			filter.setStemmer(stemmer);
			filter.setWordsToKeep(70000);
			filter.setInputFormat(dataRaw);

			Instances dataFiltered = Filter.useFilter(dataRaw, filter);

			arff = new File(bowDirectoryPath + File.separator + projectName + ".arff");
			System.out.println(bowDirectoryPath + File.separator + projectName + ".arff");
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
				bow = FileUtils.readFileToString(bowFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("ERROR: cannot find '"+bowFile.getAbsolutePath() +"'");
				continue;
			}
			try {
				cVector = FileUtils.readFileToString(cVectorFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				System.err.println("ERROR: cannot find '"+cVectorFile.getAbsolutePath() +"'");
				continue;
			}
			String mergedContent = bow + "\n" + cVector;

			String mergedPath = getCleanDirectory() + File.separator + fileName;
			File mergedFile = new File(mergedPath);

			mergedFile.getParentFile().mkdirs();
			try {
				FileUtils.write(mergedFile, mergedContent, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		for (String fileName : cVectorBuggyFileMap.keySet()) {

			File bowFile = bowFileBuggyMap.get(fileName);
			File cVectorFile = cVectorBuggyFileMap.get(fileName);
			String bow = null, cVector = null;
			try {

				bow = FileUtils.readFileToString(bowFile, "UTF-8");
				cVector = FileUtils.readFileToString(cVectorFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
			String mergedContent = bow + "\n" + cVector;
//			System.out.println("getMergedBOWArffBetween- mergedContent" + mergedContent);

			String mergedPath = getBuggyDirectory() + File.separator + fileName;
//			System.out.println("getMergedBOWArffBetween- mergedPath" + mergedPath);
			File mergedFile = new File(mergedPath);

			mergedFile.getParentFile().mkdirs();
			try {
				FileUtils.write(mergedFile, mergedContent, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		System.out.println("[merged1 - getMergedBOWArffBetween - 106 Line mergedPath :" + getMergedDirectoryPath() + "                   ]");
		// 2. get merged arff
		File arff = this.getArffFromDirectory(getMergedDirectoryPath());

		if(arff == null)
			System.out.println("      it is null!!!!!!!!");
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

	/*
	 * main for metric arff parser
	 */
	/*
	public static void main(String[] args) throws IOException {

		String arffPath1 = "/Users/yangsujin/Desktop/incubator-hivemall.arff";
		String arffPath2 = "/Users/yangsujin/Desktop/incubator-hivemall_.arff";
		File arff1 = new File(arffPath1);
		File arff2 = new File(arffPath2);


	}*/


	//여기 바꿈 private static
	private String mergeData(String oldStr, String newStr) {
		StringBuffer mergedBuf = new StringBuffer();

		mergedBuf.append(oldStr.substring(0, oldStr.indexOf('}')));
		mergedBuf.append(",");
		mergedBuf.append(newStr.substring(newStr.indexOf('{') + 1, newStr.indexOf('}')));

		return mergedBuf.toString();
	}


	//여기 바꿈 private static
	private List<String> plusAttributeSize(List<String> dataLineList, int plusSize) {

		ArrayList<String> dataPlusLineList = new ArrayList<>();

		for (String line : dataLineList) {

			String newLine = plusAttributeSize(line, plusSize);
			dataPlusLineList.add(newLine);
		}

		return dataPlusLineList;
	}

	//여기 바꿈 private static
	private String plusAttributeSize(String line, int plusSize) {
		StringBuffer newLineBuf = new StringBuffer();

		Matcher m = attributeNumPattern.matcher(line);
		int lastIndex = 0;

		while (m.find()) {

			int num = Integer.parseInt(m.group(2));
			num += plusSize;

			int start = m.start(2);
			int end = m.end(2);

			newLineBuf.append(line.substring(lastIndex, start));
			newLineBuf.append(num);
			lastIndex = end;
		}
		newLineBuf.append(line.substring(lastIndex, line.length()));

		return newLineBuf.toString();
	}
	

	//여기 바꿈 private static
	private File makeMergedArff(File arff1, File arff2) throws IOException {
		File newFile = new File(arff1.getParentFile().getAbsolutePath() + File.separator + "merged.arff");

		String content1 = FileUtils.readFileToString(arff1, "UTF-8");
		String content2 = FileUtils.readFileToString(arff2, "UTF-8");

		ArrayList<String> attributeLineList1 = getAttributeLinesFrom(content1);
		ArrayList<String> attributeLineList2 = getAttributeLinesFrom(content2);

		ArrayList<String> mergedAttributeLineList = new ArrayList<>();
		mergedAttributeLineList.addAll(attributeLineList1);
		mergedAttributeLineList.addAll(attributeLineList2);

		ArrayList<String> dataLineList1 = getDataLinesFrom(content1);
		ArrayList<String> dataLineList2 = getDataLinesFrom(content2);

		int plusAttributeNum = attributeLineList1.size();
		List<String> dataPlusLineList = plusAttributeSize(dataLineList2, plusAttributeNum);

		List<String> mergedDataLineList = new ArrayList<>();

		for (int i = 0; i < dataLineList1.size(); i++) {
			String data1 = dataLineList1.get(i);
			String data2 = dataPlusLineList.get(i);
			String mergedData = mergeData(data1, data2);
			mergedDataLineList.add(mergedData);
		}

		StringBuffer newContentBuf = new StringBuffer();

		for (String line : mergedAttributeLineList) {
			newContentBuf.append(line + "\n");
		}

		newContentBuf.append("@data\n");

		for (String line : mergedDataLineList) {
			newContentBuf.append(line + "\n");
		}

		FileUtils.write(newFile, newContentBuf.toString(), "UTF-8");

		return newFile;
	}


	public File makeMergedArff(File arff1, File arff2, List<String> keyOrder) throws IOException {
		File newFile = new File(outPath + File.separator + projectName +"-data.arff");

		newFile.delete();

		String content1 = FileUtils.readFileToString(arff1, "UTF-8");
		String content2 = FileUtils.readFileToString(arff2, "UTF-8");

		ArrayList<String> attributeLineList1 = getAttributeLinesFrom(content1);
		ArrayList<String> attributeLineList2 = getAttributeLinesFrom(content2);
		
		int numberOfAttribute = attributeLineList1.size() + attributeLineList2.size() - 1;
		
		ArrayList<String> firstCommitInformation = preprocessAttribute(attributeLineList2);
		
		attributeLineList2.remove(attributeLineList2.size() - 1); // remove Last index attribute: key

		ArrayList<String> mergedAttributeLineList = new ArrayList<>();
		mergedAttributeLineList.addAll(attributeLineList1);
		mergedAttributeLineList.addAll(attributeLineList2);

		ArrayList<String> dataLineList1 = getDataLinesFrom(content1);
		ArrayList<String> dataLineList2 = getDataLinesFrom(content2);
		
		preprocessData(dataLineList2, firstCommitInformation);

		Map<String, String> keyDataMap1 = new HashMap<>(); // arff1 <key, data-line>
		Map<String, String> keyDataMap2 = new HashMap<>(); // arff2 <key, data-line>

		for (int i = 0; i < keyOrder.size(); i++) {
			String key = keyOrder.get(i);
			keyDataMap1.put(key, dataLineList1.get(i));
		}

		for (String dataLine : dataLineList2) {
			Matcher m = dataKeyPattern.matcher(dataLine);
			m.find();
			String key = m.group(1);

			dataLine = dataLine.substring(0, dataLine.lastIndexOf(',')) + "}";

			keyDataMap2.put(key, dataLine);
		}

		// Should be equal
//		System.out.println("First arff data count: " + dataLineList1.size());
//		System.out.println("Second arff data count: " + dataLineList2.size());

		int plusAttributeNum = attributeLineList1.size();
		List<String> dataPlusLineList = plusAttributeSize(dataLineList2, plusAttributeNum);
////////?????????
		for (int i = 0; i < keyOrder.size(); i++) {
			String key = keyOrder.get(i);
			keyDataMap1.put(key, dataLineList1.get(i));
		}

		for (String dataLine : dataPlusLineList) {
			Matcher m = dataKeyPattern.matcher(dataLine);
			m.find();
			String key = m.group(1);
			
			if(!dataLine.contains(","+numberOfAttribute+" ")) {
				dataLine = dataLine.substring(0, dataLine.lastIndexOf('}'));
				dataLine = dataLine + ","+numberOfAttribute+" "+ firstCommitKey + "}";
			}
//			System.out.println(dataLine);
			dataLine = dataLine.substring(0, dataLine.lastIndexOf(',')) + "}";
//			System.out.println(dataLine);
//			System.out.println();
			keyDataMap2.put(key, dataLine);
		}
/////////////////?????
		List<String> mergedDataLineList = new ArrayList<>();

		for (String key : keyDataMap1.keySet()) {
			if (!keyDataMap2.keySet().contains(key)) {
				continue;
			}

			String data1 = keyDataMap1.get(key);
			String data2 = keyDataMap2.get(key);

			String mergedData = mergeData(data1, data2);
			mergedDataLineList.add(mergedData);
		}

		StringBuffer newContentBuf = new StringBuffer();
		
		newContentBuf.append("@relation weka.filters.unsupervised.instance.NonSparseToSparse\n\n");

		for (String line : mergedAttributeLineList) {
			newContentBuf.append(line + "\n");
		}

		newContentBuf.append("\n@data\n");

		for (String line : mergedDataLineList) {
			newContentBuf.append(line + "}\n");
		}

		FileUtils.write(newFile, newContentBuf.toString(), "UTF-8");
		
		return newFile;
	}


	private ArrayList<String> preprocessAttribute(ArrayList<String> attributeLineList2) {
		ArrayList<String> firstCommitInformation = new ArrayList<String>();
		
		for(int i = 0; i < attributeLineList2.size(); i++) {
			if(attributeLineList2.get(i).startsWith("@attribute meta_data-AuthorID") || attributeLineList2.get(i).startsWith("@attribute meta_data-CommitDate") || attributeLineList2.get(i).startsWith("@attribute meta_data-Key")) {
				String[] words = attributeLineList2.get(i).split(",");
				Pattern pattern = Pattern.compile("@.+\\{(.+)");
				Matcher matcher = pattern.matcher(words[0]);
				while(matcher.find()) {
					firstCommitInformation.add(matcher.group(1));
				}
			}
		}
		
		return firstCommitInformation;
	}
	

	private void preprocessData(ArrayList<String> dataLineList2, ArrayList<String> firstCommitInformation) {
		TreeMap<Integer,String> metrics = new TreeMap<Integer,String>();
		
		for(int i = 0; i < dataLineList2.size(); i++) {
			if(dataLineList2.get(i).startsWith("{")) {
				Pattern pattern = Pattern.compile("(\\d+)\\s(\\d+\\.?\\d*)");
				Matcher matcher = pattern.matcher(dataLineList2.get(i));
				
				while(matcher.find()) {
					metrics.put( Integer.parseInt(matcher.group(1)), matcher.group(2));
				}
				
				metrics.put(5,firstCommitInformation.get(0)); 
				metrics.put(10,firstCommitInformation.get(1));
				metrics.put(20,firstCommitInformation.get(2));
				
				StringBuffer metric = new StringBuffer();
				
				metric.append("{");
				for (int key : metrics.keySet()) {
					metric.append(key+" "+metrics.get(key)+",");
				}
				metric.deleteCharAt(metric.length() - 1);
				metric.append("}");
				break;
			}
		}
	}

	//여기 바꿈 private static
	public ArrayList<String> getDataLinesFrom(String content) {
		ArrayList<String> dataLineList = new ArrayList<>();
		String[] lines = content.split("\n");

		boolean dataPart = false;
		for (String line : lines) {
			if (dataPart) {
				dataLineList.add(line);
			} else if (line.startsWith("@data")) {

				dataPart = true;
			}

		}
		return dataLineList;
	}


	//여기 바꿈 private static
	public ArrayList<String> getAttributeLinesFrom(String content) throws IOException {
		ArrayList<String> attributeLineList = new ArrayList<>();

		String[] lines = content.split("\n");

		for (String line : lines) {
			if (line.startsWith("@attribute")) {
				if(line.startsWith("@attribute meta_data-commitTime")) {
					Matcher m = firstCommitTimePattern.matcher(line);
					m.find();
					firstCommitTime = m.group(1);
				}
				if(line.startsWith("@attribute meta_data-Key")) {
					Matcher m = firstCommitKeyPattern.matcher(line);
					m.find();
					firstCommitKey = m.group(1);
				}
				attributeLineList.add(line);
			}
		}

		return attributeLineList;
	}


	public ArrayList<String> getKeyOrder() {

		ArrayList<String> fileOrder = new ArrayList<>();

		File directory = new File(getMergedDirectoryPath());

		File cleanDirectory = null;
		File buggyDirectory = null;

		for (File f : directory.listFiles()) {
			if (f.isDirectory() && cleanDirectory == null) {
				cleanDirectory = f;
			} else if (f.isDirectory()) {
				buggyDirectory = f;
			}
		}

		for (File f : cleanDirectory.listFiles()) {
			fileOrder.add(f.getName().substring(0, f.getName().length() - 4));
		}
		for (File f : buggyDirectory.listFiles()) {
			fileOrder.add(f.getName().substring(0, f.getName().length() - 4));
		}

		return fileOrder;
	}
	

	public File makeMergedDeveloperHistoryArff(File arff1, File arff2, List<String> keyOrder, String midDate) throws IOException {
		File newFileTrain = new File(referencePath + File.separator + projectName +"-train-data.arff");
		File newFileTest = new File(referencePath + File.separator + projectName +"-test-data.arff");
		File newFileTestdeveloper = new File(referencePath + File.separator + projectName +"-test-developer-data.arff");

		newFileTrain.delete();
		newFileTest.delete();
		newFileTestdeveloper.delete();

		String content1 = FileUtils.readFileToString(arff1, "UTF-8");
		String content2 = FileUtils.readFileToString(arff2, "UTF-8");

		ArrayList<String> attributeLineList1 = getAttributeLinesFrom(content1);
		ArrayList<String> attributeLineList2 = getAttributeLinesFrom(content2);
		
		int numberOfAttribute = attributeLineList1.size() + attributeLineList2.size() - 1;

		ArrayList<String> firstCommitInformation = preprocessAttribute(attributeLineList2);
		
		
		attributeLineList2.remove(attributeLineList2.size() - 2); // remove Last index attribute: key
		attributeLineList2.remove(attributeLineList2.size() - 1);
		
		
		ArrayList<String> mergedAttributeLineList = new ArrayList<>();
		mergedAttributeLineList.addAll(attributeLineList1);
		mergedAttributeLineList.addAll(attributeLineList2);
		

		ArrayList<String> dataLineList1 = getDataLinesFrom(content1);
		ArrayList<String> dataLineList2 = getDataLinesFrom(content2);
		
		preprocessData(dataLineList2, firstCommitInformation);

		Map<String, String> keyDataMap1 = new HashMap<>(); // arff1 <key, data-line>
		Map<String, String> keyDataMap2 = new HashMap<>(); // arff2 <key, data-line>
		Map<String, String> keyDataMap2NoCommitTime = new HashMap<>();

		for (int i = 0; i < keyOrder.size(); i++) {
			String key = keyOrder.get(i);
			keyDataMap1.put(key, dataLineList1.get(i));
		}

		for (String dataLine : dataLineList2) {
			Matcher m = dataKeyPattern.matcher(dataLine);
			m.find();
			String key = m.group(1);

			dataLine = dataLine.substring(0, dataLine.lastIndexOf(',')) + "}";
			String dataLine2 = dataLine.substring(0,dataLine.lastIndexOf(',')) + "}";
//			System.out.println(dataLine2);
			keyDataMap2.put(key, dataLine);
			keyDataMap2NoCommitTime.put(key, dataLine2);
		}

		// Should be equal
//		System.out.println("First arff data count: " + dataLineList1.size());
//		System.out.println("Second arff data count: " + dataLineList2.size());
//		System.out.println();
		int plusAttributeNum = attributeLineList1.size();
		List<String> dataPlusLineList = plusAttributeSize(dataLineList2, plusAttributeNum);
////////?????????
		for (int i = 0; i < keyOrder.size(); i++) {
			String key = keyOrder.get(i);
			keyDataMap1.put(key, dataLineList1.get(i));
		}

		for (String dataLine : dataPlusLineList) {
			Matcher m = dataKeyPattern.matcher(dataLine);
			m.find();
			String key = m.group(1);
			
			if(!dataLine.contains(","+numberOfAttribute+" ")) {
				dataLine = dataLine.substring(0, dataLine.lastIndexOf('}'));
				dataLine = dataLine + ","+numberOfAttribute+" "+ firstCommitKey + "}";
			}
			
			if(!dataLine.contains(","+ (numberOfAttribute - 1) +" ")) {
				String key2 = dataLine.substring(dataLine.lastIndexOf(','),dataLine.lastIndexOf('}')+1);
				dataLine = dataLine.substring(0, dataLine.lastIndexOf(','));
				dataLine = dataLine + ","+(numberOfAttribute - 1)+" '" + firstCommitTime +"'"+ key2;
			}
			
			String dataLine2 = dataLine.substring(0,dataLine.lastIndexOf(',')) + "}";
//			System.out.println(dataLine2);
			dataLine2 = dataLine2.substring(0,dataLine2.lastIndexOf(',')) + "}";
//			System.out.println(dataLine2);
//			System.out.println();
			
			keyDataMap2.put(key, dataLine);
			keyDataMap2NoCommitTime.put(key, dataLine2);
		}
/////////////////?????
		List<String> mergedDataLineList = new ArrayList<>();
		List<String> mergedDataLineListNoCommitTime = new ArrayList<>();

		for (String key : keyDataMap1.keySet()) {
			if (!keyDataMap2.keySet().contains(key)) {
				continue;
			}
			
			if (!keyDataMap2NoCommitTime.keySet().contains(key)) {
				continue;
			}

			String data1 = keyDataMap1.get(key);
			String data2 = keyDataMap2.get(key);
			String data2NoCommitTime = keyDataMap2NoCommitTime.get(key);

			String mergedData = mergeData(data1, data2);
			String mergedDataNoCommitTime = mergeData(data1, data2NoCommitTime);
			
			mergedDataLineList.add(mergedData);
			mergedDataLineListNoCommitTime.add(mergedDataNoCommitTime);
		}

		StringBuffer newContentBufTrain = new StringBuffer();
		StringBuffer newContentBufTest = new StringBuffer();
		StringBuffer newContentBufTestDeveloper = new StringBuffer();
		
//		StringBuffer newContentBuf = new StringBuffer();
		
		newContentBufTrain.append("@relation weka.filters.unsupervised.instance.NonSparseToSparse\n\n");
		newContentBufTest.append("@relation weka.filters.unsupervised.instance.NonSparseToSparse\n\n");
		newContentBufTestDeveloper.append("@relation weka.filters.unsupervised.instance.NonSparseToSparse\n\n");

		for (String line : mergedAttributeLineList) {
			newContentBufTrain.append(line + "\n");
			newContentBufTest.append(line + "\n");
			newContentBufTestDeveloper.append(line + "\n");
		}

		newContentBufTrain.append("\n@data\n");
		newContentBufTest.append("\n@data\n");
		newContentBufTestDeveloper.append("\n@data\n");

//		for (String line : mergedDataLineList) {
//			newContentBuf.append(line + "}\n");
//		}
		
		for(int i = 0; i < mergedDataLineList.size(); i++) {
//			System.out.println(mergedDataLineList.get(i));
			Matcher m = commitTimePattern.matcher(mergedDataLineList.get(i));
			m.find();
			String commitTime = m.group(1);
			
			if(midDate.compareTo(commitTime) > 0) { //if middata < commitTime = training data
				newContentBufTrain.append(mergedDataLineListNoCommitTime.get(i) + "}\n");
			}else {//test data
				newContentBufTest.append(mergedDataLineListNoCommitTime.get(i) + "}\n");
				newContentBufTestDeveloper.append(mergedDataLineList.get(i) + "}\n");
			}
		}

		FileUtils.write(newFileTrain, newContentBufTrain.toString(), "UTF-8");
		FileUtils.write(newFileTest, newContentBufTest.toString(), "UTF-8");
		FileUtils.write(newFileTestdeveloper, newContentBufTestDeveloper.toString(), "UTF-8");
		
		return newFileTrain;
	}
}