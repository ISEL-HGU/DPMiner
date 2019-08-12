package edu.handong.csee.isel.jira.crawler;

import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class JBICDemonstrator {
	private String domain;
	private String projectKey;
	private boolean help;
	private boolean pathMode;
	private String path;

	public static void main(String[] args) {
		JBICDemonstrator jbicDemonstrator = new JBICDemonstrator();
		try {
			jbicDemonstrator.run(args);
		} catch (ParseException e) {
			System.err.println("\nParsing failed.\n\tReason - " + e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidDomainException e) {
			e.printStackTrace();
		} catch (InvalidProjectKeyException e) {
			e.printStackTrace();
		}
	}

	// Definition stage
	// TODO verbose mode 추가
	private Options createOptions() {
		Options options = new Options();

		options.addOption(Option.builder("d").hasArg().required().longOpt("domain")
				.desc("Set domain in URL (ex. issues.apache.org)").build());

		options.addOption(
				Option.builder("p").hasArg().required().longOpt("Set project key").desc("Set project key").build());

		options.addOption(Option.builder("h").longOpt("help").desc("Help").build());

		options.addOption(Option.builder("D").hasArg().longOpt("path").desc("Set a path to store csv files").build());

		return options;
	}

	// TODO header에 description 추가
	private void printHelp(Options options) {
		String header = "...Description about JiraBugIssueCrawler...\n\n";
		String footer = "\nPlease report issues at https://github.com/HGUISEL/JiraCrawler/issues\n\n";

		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("JiraBugIssueCrawler", header, options, footer, true);
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);

			domain = cmd.getOptionValue('d');
			projectKey = cmd.getOptionValue('p');
			help = cmd.hasOption('h');
			pathMode = cmd.hasOption('D');
			path = cmd.getOptionValue('D');

		} catch (ParseException e) {
			printHelp(options);
			return false;
		}

		return true;
	}

	private void run(String[] args)
			throws ParseException, IOException, InvalidDomainException, InvalidProjectKeyException {
		Options options = createOptions();

		if (parseOptions(options, args)) {
			if (help) {
				printHelp(options);
				return;
			}

			if (pathMode) {
				System.out.println("\n\tYou provided \'" + domain + "\' as the value of the option d");
				System.out.println("\tYou provided \'" + projectKey + "\' as the value of the option p");
				System.out.println("\tYou provided \'" + path + "\' as the value of the option D");

				JiraBugIssueCrawler jiraBugIssueCrawler = new JiraBugIssueCrawler(domain, projectKey, path);
//				jiraBugIssueCrawler.run();
			} else {
				System.out.println("\n\tYou provided \'" + domain + "\' as the value of the option d");
				System.out.println("\tYou provided \'" + projectKey + "\' as the value of the option p");

				JiraBugIssueCrawler jiraBugIssueCrawler = new JiraBugIssueCrawler(domain, projectKey);
//				jiraBugIssueCrawler.run();
			}
		}
	}
}
