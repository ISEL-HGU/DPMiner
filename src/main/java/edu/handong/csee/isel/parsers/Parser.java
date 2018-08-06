package edu.handong.csee.isel.parsers;

public class Parser {
	String[] convertStringAsLineList(String oneLine) {
		String[] newStrings = oneLine.split("\n");
		return newStrings;
	}

	boolean isStartWithPlus(String str) {
		if (str.startsWith("+")) {
			if (str.startsWith("+++"))
				return false;
			return true;
		}
		return false;
	}

	boolean isStartWithMinus(String str) {
		if (str.startsWith("-")) {
			if (str.startsWith("---"))
				return false;
			return true;
		}
		return false;
	}
}
