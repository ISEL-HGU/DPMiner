package edu.handong.csee.isel.data.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import edu.handong.csee.isel.data.CSVInfo;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.csv.BICInfo;
import edu.handong.csee.isel.data.csv.MetricInfo;
import edu.handong.csee.isel.data.csv.PatchInfo;

public class CSVMaker {
	public CSVPrinter printer = null;
	public String path;
	private Type type = null;

	private static enum Type {  //해당 class는 파라미터로 들어온 csvInfo의 첫번째 값의 속성을 instanceof 으로 알아낸 뒤 알아서 파일을 만든다.. 그래서 고칠부분은 없다.
		PATCH, BIC, METRIC 
	}

	public void setPath() {

		String outPath = Input.outPath;

		if (this.type != null) {
			switch (type) {

			case PATCH:
				outPath += File.separator + "PATCH_";
				break;

			case BIC:
				outPath += File.separator + "BIC_";
				break;

			case METRIC:
				outPath += File.separator + "METRIC_";
				break;
			}
		}
		outPath += Input.projectName + ".csv";

		this.path = outPath;

	}

	public void print(PatchInfo patchInfo) throws IOException {
		printer.printRecord(patchInfo.project, patchInfo.commitName, patchInfo.commitMessage, patchInfo.date,
				patchInfo.author, patchInfo.patch);
	}

	public void print(List<CSVInfo> csvInfo) throws IOException {

		if (csvInfo.size() < 1) {
			System.err.println("WARNNING: There is no BFC");
			System.exit(1);
		}

		String[] headers = csvInfo.get(0).getHeaders();
		BufferedWriter writer = Files.newBufferedWriter(Paths.get(path));
		printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headers));

		switch (type) {

		case PATCH:

			for (CSVInfo info : csvInfo) {
				PatchInfo patchInfo = (PatchInfo) info;
				print(patchInfo);
			}
			break;

		case BIC:

			for (CSVInfo info : csvInfo) {
				BICInfo bicInfo = (BICInfo) info;
				print(bicInfo);
			}
			break;

		case METRIC:
			// TODO:
			break;
		}
		
		printer.close();
	}

	private void print(BICInfo info) throws IOException {
		printer.printRecord(info.getBISha1(), info.getBIPath(), info.getPath(), info.getFixSha1(), info.getBIDate(),
				info.getFixDate(), info.getLineNum(), info.getLineNumInPrevFixRev(), info.getIsAddedLine(),
				info.getLine());

	}

	public void setDataType(List<CSVInfo> csvInfoLst) {
		CSVInfo csvInfoFirst = csvInfoLst.get(0);
		if (csvInfoFirst instanceof PatchInfo) {
//			System.out.println("CSVMAER path");
			this.type = Type.PATCH;
		}
		if (csvInfoFirst instanceof BICInfo) {
			this.type = Type.BIC;
//			System.out.println("CSVMAER BIC");
		}
		if (csvInfoFirst instanceof MetricInfo) {
			this.type = Type.METRIC;
//			System.out.println("CSVMAER MATRIC");
		}
	}

}
