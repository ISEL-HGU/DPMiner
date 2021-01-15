package edu.handong.csee.isel.metric.metadata;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;

import edu.handong.csee.isel.metric.collector.CMetricCollector;
import edu.handong.csee.isel.metric.metadata.CommitUnitInfo;
import edu.handong.csee.isel.metric.metadata.DeveloperExperienceInfo;
import edu.handong.csee.isel.metric.metadata.Metrics;
import edu.handong.csee.isel.metric.metadata.MetricCollector;
import edu.handong.csee.isel.metric.metadata.SourceFileInfo;
import edu.handong.csee.isel.metric.metadata.Utils;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.CSVLoader;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.NonSparseToSparse;

public class CommitCollector { 
//	private String inputPath;
	private String outputPath;
	private String startDate;
	private String endDate;
	private String midDate;
	private String csvOutputPath;
	private String arffOutputPath;
	private Git git;
	private Repository repo;
	private boolean developerHistory;
	ArrayList<RevCommit> commits = new ArrayList<RevCommit>();
	List<String> bugCommit = null;
	boolean allGitLog;

	private HashMap<String,DeveloperExperienceInfo> developerExperience = new HashMap<String,DeveloperExperienceInfo>();
	public HashMap<String,SourceFileInfo> sourceFileInfo = new HashMap<String,SourceFileInfo>();//source file information
	public static HashMap<String,Metrics> metrics = new HashMap<String,Metrics>();//////이놈!!!

	public CommitCollector(Git git, String resultDirectory, List<String> buggyCommit, String projectName, String startDate, String endDate, boolean developerHistory, boolean allGitLog) { // String strStartDate,String strEndDate,boolean test
		this.outputPath = resultDirectory;

		this.startDate = startDate;
		this.endDate = endDate;
		
		this.bugCommit = buggyCommit;//버그 커밋해쉬 저장
		this.git = git;
		this.csvOutputPath = outputPath + File.separator + projectName + ".csv";
		this.arffOutputPath = outputPath + File.separator + projectName + ".arff";
		this.developerHistory = developerHistory;
		this.allGitLog = allGitLog;
	}

	public void countCommitMetrics() {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		MetricCollector metricParser = new MetricCollector();
		Metrics metric;

		int count = 0;

		try {
			Iterable<RevCommit> initialCommits = null;
			
			if(allGitLog == true) {
				initialCommits = git.log().all().call();
			}else {
				initialCommits = git.log().call();
			}
			
			repo = git.getRepository();

			for (RevCommit initialCommit : initialCommits) {
				commits.add(count,initialCommit);
				count++;
			}
			//arryaList index 0 = 가장 최근 커밋 
			//arrayList index commits.size() =  첫번째 커밋

			for (int commitIndex = commits.size()-1; commitIndex > -1; commitIndex--) {// 커밋 하나씩 읽음 
				RevCommit commit = commits.get(commitIndex);

				String commitTime = Utils.getStringDateTimeFromCommitTime(commit.getCommitTime());//커밋 날짜 yyyy-MM-dd HH:mm:ss
				if(!(startDate.compareTo(commitTime)<=0 && commitTime.compareTo(endDate)<0)) // only consider BISha1 whose date is bewteen startDate and endDate
					continue;

				if (commit.getParentCount() == 0) continue;
				RevCommit parent = commit.getParent(0);
				if (parent == null)
					continue;

				List<DiffEntry> diff = Utils.diff(parent, commit, repo);

				String commitHash = commit.getName();//커밋 해쉬 
				String commitHour = Utils.getHourFromCommitTime(commit);
				String commitDay = Utils.getDayFromCommitTime(commit);//커밋한 요일 (sunday..)
				String authorId = Utils.parseAuthorID(commit.getAuthorIdent().toString());//커밋한 개발자
				CommitUnitInfo commitUnitInfo = new CommitUnitInfo();//커밋 단위 메트릭을 저장하는 instance 
				int numOfentry = 0;

				for (DiffEntry entry : diff) {// 현재 커밋에 있는 소스파일 하나씩 읽음 
					String sourcePath = entry.getNewPath().toString();

					boolean isBugCommit = isBuggy(commit, entry);//현재 커밋-소스가 버그인가? true-false
					
					if (sourcePath.indexOf("Test") >= 0 || !sourcePath.endsWith(".java"))
						continue;

					//key 생성 & 해쉬맵 생성 
					String keySourcePath = sourcePath.replaceAll("/", "-");
					String key = commitHash+"-"+keySourcePath;
//					if(key.length() > 254) {
//						if(CMetricCollector.tooLongName.containsKey(key)) {
//							key = CMetricCollector.tooLongName.get(key).toString();
//						}else {
//							System.err.println("Error : can not find key");
//						}
//					}
					metric = new Metrics();
					metrics.put(key, metric);

					String fileSource = Utils.fetchBlob(repo, commit.getName(), sourcePath);

					//save commit data to metaDataInfo
					metric.setCommitHour(commitHour);//metaDataInfo에 commit Time 저장 
					metric.setCommitDay(commitDay);//metaDataInfo에 commit Day 저장 
					metric.setCommitAuthor(authorId);//metaDataInfo에 author 저장 
					metric.setIsBugCommit((isBugCommit) ? 1 : 0);//metaDataInfo에 isBugCommit을 integer로 저장 
					metric.setCommitTime(commitTime);
					metric.setFullFilePath(sourcePath);
					metric.setFilename(parseFileName(sourcePath));

					if(numOfentry == 0) Utils.countDeveloperCommit(developerExperience,authorId,commitTime);// test와 .java를 포함하지 않은 커밋의 개발자 정보만 count 한다.
					numOfentry++;

					try (DiffFormatter formatter = new DiffFormatter(byteStream)) { //한 소스파일의 diff 읽기(코드 보기)
						formatter.setRepository(repo);
						formatter.format(entry);

						String diffContent = byteStream.toString(); // 한 소스파일의 diff를 diffContent에 저장
						metricParser.parsePatchContents(metric,commitUnitInfo, commitHash, diffContent);
						metricParser.parseSourceInfo(metric, sourceFileInfo, sourcePath, authorId, isBugCommit, commitTime, commitHash, commitUnitInfo, fileSource);
						metricParser.parseCommitUnitInfo(commitUnitInfo, sourcePath, key,developerExperience,authorId);

						byteStream.reset();
					}
				}
				if(numOfentry == 0) continue;
				metricParser.computeDeveloperInfo(developerExperience, authorId, commitTime,commitUnitInfo.getSubsystems().size());
				metricParser.computeEntropy(commitUnitInfo);

				for(int j = 0; j < commitUnitInfo.getKey().size(); j++) {
					String sourceKey = commitUnitInfo.getKey().get(j);
					metric = metrics.get(sourceKey);
					metric.setNumOfSubsystems(commitUnitInfo.getSubsystems().size());
					metric.setNumOfDirectories(commitUnitInfo.getDirectories().size());
					metric.setNumOfFiles(commitUnitInfo.getFiles().size());
					metric.setNumOfUniqueCommitToTheModifyFiles(commitUnitInfo.getPreviousCommitHashs().size());
					metric.setDeveloperExperience(developerExperience.get(authorId).getNumOfCommits());
					metric.setRecentDeveloperExperience(developerExperience.get(authorId).getREXP());
					metric.setDeveloperSubsystem(developerExperience.get(authorId).getNumOfSubsystem().size());
					metric.setEntropy(commitUnitInfo.getEntropy());
				}

			}
			byteStream.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String parseFileName(String sourcePath) {
		String[] words = sourcePath.split("/");
		return words[words.length-1];
	}

	public void saveResultToCsvFile() {

		BufferedWriter writer;
		BufferedWriter developerTrainWriter;
		BufferedWriter developerTestWriter;
		
		CSVPrinter csvPrinter;
		CSVPrinter developerCsvPrinterTrain;
		CSVPrinter developerCsvPrinterTest = null;
		
		try {
			
			if(developerHistory == false) {
				writer = new BufferedWriter(new FileWriter(csvOutputPath));
				developerTrainWriter = new BufferedWriter(new FileWriter(csvOutputPath.substring(0, csvOutputPath.lastIndexOf(".csv"))+"_Label.csv"));
				
				csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("meta_data-Modify Lines","meta_data-Add Lines","meta_data-Delete Lines","meta_data-numOfModifyChunk","meta_data-numOfAddChunk","meta_data-numOfDeleteChunk","meta_data-Distribution modified Lines","meta_data-numOfBIC","meta_data-AuthorID","meta_data-fileAge","meta_data-SumOfSourceRevision","meta_data-SumOfDeveloper","meta_data-CommitHour","meta_data-CommitDate","meta_data-AGE","meta_data-numOfSubsystems","meta_data-numOfDirectories","meta_data-numOfFiles","meta_data-NUC","meta_data-developerExperience","meta_data-REXP","meta_data-SEXP","meta_data-LT","meta_data-fileName","meta_data-fullFileName","meta_data-commitTime","Key","meta_data-Key"));
				developerCsvPrinterTrain = new CSVPrinter(developerTrainWriter, CSVFormat.DEFAULT.withHeader("isBuggy","Modify Lines","Add Lines","Delete Lines","numOfModifyChunk","numOfAddChunk","numOfDeleteChunk","Distribution modified Lines","numOfBIC","AuthorID","fileAge","SumOfSourceRevision","SumOfDeveloper","CommitHour","CommitDate","AGE","numOfSubsystems","numOfDirectories","numOfFiles","NUC","developerExperience","REXP","SEXP","LT","meta_data-commitTime","Key"));
			}else {
				writer = new BufferedWriter(new FileWriter(csvOutputPath));
				developerTrainWriter = new BufferedWriter(new FileWriter(csvOutputPath.substring(0, csvOutputPath.lastIndexOf(".csv"))+"_train_developer.csv"));
				developerTestWriter = new BufferedWriter(new FileWriter(csvOutputPath.substring(0, csvOutputPath.lastIndexOf(".csv"))+"_test_developer.csv"));

				csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("meta_data-Modify Lines","meta_data-Add Lines","meta_data-Delete Lines","meta_data-Distribution modified Lines","meta_data-numOfBIC","meta_data-AuthorID","meta_data-fileAge","meta_data-SumOfSourceRevision","meta_data-SumOfDeveloper","meta_data-CommitHour","meta_data-CommitDate","meta_data-AGE","meta_data-numOfSubsystems","meta_data-numOfDirectories","meta_data-numOfFiles","meta_data-NUC","meta_data-developerExperience","meta_data-REXP","meta_data-SEXP","meta_data-LT","meta_data-commitTime","meta_data-Key"));
				developerCsvPrinterTrain = new CSVPrinter(developerTrainWriter, CSVFormat.DEFAULT.withHeader("isBuggy","Modify Lines","Add Lines","Delete Lines","Distribution modified Lines","numOfBIC","AuthorID","fileAge","SumOfSourceRevision","SumOfDeveloper","CommitHour","CommitDate","AGE","numOfSubsystems","numOfDirectories","numOfFiles","NUC","developerExperience","REXP","SEXP","LT","Key"));
				developerCsvPrinterTest = new CSVPrinter(developerTestWriter, CSVFormat.DEFAULT.withHeader("isBuggy","Modify Lines","Add Lines","Delete Lines","Distribution modified Lines","numOfBIC","AuthorID","fileAge","SumOfSourceRevision","SumOfDeveloper","CommitHour","CommitDate","AGE","numOfSubsystems","numOfDirectories","numOfFiles","NUC","developerExperience","REXP","SEXP","LT","commitTime","Key"));
			}
			
			//no is bug commit
			Set<Map.Entry<String, Metrics>> entries = metrics.entrySet();

			for (Map.Entry<String,Metrics> entry : entries) {
				String key = entry.getKey();
				int numOfModifyLines = entry.getValue().getNumOfModifyLines();
				int numOfAddLines = entry.getValue().getNumOfAddLines();
				int numOfDeleteLines = entry.getValue().getNumOfDeleteLines();
				int numOfBIC= entry.getValue().getNumOfBIC();
				String commitAuthor = entry.getValue().getCommitAuthor();
				int fileAge = entry.getValue().getFileAge();
				int sumOfSourceRevision = entry.getValue().getSumOfSourceRevision();
				int sumOfDeveloper = entry.getValue().getSumOfDeveloper();
				String commitHour = entry.getValue().getCommitHour();
				String commitDay = entry.getValue().getCommitDay();
				int isBugCommit = entry.getValue().getIsBugCommit();
				int timeBetweenLastAndCurrentCommitDate = entry.getValue().getTimeBetweenLastAndCurrentCommitDate();
				int numOfSubsystems = entry.getValue().getNumOfSubsystems();
				int numOfDirectories = entry.getValue().getNumOfDirectories();
				int numOfFiles = entry.getValue().getNumOfFiles();
				int numOfUniqueCommitToTheModifyFiles = entry.getValue().getNumOfUniqueCommitToTheModifyFiles();
				int developerExperience = entry.getValue().getDeveloperExperience();
				float recentDeveloperExperience = entry.getValue().getRecentDeveloperExperience();
				int developerSubsystem = entry.getValue().getDeveloperSubsystem();
				int linesOfCodeBeforeTheChange = entry.getValue().getLinesOfCodeBeforeTheChange();
				String commitTime = entry.getValue().getCommitTime();
				double entropy = entry.getValue().getEntropy();
				String fullFilePath =  entry.getValue().getFullFilePath();
				String fileName = entry.getValue().getFilename();
				int numOfModifyChunk = entry.getValue().getNumOfModifyChunk();
				int numOfAddChunk = entry.getValue().getNumOfAddChunk();
				int numOfDeleteChunk = entry.getValue().getNumOfDeleteChunk();
				
				if(numOfModifyLines == 0) continue; //if the metric modify only comments, move on to the next
				
				//compute LT
				linesOfCodeBeforeTheChange = linesOfCodeBeforeTheChange - numOfAddLines + numOfDeleteLines;
				
				//normalized
				float NUC = (float)numOfUniqueCommitToTheModifyFiles/numOfFiles;
				float LA = (float)numOfAddLines/linesOfCodeBeforeTheChange;
				float LD = (float)numOfDeleteLines/linesOfCodeBeforeTheChange;
				float MoL = (float)numOfModifyLines/linesOfCodeBeforeTheChange;

				if(numOfAddLines == 0 ) LA = 0; 
				if(numOfDeleteLines == 0) LD = 0;
				if(numOfModifyLines == 0) MoL = 0;
				if(numOfUniqueCommitToTheModifyFiles <= 0 ) NUC = 0; 
				if(numOfFiles == 0) NUC = numOfUniqueCommitToTheModifyFiles;
				
				if(linesOfCodeBeforeTheChange <= 0) {
					LA = numOfAddLines;
					LD = numOfDeleteLines;
					MoL = numOfModifyLines;
				}
				
				if(developerHistory == false) {
					csvPrinter.printRecord(MoL,LA,LD,numOfModifyChunk,numOfAddChunk,numOfDeleteChunk,entropy,numOfBIC,commitAuthor,fileAge,sumOfSourceRevision,sumOfDeveloper,commitHour,commitDay,timeBetweenLastAndCurrentCommitDate,numOfSubsystems,numOfDirectories,numOfFiles,NUC,developerExperience,recentDeveloperExperience,developerSubsystem,linesOfCodeBeforeTheChange,fileName,fullFilePath,commitTime,key,key);
					developerCsvPrinterTrain.printRecord(isBugCommit == 1? "buggy" : "clean",MoL,LA,LD,numOfModifyChunk,numOfAddChunk,numOfDeleteChunk,entropy,numOfBIC,commitAuthor,fileAge,sumOfSourceRevision,sumOfDeveloper,commitHour,commitDay,timeBetweenLastAndCurrentCommitDate,numOfSubsystems,numOfDirectories,numOfFiles,NUC,developerExperience,recentDeveloperExperience,developerSubsystem,linesOfCodeBeforeTheChange,commitTime,key);
				}else {
					csvPrinter.printRecord(MoL,LA,LD,entropy,numOfBIC,commitAuthor,fileAge,sumOfSourceRevision,sumOfDeveloper,commitHour,commitDay,timeBetweenLastAndCurrentCommitDate,numOfSubsystems,numOfDirectories,numOfFiles,NUC,developerExperience,recentDeveloperExperience,developerSubsystem,linesOfCodeBeforeTheChange,commitTime,key);
					if(midDate.compareTo(commitTime) > 0) {//commit time이 mid date보다 작으면 train, 크면 test
						developerCsvPrinterTrain.printRecord(isBugCommit == 1? "buggy" : "clean",MoL,LA,LD,entropy,numOfBIC,commitAuthor,fileAge,sumOfSourceRevision,sumOfDeveloper,commitHour,commitDay,timeBetweenLastAndCurrentCommitDate,numOfSubsystems,numOfDirectories,numOfFiles,NUC,developerExperience,recentDeveloperExperience,developerSubsystem,linesOfCodeBeforeTheChange,key);
					}else {
						developerCsvPrinterTest.printRecord(isBugCommit == 1? "buggy" : "clean",MoL,LA,LD,entropy,numOfBIC,commitAuthor,fileAge,sumOfSourceRevision,sumOfDeveloper,commitHour,commitDay,timeBetweenLastAndCurrentCommitDate,numOfSubsystems,numOfDirectories,numOfFiles,NUC,developerExperience,recentDeveloperExperience,developerSubsystem,linesOfCodeBeforeTheChange,commitTime,key);

					}
				}
			}

			csvPrinter.close();
			developerCsvPrinterTrain.close();
			writer.close();
			developerTrainWriter.close();
			if(developerHistory == true) {
				developerCsvPrinterTest.close();
			}
			metrics.clear();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String CSV2ARFF() {
		try {
			CSVLoader loader = new CSVLoader();
			loader.setSource(new File(csvOutputPath));
			System.out.println(csvOutputPath);
			Instances data = loader.getDataSet();

			NonSparseToSparse nonSparseToSparseInstance = new NonSparseToSparse(); 
			nonSparseToSparseInstance.setInputFormat(data);
			Instances sparseDataset = Filter.useFilter(data, nonSparseToSparseInstance);

			ArffSaver arffSaver = new ArffSaver();
			arffSaver.setInstances(sparseDataset);

			arffSaver.setFile(new File(arffOutputPath));
			
			arffSaver.writeBatch();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return arffOutputPath;
	}

	public void parsing(String csvOutputPath) {
		//csvOutputPath = arff 파일 경로  
		ArrayList<String> arffContents = new ArrayList<String>();
		ArrayList<String> firstCommitInformation = new ArrayList<String>();
		try {
			FileReader file_reader = new FileReader(new File(csvOutputPath));
			BufferedReader bufReader = new BufferedReader(file_reader);

			String line = "";
			while((line = bufReader.readLine()) != null){
				if(line.startsWith("@attribute AuthorID") || line.startsWith("@attribute CommitTime") ||  line.startsWith("@attribute CommitDate") || line.startsWith("@attribute Key")) {
					String[] words = line.split(",");
					Pattern pattern = Pattern.compile("@.+\\{(.+)");
					Matcher matcher = pattern.matcher(words[0]);
					while(matcher.find()) {
						firstCommitInformation.add(matcher.group(1));
					}
				}

				if(line.startsWith("@attribute CommitTime")) {
					line = "@attribute CommitTime Date 'yyyy-MM-dd HH:mm:ss'";
				}

				arffContents.add(line);
			}

			for(int i = 0; i < arffContents.size(); i++) {
				if(arffContents.get(i).startsWith("{")) {
					String[] words = arffContents.get(i).split("\\}");
					words[0] = words[0] + ",5 " + firstCommitInformation.get(0) + ",9 " + firstCommitInformation.get(1) + ",10 " + firstCommitInformation.get(2) + ",20 " + firstCommitInformation.get(3) + "}";
					arffContents.set(i, words[0]);
					break;
				}
			}

			bufReader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private boolean isBuggy(RevCommit commit, DiffEntry diff) {

		for (String bic : bugCommit) {
			if (commit.getShortMessage().contains(bic)) {
				return true;
			}
			
			String key = commit.getId().getName() + "-" + diff.getNewPath().toString();
			if(key.contains(bic)) {
				return true;
			}
		}

		return false;
	}

	public void setMidDate(String midDate) {
		this.midDate = midDate;
	}
	
	public void setAllGitLog(boolean allGitLog) {
		this.allGitLog = allGitLog;
	}
}
