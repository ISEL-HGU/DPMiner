package edu.handong.csee.isel.bfc.collector.jira;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;

/**
 * 지라에서 정보를 수집할 때, 필요한 정보를 파일로 반환해주거나 저장하는 서비스를 제공한다. <br>
 * When Jira collects information, it provides a service that returns or saves necessary information as a file.
 */
public class FileManager {
	private String path;
	private String domain;
	private String projectKey;

	private List<String> fileList = new ArrayList<>();
	private static List<String> issueKeyList = new ArrayList<>();
	
	public FileManager(String path, String domain, String projectKey) {
		super();
		this.path = path;
		this.domain = domain;
		this.projectKey = projectKey;
	}

	/**
	 *
	 * @param response Save jira's bug issue key list as a file.
	 * @throws IOException
	 */
	public void storeCSVFile(Connection.Response response) throws IOException {
		//Set file name
		Date date= new Date();
		Timestamp ts = new Timestamp(date.getTime());
		String teamName = validateTeamName(this.domain);
		String dir = this.path + File.separator + teamName + this.projectKey + File.separator;
		String savedFileName = dir + teamName + this.projectKey + ts + ".csv";
		String simpleFileName = savedFileName.substring(savedFileName.lastIndexOf(File.separator)+1);
		
		//insert file into fileList
		fileList.add(savedFileName);
		
		//download csv files
//		System.out.println("\n\tFile " + simpleFileName +" is to be downloaded in " + dir);
		byte[] bytes = response.bodyAsBytes();
		File savedFile = new File(savedFileName);
		savedFile.getParentFile().mkdirs();
		FileUtils.writeByteArrayToFile(savedFile, bytes);
//		System.out.println("\tFile " + simpleFileName +" has been downloaded in " + dir);
	}
	
	private static String validateTeamName(String domain) {
		String[] elements = domain.split("\\.");
		return (elements.length == 3) ? domain.substring(domain.indexOf('.') + 1, domain.lastIndexOf('.')) : domain; //TeamName is between . marks in domain.
	}

	/**
	 *
	 * @return Returns the jira issue key list as a file.
	 * @throws IOException
	 */
	public File collectIssueKeys() throws IOException {
		for(String file:fileList) { //extract and store issue keys into issueKeyList 
//			System.out.println("\nExtracting Issue Keys from " + file);
			
			String in = FileUtils.readFileToString(new File(file), "UTF-8");
			extractIssueKeys(in);
		}
		
		String issueKeysWithNewLine = String.join("\n", issueKeyList);
		//Set file name
		String teamName = validateTeamName(this.domain);
		String dir = this.path + File.separator + teamName + this.projectKey + File.separator;
		String savedFileName = dir + teamName + this.projectKey + "IssueKeys.csv";
		// 
		System.out.println("\n\tCollecting Issue keys into " + savedFileName);
		File savedFile = new File(savedFileName);
		savedFile.getParentFile().mkdirs();
		FileUtils.write(savedFile, issueKeysWithNewLine, "UTF-8");
		System.out.println("\tCollecting completed.");
		
		return savedFile;
	}
	
	private static void extractIssueKeys(String in) {
		String[] fileContentsPerLine = in.split("\n");
		
		for(int i = 1; i < fileContentsPerLine.length; i++) { //From the second line, issue key is included.
			int initialCommaIdx = fileContentsPerLine[i].indexOf(",") + 1; 
			int nextCommaIdx = fileContentsPerLine[i].indexOf(",", initialCommaIdx + 1);
			
			//The issue key is located between the first comma location and then the comma location.
			String issueKey = fileContentsPerLine[i].substring(initialCommaIdx, nextCommaIdx);
			issueKeyList.add(issueKey);
		}
	}
}
