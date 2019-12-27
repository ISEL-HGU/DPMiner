package newpackage.data;

public class Input {

	public ReferenceType referecneType;

	public Mode mode;

	public String outPath;

	public String gitRemoteURI;

	public static enum ReferenceType {
		JIRA, GITHUB, KEYWORD
	}

	public static enum Mode {
		PATCH, BIC, METRIC
	}

}
