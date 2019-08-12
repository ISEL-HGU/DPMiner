package edu.handong.csee.isel.jira.crawler;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;

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
		System.out.println("\n\tFile " + simpleFileName +" is to be downloaded in " + dir);
		byte[] bytes = response.bodyAsBytes();
		File savedFile = new File(savedFileName);
		savedFile.getParentFile().mkdirs();
		FileUtils.writeByteArrayToFile(savedFile, bytes);
		System.out.println("\tFile " + simpleFileName +" has been downloaded in " + dir);
	}
	
	private static String validateTeamName(String domain) {
		String[] elements = domain.split("\\.");
		return (elements.length == 3) ? domain.substring(domain.indexOf('.') + 1, domain.lastIndexOf('.')) : domain; //TeamName is between . marks in domain.
	}
	
	public File collectIssueKeys() throws IOException {
		for(String file:fileList) { //extract and store issue keys into issueKeyList 
			System.out.println("\nExtracting Issue Keys from " + file);
			
			String in = FileUtils.readFileToString(new File(file), "UTF-8");
			extractIssueKeys(in, this.projectKey);
		}
		
		String issueKeysWithComma = String.join("\n", issueKeyList);
		//Set file name
		Date date= new Date();
		Timestamp ts = new Timestamp(date.getTime());
		String teamName = validateTeamName(this.domain);
		String dir = this.path + File.separator + teamName + this.projectKey + File.separator;
		String savedFileName = dir + teamName + this.projectKey + "IssueKeys" + ts + ".csv";
		//make file
		System.out.println("\n\tCollecting Issue keys into " + savedFileName);
		File savedFile = new File(savedFileName);
		savedFile.getParentFile().mkdirs();
		FileUtils.write(savedFile, issueKeysWithComma, "UTF-8");
		System.out.println("\tCollecting completed.");
		return savedFile;
	}
	
	private static void extractIssueKeys(String in, String projectKey) {
		String issueKeyRegex = "(" + projectKey + "-\\d*)";
		Pattern p = Pattern.compile(issueKeyRegex);
		Matcher m = p.matcher(in);
		while(m.find()) {
			issueKeyList.add(m.group());
		}
	}
}
