package edu.handong.csee.isel.csvProcessors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import edu.handong.csee.isel.patch.CommitStatus;

/*
 * String project;
	String shortMessage;
	String commitHash;
	int date;
	String Author;
	ArrayList<String> pathes;*/

public class CSVsetter {
	public void set(File newFile) {
		this.newFile = newFile;
	}

	public CSVsetter(File newFile) {
		this.newFile = newFile;
	}

	File newFile;

	public void makeCSVfromCommits(ArrayList<CommitStatus> commits) throws IOException {
		File folder = newFile.getParentFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(newFile.getAbsolutePath()));
				CSVPrinter csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withHeader("Project","ShortMessage" ,"Commit Hash", "Date", "Author", "Patches"));) {
			for (CommitStatus commit : commits) {
				String project = commit.getProject(); //
				String shortMessage = commit.getShortMessage(); //
				String commitHash = commit.getCommitHash();

				int date = commit.getDate();
				String dTime = this.convertCalendar(date); //

				String author = commit.getAuthor(); //
				ArrayList<String> patches = commit.getPathes();
				
				int size = 5;
				String[] patchList = new String[patches.size()+5]; //
				patchList[0] = project;
				patchList[1] = shortMessage;
				patchList[2] = commitHash;
				patchList[3] = dTime;
				patchList[4] = author;
						
				
				for (String temp : patches) {
					patchList[size++] = temp;
				}
				
//				int size = 0;
//				String[] patchList = new String[patches.size()]; //
//				for (String temp : patches) {
//					patchList[size++] = temp;
//				}

				/* csv 만드는 로직~. */
				csvPrinter.printRecord(patchList);
				
//				csvPrinter.printRecord(project, shortMessage,commitHash, dTime, author, patchList);

			}
			csvPrinter.flush();
		}
	}

	public String convertCalendar(int date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
		Date currentTime = new Date(date);
		String dTime = formatter.format(currentTime);
		return dTime;
	}
}
