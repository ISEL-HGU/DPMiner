package edu.handong.csee.isel.patch;

import java.util.Date;

// projectName
// Fix-Commit
// Fix-ShortMessage
// Fix-Date
// Fix-Author
// Patch
public class Patch {
	public String project;
	public String fix_commit;
	public String fix_shortMessage;
	public Date fix_date;
	public String fix_author;
	public String patch;

	public Patch(String project, String fix_commit, String fix_shortMessage, Date fix_date, String fix_author,
			String patch) {
		super();
		this.project = project;
		this.fix_commit = fix_commit;
		this.fix_shortMessage = fix_shortMessage;
		this.fix_date = fix_date;
		this.fix_author = fix_author;
		this.patch = patch;
	}

}
