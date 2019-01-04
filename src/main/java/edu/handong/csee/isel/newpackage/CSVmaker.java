package edu.handong.csee.isel.newpackage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class CSVmaker {
	File file;
	CSVPrinter printer;
	public CSVmaker(File file,String[] headers) throws IOException {
		this.file = file;

		BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getAbsolutePath()));
		printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));
	}
	
	// {"Project","fix-commit","fix-shortMessage","fix-date","fix-author","patch"}
	
	public void write(Data data) throws IOException {
		printer.printRecord(data.project, data.fix_commit, data.fix_shortMessage, convertCalendar(data.fix_date), data.fix_author, data.patch);
		printer.flush();
	}
	
	public String convertCalendar(Date date) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", Locale.KOREA);
		String dTime = formatter.format(date);
		return dTime;
	}
}
