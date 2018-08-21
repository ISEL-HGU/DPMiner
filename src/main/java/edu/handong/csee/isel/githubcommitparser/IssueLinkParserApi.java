package edu.handong.csee.isel.githubcommitparser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IssueLinkParserApi {
	public static ArrayList<String> issueAddress = new ArrayList<String>();
	private URL urlAddress; //URL address
	HttpURLConnection code;
	BufferedReader br;
	String line;
	
	public static ArrayList<String> getIssueAddress() {
			return issueAddress;
		}
	
	void parseIssueAddressInAPI(String address) throws Exception {
		boolean tf = true;
		int pageNumber=1;
		Random r = new Random();
		Pattern addressPattern = Pattern.compile(".+/(.+/.+)");
		Pattern pattern = Pattern.compile("\".+\":\"(.+)\"");
		
		Matcher matcherAddress = addressPattern.matcher(address);
		while(matcherAddress.find()) {
			address = matcherAddress.group(1);
		}
		
		while(tf) {
			String json = "https://api.github.com/search/issues?q=label:bug+language:java+state:closed+repo:"+address+"&sort=created&order=desc&page="+pageNumber;

			urlAddress = new URL(json);
			code = (HttpURLConnection)urlAddress.openConnection(); 

			br = new BufferedReader(new InputStreamReader(code.getInputStream()));
			String line= br.readLine();

			String[] tempStr = line.split(","); 
			if(tempStr.length == 3) {
				tf = false;
				continue;
			}
			for (int i = 0; i < tempStr.length; i++) { 
				if(tempStr[i].startsWith("\"html_url\"") && (tempStr[i].contains("issues") || tempStr[i].contains("pull"))) {
					Matcher matcher = pattern.matcher(tempStr[i]);
					while(matcher.find()) {
						issueAddress.add(matcher.group(1));
					}
				}
			}
			pageNumber++;
			
			int randomNumber=r.nextInt(6000);
			System.out.println(randomNumber);
			try {
				Thread.sleep(randomNumber);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		TreeSet<String> issue = new TreeSet<String>(issueAddress);
		ArrayList<String> issueAddress = new ArrayList<String>(issue);

		for(int i=0; i < issueAddress.size(); i++) {
			System.out.println(i+1+" "+issueAddress.get(i));
		}

	}
}
