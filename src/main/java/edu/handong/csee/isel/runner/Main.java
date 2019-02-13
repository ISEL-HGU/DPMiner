package edu.handong.csee.isel.runner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

import edu.handong.csee.isel.bic.BICCollector;
import edu.handong.csee.isel.patch.parser.PatchCollector;
import edu.handong.csee.isel.patch.parser.PatchParseType;

/**
 * -i, URL or URI(github.com, reference file having github URLs, Local
 * Repository) -o, directory of result file. [-r], reference relative to bug
 * commit. [-m], minimum printing of lines. [-x], maximum printing of lines.
 * 
 * If is there '-r', check that commit message have the pattern by reference to
 * '-r' option value. Else, check that commit message have the 'bug' or 'fix'
 * keyword.
 * 
 * @author imseongbin
 */
public class Main {
	String input;
	String resultDirectory = null;
	String reference = null;
	String label = null;
	PatchParseType type;
	int conditionMax = -1;
	int conditionMin = -1;
//	boolean isThread;
	boolean help;
	boolean isBI;

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

				if (isBI) {
					BICCollector collector = new BICCollector(input, resultDirectory, reference, type, conditionMin,
							conditionMax, label);
					collector.collect();
				} else {
					PatchCollector collector = new PatchCollector(input, resultDirectory, reference, type, conditionMin,
							conditionMax, label);
					collector.collect();
				}

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println(e.getMessage());
			}

		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {

			CommandLine cmd = parser.parse(options, args);

			try {

				if (cmd.hasOption("r"))
					type = PatchParseType.Jira;
				else
					type = PatchParseType.GitHub;

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

			input = cmd.getOptionValue("i");
			label = cmd.getOptionValue("l");
			reference = cmd.getOptionValue("r");
			resultDirectory = cmd.getOptionValue("o");
			help = cmd.hasOption("h");
//			isThread = cmd.hasOption("t");
			isBI = cmd.hasOption("b");

		} catch (Exception e) {
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

		options.addOption(Option.builder("r").longOpt("reference")
				.desc("If you have list of bug commit IDs, make a file to have the list, and push the file").hasArg()
				.argName("reference relative to bug").build());

		options.addOption(Option.builder("x").longOpt("max")
				.desc("Set a Max lines of each result patch. Only count '+++' and '---' lines. must used with '-m'")
				.hasArg().argName("Max lines of patch").build());

		options.addOption(Option.builder("m").longOpt("min")
				.desc("Set a Min lines of each result patch. This Option need to be used with 'M' Option(MaxLine).")
				.hasArg().argName("Min lines of patch").build());

		options.addOption(Option.builder("l").longOpt("label").desc("Set a bug label of github").hasArg()
				.argName("Find coincident commit with label").build());

//		options.addOption(Option.builder("t").longOpt("thread")
//				.desc("Using threads in your cpu, you can speed up. Only do well if input is local repository.")
//				.build());

		options.addOption(Option.builder("b").longOpt("bugIntroducingChange")
				.desc("If you want to get bug introducing changes, add this option").build());

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
