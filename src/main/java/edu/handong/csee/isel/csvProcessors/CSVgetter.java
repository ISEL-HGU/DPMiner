package edu.handong.csee.isel.csvProcessors;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

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
		for (CSVRecord record : records) {
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
}
