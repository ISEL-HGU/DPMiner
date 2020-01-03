package edu.handong.csee.isel.bfc.collector.jira;

import java.io.File;
import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

public class JiraBugIssueCrawler {
	private String domain;
	private String projectKey;
	private String path;
	
	private static boolean invalidProjectKeyChecker = true;
	private static int disconnectionCausedByInvalidProjectKeyCount = 0;
	
	private static final int INITIAL_START = -1000;
	private static final int INITIAL_END = 1;
	private static final int PERIOD = Integer.parseUnsignedInt("500");
	private static final int MAX_DISCONNECTION = 50; //TODO using mathematical methods to improve
	private static final String DEFAULT_PATH = System.getProperty("user.dir"); //current working directory
	
	public JiraBugIssueCrawler(String domain, String projectKey) throws InvalidDomainException{
		this(domain, projectKey, DEFAULT_PATH);
	}
	
	public JiraBugIssueCrawler(String domain, String projectKey, String path) throws InvalidDomainException {
		this.domain = validateDomain(domain);
		this.projectKey = projectKey;
		this.path = path;
	}

	public File getJiraBugs() throws IOException, InvalidProjectKeyException {
		Period period = new Period(INITIAL_START, INITIAL_END);
		JQLManager jqlManager = new JQLManager(this.projectKey);
		URLManager urlManager = new URLManager(this.domain);
		FileManager fileManager = new FileManager(this.path, this.domain, this.projectKey);
		
		String encodedJql = jqlManager.getEncodedJQL(jqlManager.getJQL1(period.getEnd()));
		String linkUrl = urlManager.getURL(encodedJql);
		Connection.Response response = getResponse(linkUrl);
		System.out.println("\n\tSearching bug issues before " + period.getEnd() + " days");
		
		boolean flag1 = requestSucceed(response.statusCode());  //flag1 is an indicator that checks whether a response was succeeded when approached linkUrl with encoded JQL1.
		
		while(!flag1) {
			encodedJql = jqlManager.getEncodedJQL(jqlManager.getJQL2(period.getStart(), period.getEnd()));
			linkUrl = urlManager.getURL(encodedJql);
			response = getResponse(linkUrl);
			System.out.println("\n\tSearching bug issues from " + period.getStart() + " days to " + period.getEnd() + " days");
	
			boolean flag2 = requestSucceed(response.statusCode()); //flag2 is an indicator that checks whether a response was succeeded when approached linkUrl with encoded JQL2.
			
			boolean originalFlag2 = flag2; //originalFlag2 is same as value of flag2 before increasing period or decreasing period.
			int originalStart = period.getStart(); //originalStart is same as value of start before increasing period or decreasing period.
			
			while(flag2 == originalFlag2) {
				originalStart = period.getStart();
				if(flag2) { 
					period.increasePeriod();
					System.out.println("\tIncreasing period...");
				}else {
					if(invalidProjectKeyChecker && disconnectionCausedByInvalidProjectKeyCount > MAX_DISCONNECTION) {
						throw new InvalidProjectKeyException();
					}
					period.decreasePeriod();
					System.out.println("\tDecreasing period...");
					disconnectionCausedByInvalidProjectKeyCount++;
				}
				
				encodedJql = jqlManager.getEncodedJQL(jqlManager.getJQL2(period.getStart(), period.getEnd()));
				linkUrl = urlManager.getURL(encodedJql);
				response = getResponse(linkUrl);
				System.out.println("\n\tSearching bug issues from " + period.getStart() + " days to " + period.getEnd() + " days");
				
				flag2 = requestSucceed(response.statusCode());
			}
			
			offInvalidProjectKeyChecking(); //From now on, there is no possibilities that the user may have entered nonexistent project key.
			
			if(originalFlag2) { 
				period.setStart(originalStart); //recover original value of start only when period was increased.
			}
			
			encodedJql = jqlManager.getEncodedJQL(jqlManager.getJQL2(period.getStart(), period.getEnd()));
			linkUrl = urlManager.getURL(encodedJql);
			response = getResponse(linkUrl);
			System.out.println("\n\tSearching bug issues from " + period.getStart() + " days to " + period.getEnd() + " days");
			
			fileManager.storeCSVFile(response); //store CSV file
			
			period.movePeriod(PERIOD);
			
			encodedJql = jqlManager.getEncodedJQL(jqlManager.getJQL1(period.getEnd()));
			linkUrl = urlManager.getURL(encodedJql);
			
			response = getResponse(linkUrl);
			System.out.println("\n\tSearching bug issues before " + period.getEnd() + " days");
			
			flag1 = requestSucceed(response.statusCode());
		}
		
		fileManager.storeCSVFile(response); //store CSV file
		
		return fileManager.collectIssueKeys(); //collect and store issue keys into CSV file
	}
	
	private void offInvalidProjectKeyChecking() {
		invalidProjectKeyChecker = false;
	}
	
	private static String validateDomain(String domain) throws InvalidDomainException {
		String str = domain;
		String domainRegex = "(?:[a-z0-9](?:[a-z0-9-]{0,61}[a-z0-9])?\\.)+[a-z0-9][a-z0-9-]{0,61}[a-z0-9]";
		
		if(!str.matches(domainRegex)) {
			throw new InvalidDomainException();
		}
		
		if(str.equals("issues.apache.org")) {//Apache has /jira in the back.
			str = str.concat("/jira");
		}
		
		return str;
	}
	
	private static Connection.Response getResponse(String url) throws IOException{
		System.out.println("\nConnecting " + url + "...");
		return Jsoup.connect(url)
				.maxBodySize(0)
				.timeout(600000)
				.ignoreHttpErrors(true)
				.execute();
	}
	
	private static boolean requestSucceed(int statusCode) {
		return (statusCode / 100 == 2); //status code 2xx means that request has been succeeded.
	}
}