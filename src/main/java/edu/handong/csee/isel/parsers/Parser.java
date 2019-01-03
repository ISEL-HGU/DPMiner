package edu.handong.csee.isel.parsers;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

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
