package edu.handong.csee.isel.patch;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Main {
	String gitRepositoryPath;
	String resultDirectory;
	String csvFile;
	int conditionMax;
	int conditionMin;
	boolean help;

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

			/* start main */

			/* end main */

		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {

			CommandLine cmd = parser.parse(options, args);

			csvFile = cmd.getOptionValue("c");
			gitRepositoryPath = cmd.getOptionValue("g");
			resultDirectory = cmd.getOptionValue("r");

			if (cmd.hasOption("M") || cmd.hasOption("m")) {
				try {
					if (cmd.hasOption("M") && cmd.hasOption("m")) {
						conditionMax = Integer.parseInt(cmd.getOptionValue("M"));
						conditionMin = Integer.parseInt(cmd.getOptionValue("m"));
						if (conditionMin > conditionMax) {
							throw new Exception("Max must be bigger than min!");
						}

					} else {
						throw new Exception("'M' and 'm' Option must be together!");
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
					printHelp(options);
					return false;
				}
			}

			help = cmd.hasOption("h");

		} catch (Exception e) {
			printHelp(options);
			return false;
		}

		return true;
	}

	// Definition Stage
	private Options createOptions() {
		Options options = new Options();

		// add options by using OptionBuilder
		options.addOption(Option.builder("c").longOpt("csv").desc("Set a path of CSV reference relative to commits")
				.hasArg().argName("CSV File").required().build());

		options.addOption(Option.builder("g").longOpt("gitRepositoryPath").desc("Set a path of a git-repository")
				.hasArg().argName("Git-repository path name").required().build());

		options.addOption(Option.builder("r").longOpt("resultDirectory")
				.desc("Set a directory to have result files(all patch files and a summary file).").hasArg()
				.argName("Path name to construct result files").required().build());

		options.addOption(Option.builder("M").longOpt("Maxline")
				.desc("Set a Max lines of each result patch. Only count '+' and '-' lines.").hasArg()
				.argName("Max lines of patch").build());

		options.addOption(Option.builder("m").longOpt("Minline")
				.desc("Set a Min lines of each result patch. This Option need to be used with 'M' Option(MaxLine).")
				.hasArg().argName("Min lines of patch").build());

		// add options by using OptionBuilder
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
