package newpackage.data;

public class Input {

	public static ReferenceType referecneType;

	public static Mode mode;

	public static enum ReferenceType {
		JIRA, GITHUB, KEYWORD
	}

	public static enum Mode {
		PATCH, BIC, METRIC
	}

}
