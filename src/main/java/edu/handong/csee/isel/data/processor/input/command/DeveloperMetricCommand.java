package edu.handong.csee.isel.data.processor.input.command;                                                                                                                                                                                                                                                                                                                                  
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.handong.csee.isel.data.Input;
 import edu.handong.csee.isel.data.Input.TaskType;
 import picocli.CommandLine.Command;
 import picocli.CommandLine.Option;
 
 @Command(
           name = "devmetric",
           description = "collecting metrics for developer history scenario and making the arff file.",
           usageHelpAutoWidth = true,
           usageHelpWidth = 200,
           sortOptions = false,
           headerHeading = "Usage:%n",
           synopsisHeading = "%n",
           descriptionHeading = "%nDescription:%n%n",
           optionListHeading = "%nOptions:%n"
         )   
 public class DeveloperMetricCommand implements Runnable{
     @Option(names = "-i", required = true, description = "--input <URI or URL> \ninput type:URL(https://github.com/user/project_name \noption must be used in this program.") 
     private String inputGitURL;
     @Option(names = "-o", required = true, description = "--result <directory> \nDirectory will have result file. \noption must be used in this program.") 
     private String outputPath;
     @Option(names = "-bp", required = true, description = "--BugIntroducingChange csv file path <BIC csv file path> \nPath of csv file.") 
     private String bicPath;
     
 
     @Option(names = "-s", description = "--startdate <Start date> \nStart date for collecting training data. \nFormat:\"yyyy-MM-dd HH:mm:ss\".") 
     private String startDate;
     @Option(names = "-e", description = "--enddate <End date> \nEnd date for collecting test data. \nFormat:\"yyyy-MM-dd HH:mm:ss\".") 
     private String endDate;
     
     @Option(names = "-p", description = "--percent <percent of developer> \nPercent of developer. Range : 2 ~ 9. (default:5)") 
     private int percent = 5;
     
     @Override
     public void run() {
         Input.taskType = TaskType.DEVELOPERMETRIC;
         Input.gitURL = inputGitURL;
         Input.outPath = outputPath;
         Input.BICpath = bicPath;
         Input.gitRemoteURI = Input.gitURL + ".git";
         Input.projectName = getProjectName(Input.gitRemoteURI);
     
         if(startDate != null )
             Input.startDate = startDate;
         if(endDate != null )
             Input.endDate = endDate;
     
         if(percent > 1 && percent < 10) {
             Input.percent = percent;
         }else {
             System.out.println("percent value is out of range (Range : 2 ~ 9). set to default value 5....");
             Input.percent = 5;
         }   
     }   
     
     private String getProjectName(String gitURI) {
         Pattern p = Pattern.compile(".*/(.+)\\.git");
         Matcher m = p.matcher(gitURI);
         m.find();
         return m.group(1);
     }   
 
 }