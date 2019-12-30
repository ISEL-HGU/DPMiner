package edu.handong.csee.isel.data.processor;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.csv.BICInfo;
import edu.handong.csee.isel.data.csv.MetricInfo;
import edu.handong.csee.isel.data.csv.PatchInfo;

public class CSVMaker {
	public CSVPrinter printer = null;
	public String path;

	public void setPath(String path) {
		this.path = path;

	}

	public void print(PatchInfo patchInfo) throws IOException {
		printer.printRecord(patchInfo.project, patchInfo.commitName, patchInfo.commitMessage, patchInfo.date,
				patchInfo.author, patchInfo.patch);
	}

	public void print(List<CSVInfo> csvInfo) throws IOException {
		String[] headers = csvInfo.get(0).getHeaders();
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
		printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));

		if (csvInfo instanceof PatchInfo) {
			for (CSVInfo info : csvInfo) {
				PatchInfo patchInfo = (PatchInfo) info;
				print(patchInfo);
			}
		}
		if (csvInfo instanceof BICInfo) {
			for (CSVInfo info : csvInfo) {
				BICInfo bicInfo = (BICInfo) info;
				print(bicInfo);
			}
		}
		if (csvInfo instanceof MetricInfo) {

		}

	}

	private void print(BICInfo info) throws IOException {
		printer.printRecord(info.getBISha1(), info.getBIPath(), info.getPath(), info.getFixSha1(), info.getBIDate(),
				info.getFixDate(), info.getLineNum(), info.getLineNumInPrevFixRev(), info.getIsAddedLine(),
				info.getLine());

	}

}
