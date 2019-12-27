package csvParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
//./BugPatchCollector -i https://github.com/apache/freemarker -j issues.apache.org -k FREEMARKER -b -o /data/BIC 
	
	public static void main(String[] args) throws FileNotFoundException {
		File file = new File("/Users/imseongbin/Desktop/info.csv");
		
		
		Scanner in = new Scanner(new BufferedReader(new FileReader(file)));

		ArrayList<Info> infos = new ArrayList<>();
		
		while(in.hasNext()) {
			String line = in.nextLine();
			String[] items = line.split(",");
			
			if(items.length == 3 && !items[1].toUpperCase().equals("X") && !items[2].toUpperCase().equals("X"))
				infos.add(new Info(items[0],items[1],items[2]));
		}
		
		for(Info info : infos) {
			
			String line = "./BugPatchCollector -i " + info.input + " -j issues.apache.org -k " + info.key + " -b -o /data/BIC";
			
			System.out.println(line);
		}
		
	}

	public static class Info {
		
		Info(String f, String s, String t) {
			first = f;
			key = s;
			input = t;
		}
		
		public String first;
		public String key;
		public String input;
		
	}
}
