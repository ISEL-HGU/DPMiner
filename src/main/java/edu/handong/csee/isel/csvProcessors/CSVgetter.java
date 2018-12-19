package edu.handong.csee.isel.csvProcessors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import edu.handong.csee.isel.patch.CommitStatus;

public class CSVgetter {
	Reader reader;

	public CSVgetter(String CSVpath) throws FileNotFoundException {
		reader = new FileReader(CSVpath);
	}
	
	public void setCSVpath (String CSVpath) throws FileNotFoundException{
		reader = new FileReader(CSVpath);
	}
	
	
	/**
	 * if Column not exist, skip it! 
	 */
	public ArrayList<String> getColumn(int n) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
//		boolean first = true;
		for (CSVRecord record : records) {
//			if(first) {
//				first = false;
//				continue;
//			}
			list.add(record.get(n));
		}
		return list;
	}

	public ArrayList<String> getColum(int n, int maxLow) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(reader);
		
		int i = 0;
		for (CSVRecord record : records) {
			if (maxLow < i)
				break;
			list.add(record.get(n));
			i++;
		}
		return list;
	}
	
	/* reference parsing */
	public static void main(String[] args) throws IOException {
		File f = new File("/Users/imseongbin/Desktop/JIRA_brooklyn.csv");
		CSVgetter getter = new CSVgetter(f.getAbsolutePath());
		
		ArrayList<String> keys = getter.getColumn(1);
		
//		for(String key: keys)
//			System.out.println(key);
		
		File newFile = new File("/Users/imseongbin/Desktop/brooklyn_ref.csv");
		
		File folder = newFile.getParentFile();
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(newFile.getAbsolutePath()));
				CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT);) {
			for (String key  : keys) {
				csvPrinter.printRecord(key);
			}
			csvPrinter.flush();
		}
		
	}
}
