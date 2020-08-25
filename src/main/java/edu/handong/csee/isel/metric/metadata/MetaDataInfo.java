package edu.handong.csee.isel.metric.metadata;

public class MetaDataInfo {
	int numOfBIC;        //ok
	String commitAuthor; //ok
	int numOfUniqueCommitToTheModifyFiles; //ok
	int numOfModifyLines; //ok
	int numOfDeleteLines; //ok
	int numOfAddLines; //ok
	int distributionOfModifiedLines; //ok
	int sumOfSourceRevision; //ok
	int sumOfDeveloper; //ok
	String commitHour; //ok
	String commitDay; //ok
	int fileAge; //ok
	int isBugCommit; //ok
	int timeBetweenLastAndCurrentCommitDate; //ok
	int numOfSubsystems; //ok
	int numOfDirectories;//ok
	int numOfFiles;//ok
	int developerExperience;//ok
	float recentDeveloperExperience;//ok
	int LinesOfCodeBeforeTheChange;//ok
	String commitTime;
	double entropy;

	public MetaDataInfo() {
		this.numOfBIC = 0;
		this.commitAuthor = null;
		this.commitHour = null;
		this.numOfModifyLines = 0;
		this.numOfDeleteLines = 0;
		this.numOfAddLines = 0;
		this.distributionOfModifiedLines = 0;
		this.sumOfSourceRevision = 0;
		this.sumOfDeveloper = 0;
		this.isBugCommit = 0;
		this.timeBetweenLastAndCurrentCommitDate = 0;
		this.numOfSubsystems = 0;
		this.numOfDirectories = 0;
		this.numOfFiles = 0;
		this.numOfUniqueCommitToTheModifyFiles = 0;
		this.developerExperience = 0;
		this.recentDeveloperExperience = 0;
		this.LinesOfCodeBeforeTheChange = 0;
		this.entropy = 0.0;
		this.commitTime = null;
	}

	public int getNumOfBIC() {
		return numOfBIC;
	}
	public void setNumOfBIC(int numOfBIC) {
		this.numOfBIC = numOfBIC;
	}
	public String getCommitAuthor() {
		return commitAuthor;
	}
	public void setCommitAuthor(String commitAuthor) {
		this.commitAuthor = commitAuthor;
	}
	public int getNumOfModifyLines() {
		return numOfModifyLines;
	}
	public void setNumOfModifyLines(int numOfModifyLines) {
		this.numOfModifyLines = numOfModifyLines;
	}
	public int getNumOfDeleteLines() {
		return numOfDeleteLines;
	}
	public void setNumOfDeleteLines(int numOfDeleteLines) {
		this.numOfDeleteLines = numOfDeleteLines;
	}
	public int getNumOfAddLines() {
		return numOfAddLines;
	}
	public void setNumOfAddLines(int numOfAddLines) {
		this.numOfAddLines = numOfAddLines;
	}
	public int getDistributionOfModifiedLines() {
		return distributionOfModifiedLines;
	}
	public void setDistributionOfModifiedLines(int distributionOfModifiedLines) {
		this.distributionOfModifiedLines = distributionOfModifiedLines;
	}

	public int getSumOfSourceRevision() {
		return sumOfSourceRevision;
	}

	public void setSumOfSourceRevision(int sumOfSourceRevision) {
		this.sumOfSourceRevision = sumOfSourceRevision;
	}

	public int getSumOfDeveloper() {
		return sumOfDeveloper;
	}

	public void setSumOfDeveloper(int sumOfDeveloper) {
		this.sumOfDeveloper = sumOfDeveloper;
	}

	public String getCommitDay() {
		return commitDay;
	}

	public void setCommitDay(String commitDay) {
		this.commitDay = commitDay;
	}

	public String getCommitHour() {
		return commitHour;
	}

	public void setCommitHour(String commitHour) {
		this.commitHour = commitHour;
	}

	public int getFileAge() {
		return fileAge;
	}

	public void setFileAge(int fileAge) {
		this.fileAge = fileAge;
	}

	public int getIsBugCommit() {
		return isBugCommit;
	}

	public void setIsBugCommit(int isBugCommit) {
		this.isBugCommit = isBugCommit;
	}

	public int getTimeBetweenLastAndCurrentCommitDate() {
		return timeBetweenLastAndCurrentCommitDate;
	}

	public void setTimeBetweenLastAndCurrentCommitDate(int timeBetweenLastAndCurrentCommitDate) {
		this.timeBetweenLastAndCurrentCommitDate = timeBetweenLastAndCurrentCommitDate;
	}

	public int getNumOfSubsystems() {
		return numOfSubsystems;
	}

	public void setNumOfSubsystems(int numOfSubsystems) {
		this.numOfSubsystems = numOfSubsystems;
	}

	public int getNumOfDirectories() {
		return numOfDirectories;
	}
	public void setNumOfDirectories(int numOfDirectories) {
		this.numOfDirectories = numOfDirectories;
	}

	public int getNumOfFiles() {
		return numOfFiles;
	}

	public void setNumOfFiles(int numOfFiles) {
		this.numOfFiles = numOfFiles;
	}

	public int getNumOfUniqueCommitToTheModifyFiles() {
		return numOfUniqueCommitToTheModifyFiles;
	}

	public void setNumOfUniqueCommitToTheModifyFiles(int numOfUniqueCommitToTheModifyFiles) {
		this.numOfUniqueCommitToTheModifyFiles = numOfUniqueCommitToTheModifyFiles;
	}
	
	public int getDeveloperExperience() {
		return developerExperience;
	}

	public void setDeveloperExperience(int developerExperience) {
		this.developerExperience = developerExperience;
	}

	public float getRecentDeveloperExperience() {
		return recentDeveloperExperience;
	}

	public void setRecentDeveloperExperience(float recentDeveloperExperience) {
		this.recentDeveloperExperience = recentDeveloperExperience;
	}
	
	public int getLinesOfCodeBeforeTheChange() {
		return LinesOfCodeBeforeTheChange;
	}

	public void setLinesOfCodeBeforeTheChange(int linesOfCodeBeforeTheChange) {
		LinesOfCodeBeforeTheChange = linesOfCodeBeforeTheChange;
	}

	public String getCommitTime() {
		return commitTime;
	}

	public void setCommitTime(String commitTime) {
		this.commitTime = commitTime;
	}

	public double getEntropy() {
		return entropy;
	}

	public void setEntropy(double entropy) {
		this.entropy = entropy;
	}
	
	
}
