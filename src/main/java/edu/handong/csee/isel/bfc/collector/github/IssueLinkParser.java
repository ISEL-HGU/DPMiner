package edu.handong.csee.isel.bfc.collector.github;

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
 * 입력된 Github Repository의 특정 Label의 Issue page Link를 수집하는 서비스를 제공한다. <br>
 * Provides a service that collects the issue page link of a specific label of the entered Github Repository.
 */
public class IssueLinkParser {
	public static ArrayList<String> issueAddress = new ArrayList<String>();


	/**
	 * 입력된 Github Repository의 Label이 입력된 label이고, 상태가 Closed인 issue link를 가져온다.<br>
	 * The input label of the Github Repository is the input label, and the issue link with a status of Closed is brought.
	 * @param address the Github Repository URL.
	 * @param label an issue label managed by Github.
	 * @throws IOException
	 */
	public void parseIssueAddress(String address, String label) throws IOException {

		boolean tf = true;
		int pageNumber = 1;
		Random r = new Random();
		if (label == null)
			label = "bug";

		while (tf) {
			int numOfIssue = 0;
			String parsingAddress = address + "/issues?page=" + pageNumber + "&q=label:" + label + "+is:Aclosed";
			
			Document doc = Jsoup.connect(parsingAddress).header("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36")
					.get();
			
			Elements docLine = doc.select("a");
			Elements docLast = doc.select("h3");

			Pattern pattern = Pattern.compile("<.+\\\"(.+)/.+\\\".+\\\".+\\\">.+");

			for (Element last : docLast) {
				if (last.toString().contains("No results matched your search"))
					tf = false;
				if (tf == false)
					break;
			}
			
			for (Element line : docLine) {
				if (line.toString().contains("link-gray-dark v-align-middle no-underline h4 js-navigation-open")) {
					Matcher matcher = pattern.matcher(line.toString());
					// System.out.println(line);
					while (matcher.find()) {
						issueAddress.add("https://github.com/" + matcher.group(1));
						numOfIssue++;
					}
				}
			}
			if(numOfIssue == 0)break;
			pageNumber++;	
			
			int randomNumber = 5000 + r.nextInt(10000);
			try {
				Thread.sleep(randomNumber);
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

		}
	}

	/**
	 *
	 * @return Returns the Issue Link url list.
	 */
	public static ArrayList<String> getIssueAddress() {
		return issueAddress;
	}

}