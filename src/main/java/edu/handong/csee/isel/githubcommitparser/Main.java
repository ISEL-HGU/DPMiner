package edu.handong.csee.isel.githubcommitparser;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

public class Main {
	static String address = null;
	static String output = null;
	static String file = null;
	static String printNumber = null;
	boolean verbose;
	boolean help;

	public static void main(String[] args) {
		Main my = new Main();
		my.run(args);
	}

	private void run(String[] args) {
		Options options = createOptions();

		if (parseOptions(options, args)) {
			if (help) {
				printHelp(options);
				return;
			}

			FileReader fr = new FileReader();

			fr.githubAddress.add(address);

			if (address == null && file == null) {
				printHelp(options);
				return;
			}

			if (file != null) {
				fr.githubAddress.clear();
				fr.readGithubAddressFile(file);
			}

			IssueLinkParser iss = new IssueLinkParser();
			CommitParser co = new CommitParser();

			for (int i = 0; i < fr.githubAddress.size(); i++) {
				String oneAddress = fr.githubAddress.get(i);

				try {
					iss.parseIssueAddress(oneAddress);
					co.parseCommitAddress(oneAddress);
					co.parseAndPrintCommiContents(oneAddress, output, printNumber); // this class need to change from GithubPatchCollector.java
					
					iss.issueAddress.clear();
					//co.commitAddress.clear();
					//co.commitLine.clear();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			System.out.println("You provided \"" + address + "\" as the value of the option a");

			if (verbose) {
				System.out
						.println("Your program is terminated. (This message is shown because you turned on -v option!");
			}
		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {
			CommandLine cmd = parser.parse(options, args);

			address = cmd.getOptionValue("a");
			file = cmd.getOptionValue("f");
			output = cmd.getOptionValue("o");
			printNumber = cmd.getOptionValue("n");
			verbose = cmd.hasOption("v");
			help = cmd.hasOption("h");

		} catch (Exception e) {
			printHelp(options);
			return false;
		}
		return true;
	}

	private void printHelp(Options options) {
		// automatically generate the help statement
		HelpFormatter formatter = new HelpFormatter();
		String header = "bug issue parser";
		String footer = ""; // "\nPlease report issues at
							// https://github.com/lamb0711/WebPageCrawler/issues";
		formatter.printHelp("bug issue parser", header, options, footer, true);
	}

	private Options createOptions() {
		Options options = new Options();

		options.addOption(Option.builder("a").longOpt("address").desc("Set a address of github").hasArg()
				.argName("Path name to project address").build());

		options.addOption(Option.builder("f").longOpt("file").desc("Set a address of file").hasArg()
				.argName("Path name to file").build());

		options.addOption(Option.builder("o").longOpt("output").desc("Set a output route of csv file").hasArg()
				.argName("Path name to output directory").required().build());

		options.addOption(Option.builder("n").longOpt("printNumber").desc("Set a number of output pathces line")
				.hasArg().argName("patches line number to display").build());

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

}
