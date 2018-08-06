package edu.handong.csee.isel.parsers;

public class Parser {
	public String[] convertStringAsLineList(String oneLine) {
		String[] newStrings = oneLine.split("\n");
		return newStrings;
	}

	public boolean isStartWithPlus(String str) {
		if (str.startsWith("+")) {
			if (str.startsWith("+++"))
				return false;
			return true;
		}
		return false;
	}

	public boolean isStartWithMinus(String str) {
		if (str.startsWith("-")) {
			if (str.startsWith("---"))
				return false;
			return true;
		}
		return false;
	}
	
	public int parseNumOfDiffLine(String inStr) {
		int count = 0;
		String[] newStrings = inStr.split("\n");
		for(String str : newStrings) {
			if(this.isStartWithMinus(str)||this.isStartWithPlus(str)) {
				count ++;
			}
		}
		
		return count;
	}
}
