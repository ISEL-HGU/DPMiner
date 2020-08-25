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
			System.exit(1);
		}
		
		
		Input input = new Input();
		input.gitURL = cmd.getOptionValue("i");// input path 
		if (input.gitURL.endsWith("/")) {
			input.gitURL = input.gitURL.substring(0, input.gitURL.length() - 1);
		}
		input.gitRemoteURI = input.gitURL + ".git";
		
		input.projectName = getProjectName(input.gitRemoteURI);
		
		input.outPath = cmd.getOptionValue("o");// output path 
		if (input.outPath.endsWith(File.separator)) {
			input.outPath = input.outPath.substring(0, input.outPath.length() - 1);
		}
		
		
		//각각 옵션이 null아닐때 값을 넣어주고 해당 값에 맞는 input.mode를 정해 주었다!
		if(cmd.hasOption("ij")) {
			input.jiraURL = cmd.getOptionValue("ij");
			input.jiraProjectKey = cmd.getOptionValue("jk");
			input.mode = Input.Mode.JIRA;
		}
		else if(cmd.hasOption("ik")) {
			input.issueKeyWord = cmd.getOptionValue("ik");
			input.mode = Input.Mode.KEYWORD;
		}
		else {
			input.mode = Input.Mode.GITHUB;
		}
	
		String task= cmd.getOptionValue("t");  //무슨 테스크를 할지 값을 비교해서 밑에서 정해줌 

		if (task.equals("patch")) {
			input.taskType = Input.TaskType.PATCH;
		} else if (task.equals("BIC")) {
			input.taskType = Input.TaskType.BIC;
		} else if (task.equals("metric")) {
			input.taskType = Input.TaskType.METRIC;
		} else if (task.equals("Develop_Metric")) {
			input.taskType = Input.TaskType.DEVELOPERMETRIC;
		}
		
		input.BICpath = cmd.getOptionValue("bp");  //metric 만들때 필요한 BIC 인풋 path옵션! 
		
		input.label = cmd.getOptionValue("l");
		input.startDate = cmd.getOptionValue("s");
		input.endDate = cmd.getOptionValue("e");
		
		
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

		return input;
	}
		

	private boolean isValid(CommandLine cmd, Options options) {
		try {

			if (!(cmd.hasOption("t"))) {
				String eMessage = "'t' options must be used in this program";
				throw new Exception(eMessage);
			}
			if (!(cmd.hasOption("i"))) {
				String eMessage = "'i' options must be used in this program";
				throw new Exception(eMessage);
			}
			if (!(cmd.hasOption("o"))) {
				String eMessage = "'o' options must be used in this program";
				throw new Exception(eMessage);  //여기 위로는 필수로 필요햔 옵션들 
			}
			
			String task= cmd.getOptionValue("t");  //t가 해당하는 옵션이 없을 경우 에러 
			
			if (!((task.equals("metric"))||(task.equals("Develop_Metric"))||(task.equals("patch"))||(task.equals("BIC")))){
				System.out.println(task);
				String eMessage = "task option have to be patch or BIC or metric or Develop_Metirc";
				throw new Exception(eMessage);
			}
			// path 이거나 bic 이라면 옆에 해당 옵션중 한개는 무조건 있어야 한다. 
			if ((task.equals("patch")||task.equals("BIC")) ^ (cmd.hasOption("ij")||cmd.hasOption("ig")||cmd.hasOption("ik"))) {
				String eMessage = "Making patch or BIC.csv need 'ij' or 'ig' of 'ik'option";
				throw new Exception(eMessage);
			}
			// metic과 Develop_Metric이라면 bP(BIC path)는 무조건 있어야 한다.
			if ((task.equals("metric")||task.equals("Develop_Metric")) ^ (cmd.hasOption("bp"))) {
				String eMessage = "It need a BIC CSV file to make a metric";
				throw new Exception(eMessage);
			}
			
			//온셥에 ij가 있다면 jk는 무조건 있어야 한다.
			if (cmd.hasOption("ij") ^ cmd.hasOption("jk")) {
				String eMessage = "'ij' and 'jk' Option must be used together!";
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
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			printHelp(options);
			return false;
		}
		return true;
	}
	
	
	
//bic CSV파일 만드는 컴파일 명령  	
//	./BugPatchCollector -i https://github.com/apache/nutch -o /Users/juhui/Desktop/testMain/BIC -t BIC -ij issues.apache.org -jk NUTCH 
	
//arff파일 만드는 명령
//	./BugPatchCollector -i https://github.com/apache/metamodel -o /data/metric -t -c /data/BIC/BIC_metamodel.csv
	
//바뀐 메인 돌리기 BIC
//	./BugPatchCollector -i https://github.com/apache/juddi -ik test -t BIC -o /Users/juhui/Desktop/KeyTest
//	./BugPatchCollector -i https://github.com/apache/camel -ij issues.apache.org -jk CAMEL -t patch -o /Users/juhui/Desktop/PatchTest

//바뀐 메인 메트릭.....
//	./BugPatchCollector -i https://github.com/apache/juddi  -t metric -bp /Users/juhui/Desktop/TestMain/BIC/BIC_juddi.csv -o /Users/juhui/Desktop/result

	
//	./BugPatchCollector -i https://github.com/apache/juddi -ik fix -t BIC -o /Users/juhui/Desktop/testMain/keyword
	//https://commons.apache.org/proper/commons-cli/properties.html 옵션쓰 
	//https://stackoverflow.com/questions/7739214/command-line-parser-and-lack-of-subcommand-and-grouping
	private Options createOptions() {// desc 다시쓰자!
		Options options = new Options();

		options.addOption(
				Option.builder("i").longOpt("input").desc("input type: URL(https://github.com/user/project_name")
						.hasArg().argName("URI or URL").required().build());

		options.addOption(Option.builder("o").longOpt("result").desc("directory will have result file").hasArg()
				.argName("directory").required().build());
		
		options.addOption(Option.builder("t").longOpt("task").desc("kind of task").hasArg()
				.argName("task").required().build());

		options.addOption(Option.builder("ij").longOpt("issue jiraURL").desc("Jira issues URL (example: issues.apache.org)")
				.hasArg().argName("Jira project URL").build());
		
		options.addOption(Option.builder("jk").longOpt("jiraProject keyword").desc(
				"Jira project key. you can get more informations: https://github.com/HGUISEL/BugPatchCollector/issues/18")
				.hasArg().argName("Project Key").build());
		
		options.addOption(Option.builder("ig").longOpt("issue github")
				.desc("When searching fix-commit, use GitHub-issues with label name").build());
		
		options.addOption(Option.builder("ik").longOpt("issue keyword").desc("keyword of commit message")
				.hasArg().argName("CommitMsgkeyword").build());
		
		options.addOption(Option.builder("bp").longOpt("BugIntroducingChange csv file path").desc("Path of BIC csv file")
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
		
		options.addOption(Option.builder("l").longOpt("label").desc("Set a bug label of github (default: 'bug')")
				.hasArg().argName("Find coincident commit with label").build());

		
		options.addOption(Option.builder("h").longOpt("help").desc("Help").build());


		options.addOption(Option.builder("p").longOpt("percent")
		.desc("Percent of developer. Range : 2 ~ 9")
		.hasArg()
		.argName("percent of developer")
		.build());
		
		
		options.addOption(Option.builder("x").longOpt("max").desc(
		"Set a Max lines of each result patch. Only count '+++' and '---' lines. must used with '-m'. (default: 500)")
		.hasArg().argName("Max lines of patch").build());

       options.addOption(Option.builder("m").longOpt("min")
		.desc("Set a Min lines of each result patch. This Option need to be used with 'x' Option. (default: 0)")
		.hasArg().argName("Min lines of patch").build());

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
