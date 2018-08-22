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
/*
 * 
 */
public class IssueLinkParser {
	public static ArrayList<String> issueAddress = new ArrayList<String>();

	void parseIssueAddress(String address,String label) throws IOException{

		boolean tf = true;
		int pageNumber=1;
		Random r = new Random();
		if(label == null) label = "bug";
		
		while(tf) {
			String parsingAddress = address+"/issues?page="+pageNumber+"&q=label:"+label+"+is%3Aclosed";

			Document doc = Jsoup.connect(parsingAddress).header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36").get();

			Elements docLine = doc.select("a");	
			Elements docLast = doc.select("h3");

			Pattern pattern = Pattern.compile("<.+\\\"(.+)\\\".+\\\".+\\\">.+");

			for(Element last : docLast) {
				if(last.toString().contains("No results matched your search")) tf=false;
				if(tf == false) break;
			}

			for(Element line : docLine) {
				if(line.toString().contains("link-gray-dark v-align-middle no-underline h4 js-navigation-open")) {
					Matcher matcher = pattern.matcher(line.toString());
					while(matcher.find()) {
						issueAddress.add("https://github.com/"+matcher.group(1));
					}
				}
			}
			pageNumber++;

			int randomNumber=2000+r.nextInt(3000);
			System.out.print(randomNumber+"\r");
			try {
				Thread.sleep(randomNumber);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

		int i=1;
		for(String l : issueAddress) {
			System.out.println(i+" "+l);
			i++;
		}

	}

	public static ArrayList<String> getIssueAddress() {
		// TODO Auto-generated method stub
		return issueAddress;
	}

}