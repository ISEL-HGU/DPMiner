package edu.handong.csee.isel.githubcommitparser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

/**
 *  Parse the issue addresses class.
 * @author yangsujin
 *
 */
public class IssueLinkParser {
	/**
	 * Save parsing Github issues addresses.
	 */
	public static ArrayList<String> issueAddress = new ArrayList<String>();

	/**
	 * Parse the issue addresses of a specific label in Github repository.
	 * @param address	Github repository address
	 * @param label		Github repository issues label names
	 * @author yangsujin
	 *
	 */
	void parseIssueAddress(String address,String label) throws IOException{

		boolean tf = true;
		int pageNumber=1;
		Random r = new Random();
		if(label == null) label = "bug";

		while(tf) {
			String parsingAddress = address+"/issues?page="+pageNumber+"&q=label:"+label+"+is%3Aclosed";
			//System.out.println(parsingAddress);
			Document doc = Jsoup.connect(parsingAddress).header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36").get();

			Elements docLine = doc.select("a");	
			Elements docLast = doc.select("h3");

			Pattern pattern = Pattern.compile("<.+\\\"(.+)/.+\\\".+\\\".+\\\">.+");

			for(Element last : docLast) {
				if(last.toString().contains("No results matched your search")) tf=false;
				if(tf == false) break;
			}

			for(Element line : docLine) {
				if(line.toString().contains("link-gray-dark v-align-middle no-underline h4 js-navigation-open")) {
					Matcher matcher = pattern.matcher(line.toString());
					//System.out.println(line);
					while(matcher.find()) {
						issueAddress.add("https://github.com/"+matcher.group(1));
					}
				}
			}
			pageNumber++;

			int randomNumber=2000+r.nextInt(3000);
			try {
				Thread.sleep(randomNumber);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("Success to parsing issue addresses!");
//		for( int i=0; i<issueAddress.size(); i++)
//			System.out.println(i+1+" "+issueAddress.get(i));
	}

	/**
	 * Using issueAddress in another class.
	 * @return ArrayList<String>
	 */
	public static ArrayList<String> getIssueAddress() {
		return issueAddress;
	}

}