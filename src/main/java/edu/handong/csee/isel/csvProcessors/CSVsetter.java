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

	public void makeCSVfromCommits(ArrayList<CommitStatus> commits, String[] headers) throws IOException {
		File folder = newFile.getParentFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(newFile.getAbsolutePath()));
				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));) {
			for (CommitStatus commit : commits) {
				String project = commit.getProject(); //
				String shortMessage = commit.getShortMessage(); //
				String commitHash = commit.getCommitHash();
				
				int date = commit.getDate();
				String dTime = this.convertCalendar(date); //

				String author = commit.getAuthor(); //
				String path = commit.getPath();
				String patch = commit.getPatch();
				
				csvPrinter.printRecord(project, shortMessage, commitHash, dTime, author, path, patch);

			}
			csvPrinter.flush();
		}
	}

	public String convertCalendar(int date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
		Date currentTime = new Date(date*1000L);
		String dTime = formatter.format(currentTime);
		return dTime;
	}
}
