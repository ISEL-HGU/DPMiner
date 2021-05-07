package edu.handong.csee.isel.bfc.collector.github;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parsing commit addresses and print bug commit contents to .csv file.
 * @author yangsujin
 *
 */
public class CommitParser {
	public TreeSet<String> commitAddress = new TreeSet<String>();
	private URL urlAddress; // URL address
	HttpURLConnection code;
	BufferedReader br;
	String line;
	Random r = new Random();
	private String line2;

	/**
	 * 입력된 github repository의 issue page에서 bug fixing commit을 수집한다. (Collect bug fixing commits from the issue page of the entered github repository.)
	 * @param address 특정 github repository url이다. (It is a specific github repository url.)
	 * @throws Exception
	 */
	public void parseCommitAddress(String address) throws Exception {
		int size = IssueLinkParser.getIssueAddress().size();
		int randomNumber;
		Pattern pattern = Pattern.compile(".+/(.+)");

		for (int i = 0; i < size; i++) {
			String issAddress = IssueLinkParser.getIssueAddress().get(i);
//			System.out.println(issAddress);
			Document doc = Jsoup.connect(issAddress).header("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36")
					.get();
			
			ArrayList<String> pullRequestAddress = new ArrayList<>();
			pullRequestAddress.add(issAddress);
			
			//parsing pull request
//			Elements docLine = doc.select("div.mt-2");
//			docLine = docLine.select("div.flex-auto a");
//			
//			for (Element a : docLine) {
//
//				String line = a.toString();
//				line = line.substring(line.indexOf("\"")+1,line.length()-1);
//				line = "https://github.com" + line.substring(0,line.indexOf("\""));
//				pullRequestAddress.add(line);
//			}
			
			for(int j = 0; j < pullRequestAddress.size(); j++) {
				String commits = pullRequestAddress.get(j);
				Document docdoc = Jsoup.connect(commits).header("User-Agent",
						"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36")
						.get();
				Elements all = docdoc.select("div.d-flex.flex-auto");
				
				for(Element each : all) {
					Elements commit = each.select("div.text-right.ml-1 a");
					Elements greenRed = each.select("div.pr-1.flex-shrink-0 summary");
					if(greenRed.isEmpty()) continue;
					
					String com = commit.get(0).toString();
					String color = greenRed.get(0).toString();
					
					if(!color.startsWith("<summary class=\"text-green\">")) {
						continue;
					}
					
					com = com.substring(com.indexOf("\"")+1,com.length()-1);
					com = com.substring(0,com.indexOf("\""));
//					System.out.println(com);
					Matcher matcher = pattern.matcher(com.toString());
					while (matcher.find()) {
						commitAddress.add(matcher.group(1));
					}
					randomNumber = 3000 + r.nextInt(1000);
				}
			}
			try {
				randomNumber = 5000 + r.nextInt(10000);
				Thread.sleep(randomNumber);
			}catch(InterruptedException e) {
                e.printStackTrace();
                return;
            }
		}
		
		System.out.println("Success to parsing bug commit addresses!");
		
		int i = 0;
		for(String ele : commitAddress) {
//			System.out.println((i + 1) +" : "+ ele);
			i++;
		}
//		System.exit(0);
		
	}

	/**
	 *
	 * @return Returns a bug fixing commit.
	 */
	public HashSet<String> getCommitAddress() {
		HashSet<String> hashset = new HashSet(this.commitAddress);
		 
		return hashset;
	}

}