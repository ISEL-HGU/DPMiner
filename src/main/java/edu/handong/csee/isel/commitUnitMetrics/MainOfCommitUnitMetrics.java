package edu.handong.csee.isel.commitUnitMetrics;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;


public class MainOfCommitUnitMetrics {
	String gitRepositoryPath = null;
	String resultDirectory = null;
	boolean verbose;
	boolean help;
	
	public static void main(String[]args) {
		MainOfCommitUnitMetrics my = new MainOfCommitUnitMetrics();
		my.run(args);
	}
	
	private void run(String[] args) {
		Options options = createOptions();
		
		if(parseOptions(options, args)){
			if (help){
				printHelp(options);
				return;
			}
			
			//주소를 받아와
			//커밋을 모두 저장
			CommitCollector commitCollector = new CommitCollector(gitRepositoryPath,resultDirectory);
			commitCollector.countCommitMetrics();
			
			//커밋하나 하나를 꺼내며 메트릭 count
			//arff파일프린트 
			
			if(verbose) {
				
				// TODO list all files in the path
				
				System.out.println("Your program is terminated. (This message is shown because you turned on -v option!");
			}
		}
	}

	private boolean parseOptions(Options options, String[] args) {
		CommandLineParser parser = new DefaultParser();

		try {

			CommandLine cmd = parser.parse(options, args);

			gitRepositoryPath = cmd.getOptionValue("i");
			resultDirectory = cmd.getOptionValue("o");
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
		options.addOption(Option.builder("i").longOpt("input").desc(
				"Three input type: URL or URI(github.com, reference file having github URLs, Local " + "Repository)")
				.hasArg().argName("URI or URL").required().build());

		options.addOption(Option.builder("o").longOpt("result").desc("directory will have result file").hasArg()
				.argName("directory").build());
		
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
