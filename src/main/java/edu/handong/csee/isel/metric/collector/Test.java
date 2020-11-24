package edu.handong.csee.isel.metric.collector;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

public class Test {
	final static String patternStr = "\\{.+\\,\\d+\\s(.+)\\}"; 
	public static void main(String[] args) throws IOException {
		File f1, f2;

		f1 = new File("/Users/imseongbin/Desktop/reference/lottie-android-merged-bow/lottie-android.arff");
		f2 = new File("/Users/imseongbin/Desktop/lottie-android.arff");

		String content1 = FileUtils.readFileToString(f1, "UTF-8");
		String content2 = FileUtils.readFileToString(f2, "UTF-8");

		ArrayList<String> list1 = getDataLinesFrom(content1);
		ArrayList<String> list2 = getDataLinesFrom(content2);
		
		System.out.println("size1: " + list1.size());
		System.out.println("size2: " + list2.size());
		
		ArrayList<String> pathList1 = new ArrayList<>();
		ArrayList<String> pathList2 = new ArrayList<>();
		
		pathList1 = getFileOrder();
		
		Pattern p = Pattern.compile(patternStr,Pattern.CASE_INSENSITIVE);
		for(String line : list2) {
			Matcher m = p.matcher(line);
			if(m.find()) {
				String path = m.group(1);
				pathList2.add(path);
			} else {
				System.out.println("ERROR!");
				return;
			}
		}
		
		System.out.println("path 1 List size: " + pathList1.size());
		System.out.println("path 2 List size: " + pathList2.size());
		
		for(int i = 0; i < Math.max(pathList1.size(), pathList2.size()); i++) {
			String path1, path2;
			try {
				path1 = pathList1.get(i);
			} catch (Exception e) {
				path1 = null;
			}
			try {
				path2 = pathList2.get(i);
			} catch (Exception e) {
				path2 = null;
			}
			
			if(path1 != null) {
				int index = pathList2.indexOf(path1);
				if(index == -1) {
					System.out.println("b does not contain " + path1);
				}
			}
			if(path2 != null) {
				int index = pathList1.indexOf(path2);
				if(index == -1) {
					System.out.println("a does not contain " + path2);
				}
			}
		}
		
	}
	
	public static ArrayList<String> getFileOrder() {

		ArrayList<String> fileOrder = new ArrayList<>();

		File directory = new File("/Users/imseongbin/Desktop/reference/lottie-android-merged-bow");

		File cleanDirectory = null;
		File buggyDirectory = null;
		
		for(File f : directory.listFiles()) {
			if(f.isDirectory() && cleanDirectory == null) {
				cleanDirectory = f;
			} else if(f.isDirectory()){
				buggyDirectory = f;
			}
		}
		

		for (File f : cleanDirectory.listFiles()) {
			fileOrder.add(f.getName());
		}
		for (File f : buggyDirectory.listFiles()) {
			fileOrder.add(f.getName());
		}

		return fileOrder;
	}

	private static ArrayList<String> getDataLinesFrom(String content) {
		ArrayList<String> dataLineList = new ArrayList<>();
		String[] lines = content.split("\n");

		boolean dataPart = false;
		for (String line : lines) {
			if (dataPart) {
				dataLineList.add(line);

			} else if (line.startsWith("@data")) {

				dataPart = true;
			}

		}
		return dataLineList;
	}
}
