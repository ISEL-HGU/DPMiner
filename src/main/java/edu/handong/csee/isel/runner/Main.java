package edu.handong.csee.isel.runner;

import java.util.HashSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import edu.handong.csee.isel.jira.crawler.JiraBugIssueCrawler;
import edu.handong.csee.isel.parser.GitHubParser;
import edu.handong.csee.isel.parser.JiraParser;
import edu.handong.csee.isel.parser.NoParser;
import edu.handong.csee.isel.parser.Parser;
import edu.handong.csee.isel.parser.githubparser.NoIssuePagesException;
import edu.handong.csee.isel.utils.Utils;

/**
 * -i, URL or URI(github.com, reference file having github URLs, Local
 * Repository) -o, directory of result file. [-r], reference relative to bug
 * commit. [-m], minimum printing of lines. [-x], maximum printing of lines.
 * 
 * If is there '-r', check that commit message have the pattern by reference to
 * '-r' option value. Else, check that commit message have the 'bug', 'fix' or
 * resolved keyword.
 * 
 * @author imseongbin
 */
public class Main {
	boolean help;
	Input input;

	public static void main(String[] args) {
		Main bc = new Main();
		bc.run(args);
	}

	public void run(String[] args) {
		Options options = createOptions();

		if (parseOptions(options, args)) {
			if (help) {
				printHelp(options);
				return;
			}

			try {
				HashSet<String> keyHashes = null; // bug commit id
				Parser parser = null;

				if (input.repository == Repository.GitHub) { // No or too small bug issues
					try {
						keyHashes = Utils.parseGithubIssues(input.url, input.label);
						if (keyHashes.size() < 10) { // too small
							input.repository = Repository.No;
						}
					} catch (NoIssuePagesException e) {

						input.repository = Repository.No;
					}
				}

				switch (input.repository) {
				case Jira: {
					parser = new JiraParser(input);
					break;
				}

				case GitHub: {
					parser = new GitHubParser(input, keyHashes);
					break;
				}

				case No: {
					parser = new NoParser(input);
					break;
				}
				}

				if (parser instanceof JiraParser) {
					((JiraParser) parser).parse(input.reference);
				}
				if (parser instanceof GitHubParser) {
					((GitHubParser) parser).parse();
				}

//				System.out.println("Exit!");
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		String url;
		String resultDirectory = null;
		String reference = null;
		String label = null;
		Repository repository;
		int conditionMax = 500;
		int conditionMin = 0;
		boolean isBI;

		try {

			CommandLine cmd = parser.parse(options, args);

			try {

				if (cmd.hasOption("j"))
					repository = Repository.Jira;
				else
					repository = Repository.GitHub;

				if (cmd.hasOption("x") || cmd.hasOption("m")) {
					if (cmd.hasOption("x") && cmd.hasOption("m")) {
						conditionMax = Integer.parseInt(cmd.getOptionValue("x"));
						conditionMin = Integer.parseInt(cmd.getOptionValue("m"));
						if (conditionMin > conditionMax) {
							throw new Exception("Max must be bigger than min!");
						}
					} else {
						throw new Exception("'x' and 'm' Option must be used together!");
					}

				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				printHelp(options);
				return false;
			}

			if (cmd.hasOption("j") ^ cmd.hasOption("k")) {
				System.out.println("'j' options must be used with 'k'");
				throw new Exception();
			} else if (cmd.hasOption("j")) {
				String jiraURL = cmd.getOptionValue("j");
				String projectKey = cmd.getOptionValue("k");
				JiraBugIssueCrawler crawler = new JiraBugIssueCrawler(jiraURL, projectKey);
				reference = crawler.getJiraBugs().getAbsolutePath();
			}

			url = cmd.getOptionValue("i");
			label = cmd.getOptionValue("l");
			resultDirectory = cmd.getOptionValue("o");
			isBI = cmd.hasOption("b");
			help = cmd.hasOption("h");

			input = new Input(url, resultDirectory, reference, label, repository, conditionMin, conditionMax, isBI);
		} catch (Exception e) {
			e.printStackTrace();
			printHelp(options);
			return false;
		}

		return true;
	}

	private Options createOptions() {
		Options options = new Options();

		options.addOption(Option.builder("i").longOpt("input").desc(
				"Three input type: URL or URI(github.com, reference file having github URLs, Local " + "Repository)")
				.hasArg().argName("URI or URL").required().build());

		options.addOption(Option.builder("o").longOpt("result").desc("directory will have result file").hasArg()
				.argName("directory").required().build());

//		options.addOption(Option.builder("r").longOpt("reference")
//				.desc("If you have list of bug commit IDs, make a file to have the list, and push the file").hasArg()
//				.argName("reference relative to bug").build());

		options.addOption(Option.builder("x").longOpt("max").desc(
				"Set a Max lines of each result patch. Only count '+++' and '---' lines. must used with '-m'. (default: 500)")
				.hasArg().argName("Max lines of patch").build());

		options.addOption(Option.builder("m").longOpt("min")
				.desc("Set a Min lines of each result patch. This Option need to be used with 'x' Option. (default: 0)")
				.hasArg().argName("Min lines of patch").build());

		options.addOption(Option.builder("l").longOpt("label").desc("Set a bug label of github").hasArg()
				.argName("Find coincident commit with label").build());

//		options.addOption(Option.builder("t").longOpt("thread")
//				.desc("Using threads in your cpu, you can speed up. Only do well if input is local repository.")
//				.build());

		options.addOption(Option.builder("b").longOpt("bugIntroducingChange")
				.desc("If you want to get bug introducing changes, add this option").build());

		options.addOption(Option.builder("j").longOpt("jira").desc("Jira issues URL (example: issues.apache.org)")
				.hasArg().argName("Jira project URL").build());

		options.addOption(Option.builder("k").longOpt("min").desc(
				"Jira project key. you can get more informations: https://github.com/HGUISEL/BugPatchCollector/issues/18")
				.hasArg().argName("Project Key").build());

		options.addOption(Option.builder("h").longOpt("help").desc("Help").build());

		return options;
	}

	private void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		String header = "Collecting bug-patch program";
		String footer = "\nPlease report issues at https://github.com/HGUISEL/BugPatchCollector/issues";
		formatter.printHelp("BugPatchCollector", header, options, footer, true);
	}

}
