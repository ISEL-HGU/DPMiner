package edu.handong.csee.isel.githubcommitparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CommitParser {
	public ArrayList<String> commitAddress = new ArrayList<String>();
	public ArrayList<String> commitLine = new ArrayList<String>();
	private URL urlAddress; // URL address
	HttpURLConnection code;
	BufferedReader br;
	String line;

	Random r = new Random();
	private String line2;

	void parseCommitAddress(String address) throws IOException {
		int size = IssueLinkParser.getIssueAddress().size();

		for (int i = 0; i < size; i++) {
			String issAddress = IssueLinkParser.getIssueAddress().get(i);

			Document doc = Jsoup.connect(issAddress).header("User-Agent",
					"Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2743.82 Safari/537.36")
					.get();

			Elements docLine = doc.select("div.commit-meta a");

			Pattern pattern = Pattern.compile("<.+=\"(/.+/.+/.+/.+)\".+=\".+\">.+<.+>");

			for (Element line : docLine) {
				// System.out.println(line);
				Matcher matcher = pattern.matcher(line.toString());
				while (matcher.find()) {
					commitAddress.add("https://github.com" + matcher.group(1) + ".patch");
				}
			}

//			int randomNumber=r.nextInt(3000);
//
//			System.out.println(randomNumber);
//
//			try {
//				Thread.sleep(randomNumber);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
		}

		int i = 1;
		for (String l : commitAddress) {
			System.out.println(i + " " + l);
			i++;
		}
	}

	void parseAndPrintCommiContents(String address, String output, String printNumber,String conditionMin) throws IOException {
		String project = null;

		Pattern projectPattern = Pattern.compile(".+//.+/.+/(.+)");
		Matcher projectMatcher = projectPattern.matcher(address);
		while (projectMatcher.find())
			project = projectMatcher.group(1);

		String fileName = output + "/" + project + ".csv";

		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));

			CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader("Project Name", "Short Message",
					"Commit Number", "Date", "Author", "Path", "Patches"));

			for (int j = 0; j < commitAddress.size(); j++) {
				String commitAddressLine = commitAddress.get(j);

				try {
					urlAddress = new URL(commitAddressLine);
					code = (HttpURLConnection) urlAddress.openConnection();

					br = new BufferedReader(new InputStreamReader(code.getInputStream()));

					while ((line = br.readLine()) != null) {
						commitLine.add(line);
					}

					if (commitLine.size() > 0) {
						// if(printNumber == null) Integer.parseInt(printNumber);

						commitAddressLine = parseCSVComponent(commitAddressLine);

						for (int i = 4; i < commitLine.size(); i++) {
							if (commitLine.get(i).contains("diff --")) {
								line2 = " ";
								i++;
								String path = null;
								int plusMinusNumber = 0;
								//int MImm
								for (; i < commitLine.size(); i++) {
									if (commitLine.get(i).contains("diff --")) {
										i--;
										break;
									}

									if (commitLine.get(i).contains("--- a")) {
										path = commitLine.get(i);
									}

									if (commitLine.get(i).startsWith("+") || commitLine.get(i).startsWith("-"))
										plusMinusNumber++;

									if (path != null) {
										Pattern pathPattern = Pattern.compile("---\\s./[a-z]+/(.+)");
										Matcher pathMatcher = pathPattern.matcher(path);
										while (pathMatcher.find())
											path = pathMatcher.group(1);
									}
									if (commitLine.get(i).startsWith("From "))
										break;

									line2 = line2.concat(commitLine.get(i));
									line2 = line2.concat("\n");

									if (plusMinusNumber == Integer.parseInt(printNumber) + 2)
										break;

								}
								if (plusMinusNumber == Integer.parseInt(printNumber) + 2) continue;
								if (plusMinusNumber < Integer.parseInt(conditionMin)+2) continue;
								csvPrinter.printRecord(project, commitLine.get(3), commitLine.get(0), commitLine.get(2),
										commitLine.get(1), path, line2);
							}
						}
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
//				int randomNumber=r.nextInt(3000);
//
//				System.out.println(randomNumber);
//
//				try {
//					Thread.sleep(randomNumber);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
				commitLine.clear();

			}

			csvPrinter.close();

		} catch (Exception e) {
			System.out.println("Error opening the file " + fileName);
			e.printStackTrace();
			System.exit(0);
		}

	}

	private synchronized String parseCSVComponent(String commitAddressLine) {

		Pattern subjectPattern = Pattern.compile(".+:.\\[.+\\].(.+)");
		Matcher subjectMatcher = subjectPattern.matcher(commitLine.get(3));
		while (subjectMatcher.find())
			commitLine.set(3, subjectMatcher.group(1));

		Pattern numberPattern = Pattern.compile(".+\\s(.+)\\s.+\\s.+\\s.+\\s.+\\s.+");
		Matcher numberMatcher = numberPattern.matcher(commitLine.get(0));
		while (numberMatcher.find())
			commitLine.set(0, numberMatcher.group(1));

		Pattern namePattern = Pattern.compile(".+:.(.+)");
		Matcher nameMatcher = namePattern.matcher(commitLine.get(1));
		while (nameMatcher.find())
			commitLine.set(1, nameMatcher.group(1));

		Pattern datePattern = Pattern.compile(".+\\s.+,\\s(.+)\\s.+");
		Matcher dateMatcher = datePattern.matcher(commitLine.get(2));
		while (dateMatcher.find())
			commitLine.set(2, dateMatcher.group(1));

		return commitAddressLine;
	}

}