package edu.handong.csee.isel.data.processor.input.converter;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.processor.input.InputConverter;

public class CLIConverter implements InputConverter {

	@Override
	public Input getInputFrom(String[] args) {
		CommandLineParser parser = new DefaultParser();
		Options options = createOptions();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			printHelp(options);
			System.exit(1);
		}

		// CLI exception handling
		if (!isValid(cmd, options)) {
			printHelp(options);
			System.exit(1);
		}

		Input input = new Input();
		int max;
		int min;
		if (cmd.hasOption("x") || cmd.hasOption("m")) {
			max = Integer.parseInt(cmd.getOptionValue("x"));
			min = Integer.parseInt(cmd.getOptionValue("m"));
		} else {
			max = 500;
			min = 0;
		}
		input.maxSize = max;
		input.minSize = min;

		input.gitURL = cmd.getOptionValue("i");
		if (input.gitURL.endsWith("/")) {
			input.gitURL = input.gitURL.substring(0, input.gitURL.length() - 1);
		}
		input.gitRemoteURI = input.gitURL + ".git";
		input.jiraProjectKey = cmd.getOptionValue("k");
		input.jiraURL = cmd.getOptionValue("j");
		input.projectName = getProjectName(input.gitRemoteURI);
		input.label = cmd.getOptionValue("l");
		input.BICpath = cmd.getOptionValue("c");
		input.startDate = cmd.getOptionValue("s");
		input.endDate = cmd.getOptionValue("e");
		
		int inputPercent;
		if(!cmd.hasOption("p")) {
			inputPercent = 5; //default
		}else {
			inputPercent = Integer.parseInt(cmd.getOptionValue("p"));
			if(inputPercent > 1 && inputPercent < 10) {
				input.percent = Integer.parseInt(cmd.getOptionValue("p"));
			}else {
				inputPercent = 5;
			}
		}
		input.percent = inputPercent;
		
		if(cmd.hasOption("a")) {
			input.allGitLog = true;
		}else {
			input.allGitLog = false;
		}

		input.outPath = cmd.getOptionValue("o");
		if (input.outPath.endsWith(File.separator)) {
			input.outPath = input.outPath.substring(0, input.outPath.length() - 1);
		}

		if (cmd.hasOption("g")) {
			input.referecneType = Input.ReferenceType.GITHUB;
		} else if (cmd.hasOption("j")) {
			input.referecneType = Input.ReferenceType.JIRA;
		} else if (cmd.hasOption("c")) {
			input.referecneType = Input.ReferenceType.BICCSV;
		} else {
			input.referecneType = Input.ReferenceType.KEYWORD;
		}

		if (cmd.hasOption("b")) {
			input.mode = Input.Mode.BIC;
		} else if (cmd.hasOption("t")) {
			input.mode = Input.Mode.METRIC;
		} else if(cmd.hasOption("d")){
			input.mode = Input.Mode.DEVELOPERMETRIC;
		} else {
			input.mode = Input.Mode.PATCH;
		} 

		return input;
	}

	private boolean isValid(CommandLine cmd, Options options) {
		try {
			if (cmd.hasOption("j") ^ cmd.hasOption("k")) {
				String eMessage = "'j' options must be used with 'k'";
				throw new Exception(eMessage);
			}
			if (cmd.hasOption("x") ^ cmd.hasOption("m")) {
				String eMessage = "'x' and 'm' Option must be used together!";
				throw new Exception(eMessage);
			}
			if (cmd.hasOption("x")) {
				int max = Integer.parseInt(cmd.getOptionValue("x"));
				int min = Integer.parseInt(cmd.getOptionValue("m"));

				if (max < min) {
					String eMessage = "Max must be bigger than min!";
					throw new Exception(eMessage);
				}
			}
			
			if (cmd.hasOption("t") && !cmd.hasOption("c")) {
				String eMessage = "Extracting Metrics requires BIC csv. get BIC first";
				throw new Exception(eMessage);
			}
			
			if (cmd.hasOption("d") && !cmd.hasOption("c")) {
				String eMessage = "Extracting Metrics requires BIC csv. get BIC first";
				throw new Exception(eMessage);
			}
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			printHelp(options);
			return false;
		}
		return true;
	}

	private Options createOptions() {
		Options options = new Options();

		options.addOption(
				Option.builder("i").longOpt("input").desc("input type: URL(https://github.com/user/project_name")
						.hasArg().argName("URI or URL").required().build());

		options.addOption(Option.builder("o").longOpt("result").desc("directory will have result file").hasArg()
				.argName("directory").required().build());

		options.addOption(Option.builder("x").longOpt("max").desc(
				"Set a Max lines of each result patch. Only count '+++' and '---' lines. must used with '-m'. (default: 500)")
				.hasArg().argName("Max lines of patch").build());

		options.addOption(Option.builder("m").longOpt("min")
				.desc("Set a Min lines of each result patch. This Option need to be used with 'x' Option. (default: 0)")
				.hasArg().argName("Min lines of patch").build());

		options.addOption(Option.builder("l").longOpt("label").desc("Set a bug label of github (default: 'bug')")
				.hasArg().argName("Find coincident commit with label").build());

		options.addOption(Option.builder("b").longOpt("bugIntroducingChange")
				.desc("If you want to get bug introducing changes, add this option").build());

		options.addOption(Option.builder("g").longOpt("github")
				.desc("When searching fix-commit, use GitHub-issues with label name").build());

		options.addOption(Option.builder("t").longOpt("metric").desc("collect metrics from all changes").build());

		options.addOption(Option.builder("j").longOpt("jiraURL").desc("Jira issues URL (example: issues.apache.org)")
				.hasArg().argName("Jira project URL").build());

		options.addOption(Option.builder("k").longOpt("jiraProject").desc(
				"Jira project key. you can get more informations: https://github.com/HGUISEL/BugPatchCollector/issues/18")
				.hasArg().argName("Project Key").build());
		
		options.addOption(Option.builder("c").longOpt("BugIntroducingChange csv file path").desc("Path of csv file")
				.hasArg().argName("BIC csv file path").build());

		options.addOption(Option.builder("s").longOpt("startdate")
				.desc("Start date for collecting training data. Format: \"yyyy-MM-dd HH:mm:ss\"")
				.hasArg()
				.argName("Start date")
				.build());
		
		options.addOption(Option.builder("e").longOpt("enddate")
				.desc("End date for collecting test data. Format: \"yyyy-MM-dd HH:mm:ss\"")
				.hasArg()
				.argName("End date")
				.build());
		
		options.addOption(Option.builder("d").longOpt("developer")
				.desc("collecting metrics for developer history scenario")
				.argName("developer history")
				.build());
		
		options.addOption(Option.builder("a").longOpt("git.log().all().call()")
				.desc("call all git log option. defalt : git.log().call()")
				.argName("git call all")
				.build());
		
		options.addOption(Option.builder("p").longOpt("percent")
				.desc("Percent of developer. Range : 2 ~ 9")
				.hasArg()
				.argName("percent of developer")
				.build());
		
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

	public static String getProjectName(String gitURI) {

		Pattern p = Pattern.compile(".*/(.+)\\.git");
		Matcher m = p.matcher(gitURI);
		m.find();
		return m.group(1);

	}
}
