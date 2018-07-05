package edu.handong.csee.isel.java.patch;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class BugPatchCollector {
	String gitRepositoryPath;
	String resultDirectory;
	boolean verbose;
	boolean help;

	public static void main(String[] args) {
		BugPatchCollector bc = new BugPatchCollector();
		bc.run(args);
	}

	public void run(String[] args) {
		Options options = createOptions();

		if (parseOptions(options, args)) {
			if (help) {
				printHelp(options);
				return;
			}
			
			/* Start Main */
			
			
			
			
			
			/* until */
			
			if(verbose) {
				System.out.println("******The program help making Patch from all commit");
				System.out.println("******input git-repository-path ");
				System.out.println("******input result-File-Name");
				System.out.println("****************************");
				System.out.println("******SB Made it******");
				System.out.println("****************************");
			}
		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {

			CommandLine cmd = parser.parse(options, args);

			gitRepositoryPath = cmd.getOptionValue("g");
			resultDirectory = cmd.getOptionValue("r");
			verbose = cmd.hasOption("v");
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
		options.addOption(Option.builder("g").longOpt("gitRepositoryPath").desc("Set a path of a git-repository")
				.hasArg().argName("Git-repository path name").required().build());

		options.addOption(
				Option.builder("r").longOpt("resultDirectory").desc("Set a directory to have result files(all patch files and a summary file).").hasArg()
						.argName("Path name to construct result files").required().build());

		// add options by using OptionBuilder
		options.addOption(Option.builder("v").longOpt("verbose").desc("Display detailed messages!")
				// .hasArg() // this option is intended not to have an option value but just an
				// option
				.argName("verbose option")
				// .required() // this is an optional option. So disabled required().
				.build());

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
