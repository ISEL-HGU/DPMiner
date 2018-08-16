package edu.handong.csee.isel.githubcommitparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class IssueLinkParserApi {
	public ArrayList<String> issueAddressApi = new ArrayList<String>();
	
	void parseIssueAddressInAPI(String address) {
		
		boolean tf = true;

		int pageNumber=1;

		Random r = new Random();

		//while(tf) {
			String parsingAddress = "https://api.github.com/search/issues?q=label:bug+language:java+state:closed+repo:PhilJay/MPAndroidChart&sort=created&order=desc&page="+pageNumber;
			
	//}
		
	}

}
