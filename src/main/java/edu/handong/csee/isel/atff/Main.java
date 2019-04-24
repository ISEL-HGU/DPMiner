package edu.handong.csee.isel.atff;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class Main {
	static String csv1 = "/Users/imseongbin/Desktop/files/BIC_okhttp.csv";
	static String csv2 = "/Users/imseongbin/Desktop/files/MET_okhttp.csv";
	static String new_csv = "/Users/imseongbin/Desktop/NEW_okhttp.csv";

	public static void main(String[] args) throws IOException {

		// 1. crawl CSV 2 -> BIC
		HashSet<String> set = getRecordBy(csv2, 0);

		// 2. crawl CSV 1 -> Metrics
		Reader in = new FileReader(csv1);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
		int col_size;
		ArrayList<String> headers = new ArrayList<String>();

		for (CSVRecord record : records) {
			col_size = record.size();
			for (int i = 0; i < col_size; i++) {
				headers.add(record.get(i));
				System.out.println(record.get(i));
			}
			headers.add("is_buggy");
			break;
		}

		boolean first = true;

		// 3. make csv
		try (BufferedWriter writer = java.nio.file.Files.newBufferedWriter(Paths.get(new_csv));

				CSVPrinter csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withHeader(headers.toArray(new String[headers.size()])));) {
			for (CSVRecord record : records) {
				if (first) {
					first = false;
					continue;
				}
				String id = record.get(0); // commit_id
				String buggy = "";
				if (set.contains(id)) {
					buggy = "buggy";
				} else {
					buggy = "clean";
				}
				ArrayList<String> line = new ArrayList<String>();
				for (String str : record) {
					line.add(str);
				}
				line.add(buggy);
				csvPrinter.printRecord(line);
			}
			csvPrinter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static HashSet<String> getRecordBy(String file, int col) throws IOException {
		HashSet<String> set = new HashSet<String>();

		Reader in = new FileReader(file);
		Iterable<CSVRecord> records = CSVFormat.RFC4180.parse(in);
		for (CSVRecord record : records) {
			String column = record.get(col);
			set.add(column);
		}

		return set;
	}

}
