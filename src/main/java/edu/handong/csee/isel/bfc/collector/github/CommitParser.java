package edu.handong.csee.isel.bfc.collector.github;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Parsing commit addresses and print bug commit contents to .csv file.
 * 
 * @author yangsujin
 *
 */
public class CommitParser {
	public HashSet<String> commitAddress = new HashSet<String>();
	public ArrayList<String> commitLine = new ArrayList<String>();
	private URL urlAddress; // URL address
	HttpURLConnection code;
	BufferedReader br;
	String line;
	Random r = new Random();
	private String line2;

	/**
	 * Parse commit addresses in bug issue addresses.
	 * 
	 * @param address Github repository address
	 * @throws IOException
	 */
	public void parseCommitAddress(String address) throws IOException {
		int size = IssueLinkParser.getIssueAddress().size();

		for (int i = 0; i < size; i++) {
			String issAddress = IssueLinkParser.getIssueAddress().get(i);

			Document doc = Jsoup.connect(issAddress).header("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36")
					.get();

			Elements docLine = doc.select("div.commit-meta a");

			Pattern pattern = Pattern.compile("<.+=\"/.+/.+/.+/(.+)\".+=\".+\">.+<.+>");

			for (Element line : docLine) {
				// System.out.println(line);
				Matcher matcher = pattern.matcher(line.toString());
				while (matcher.find()) {
					commitAddress.add(matcher.group(1));
				}
			}

			int randomNumber = r.nextInt(3000);
			try {
				Thread.sleep(randomNumber);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
//		System.out.println("Success to parsing bug commit addresses!");
//
//		for (int i = 0; i < commitAddress.size(); i++) {
//			System.out.println(i + 1 + commitAddress.get(i));
//		}
	}

	public HashSet<String> getCommitAddress() {
		return commitAddress;
	}

}