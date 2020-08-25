package edu.handong.csee.isel.data.processor.input.command;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                
 
 import static picocli.CommandLine.*;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import edu.handong.csee.isel.data.Input;
 import edu.handong.csee.isel.data.Input.Mode;
 import edu.handong.csee.isel.data.Input.TaskType;
 import picocli.CommandLine.ArgGroup;
 import picocli.CommandLine.Option;
 
 @Command(
           name = "bic",
           description = "Making the BIC.csv using the issue. If you want to get bug introducing changes, use this command.",
           usageHelpAutoWidth = true,
           usageHelpWidth = 300,
           sortOptions = false,
           headerHeading = "Usage:%n",
           synopsisHeading = "%n",
           descriptionHeading = "%nDescription:%n%n",
           optionListHeading = "%nOptions:%n"
         )   
 public class BICCommand implements Runnable {
 
     @Option(names = "-i", required = true, description = "--input <URI or URL> \ninput type:URL(https://github.com/user/project_name \noption must be used in this program.") 
     private String inputGitURL;
     @Option(names = "-o", required = true, description = "--result <directory> \nDirectory will have result file. \noption must be used in this program.") 
     private String outputPath;
     
     @ArgGroup(exclusive = true, multiplicity = "1")
     Exclusive exclusive;
     
     static class Exclusive {
     
         @ArgGroup(exclusive = false) 
         DependentJira dependentJira = new DependentJira();
 
         static class DependentJira {
             @Option(names = "-ij", required = true, description = "--issue jira <Jira project URL> \nJira issues URL (example:issues.apache.org)") 
             private String jiraURL;    
             @Option(names = "-jk", required = true, description = "--jiraProject <Project Key> \nJira project key.") 
             private String jirajiraProjectKey;
         }   
     
         @ArgGroup(exclusive = false) 
         DependentGithub dependentGithub = new DependentGithub();
     
         static class DependentGithub {
             @Option(names = "-ig", required = true, description = "--issue github \nWhen searching fix-commit, use GitHub-issues with label name.") 
             private boolean github; 
             @Option(names = "-l", required = false, description = "--label <Find coincident commit with label> \n Set a bug label of github (default: 'bug').") 
             private String githubLabel = "bug";
         }   
     
         @Option(names = "-ik", required = true, description = "--issue keyword") 
         private String keyWord;
     }   
     
     @ArgGroup(exclusive = false) 
     DependentMaxMin dependentMaxMin = new DependentMaxMin();
     
     static class DependentMaxMin{
         @Option(names = "-m", required = true, description = "-m,--min <Min lines of patch> \nSet a Min lines of each result patch. \nThis Option need to be used with 'x' Option. (default: 0)") 
         private int min = 0;  
         @Option(names = "-x", required = true, description = "-x,--max <Max lines of patch> \nSet a Max lines of each result patch. Only count '+++' and '---'lines. must used with '-m'. (default:500)") 
         private int max = 500; 
     }   
     
   
     @Override
     public void run() {
         Input.taskType = TaskType.BIC;
         Input.gitURL = inputGitURL;
         Input.outPath = outputPath;
         Input.gitRemoteURI = Input.gitURL + ".git";
         Input.projectName = getProjectName(Input.gitRemoteURI);
     
         if(exclusive.dependentJira.jiraURL != null && exclusive.dependentJira.jirajiraProjectKey != null) {
             Input.jiraURL = exclusive.dependentJira.jiraURL;
             Input.jiraProjectKey = exclusive.dependentJira.jirajiraProjectKey;
             Input.mode = Mode.JIRA;
         }else if(exclusive.dependentGithub.github) {
             Input.mode = Mode.GITHUB;
             if(exclusive.dependentGithub.githubLabel != null) {
                 Input.label = exclusive.dependentGithub.githubLabel;
             }   
         }else if(exclusive.keyWord != null) {
             Input.issueKeyWord = exclusive.keyWord;
             Input.mode = Mode.KEYWORD;
         }   
     
         if (dependentMaxMin.max > dependentMaxMin.min) {
              Input.maxSize = dependentMaxMin.max;
              Input.minSize = dependentMaxMin.min;
         }else {
             System.out.println("min must be less then max. set to default value (min:0, max:500)....");
             Input.maxSize = 500;
             Input.minSize = 0;
         }   
     
     }   
 
 
     private String getProjectName(String gitURI) {
         Pattern p = Pattern.compile(".*/(.+)\\.git");
         Matcher m = p.matcher(gitURI);
         m.find();
         return m.group(1);
     }   
 
 }
