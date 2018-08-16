package edu.handong.csee.isel.githubcommitparser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class FileReader {
	public ArrayList<String> githubAddress = new ArrayList<String>();
	
	void readGithubAddressFile(String fileAddress) {
		
		Scanner inputStream = null;
		
		try {
			inputStream = new Scanner(new File(fileAddress));
		}  catch (FileNotFoundException e) {
			System.out.println ("Error opening the file ");
		}
		
		while (inputStream.hasNextLine ()) {
			String line = inputStream.nextLine ();
			githubAddress.add(line);
		}
		
		for(int i=0; i<githubAddress.size(); i++) System.out.println(githubAddress.get(i));
		
		inputStream.close ();
	}

}
