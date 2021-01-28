package edu.handong.csee.isel.data.processor.input.command;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                

import static picocli.CommandLine.*;

import java.util.HashMap;

import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.data.Input.TaskType;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Model.CommandSpec;

@Command(
          name = "findrepo",
          description = "Find the list of repertoires that satisfy the user's choice.",
          usageHelpAutoWidth = true,
          usageHelpWidth = 300,
          sortOptions = false,
          headerHeading = "Usage:%n",
          synopsisHeading = "%n",
          descriptionHeading = "%nDescription:%n%n",
          optionListHeading = "%nOptions:%n"
        )   
public class FindRepoCommand implements Runnable {

    @Option(names = "-l", description = "--language setting <java, c, cpp, javascript, ruby, etc.> \nSet language to search repositories.") 
    private String languageType;
    
    @Option(names = "-f", description = "--fork counts <num..num> \nSet the count of forks to range of repositories.") 
    private String forkNum;
    
    @Option(names = "-d", description = "--recent push date <yyyy-MM-dd..yyyy-MM-dd> \nSet the date when the author pushed lately") 
    private String recentDate;
    
    @Option(names = "-c", description = "--created date <yyyy-MM-dd..yyyy-MM-dd> \nSet the date when the repository was created") 
    private String createDate;
    
    @Option(names = "-cb", description = "--commit count <'>num' or '<num'> \nSet commit count") 
    private String commitCountBase;
    
    @Option(names = "-auth", description = "--authentication token <token> \nSet authentication token") 
    private String authToken;
  
    @Spec CommandSpec spec;
	
    @Override
    public void run() {
        Input.taskType = TaskType.FINDREPO;
        
        String repo_opt = "";
        
        if(languageType != null) {
        	repo_opt += "language:" + languageType;
        	Input.languageType = languageType;
        }
        
        if(forkNum != null) {
        	repo_opt += " forks:" + forkNum;
        	Input.forkNum = forkNum;
        }
        
        if(createDate != null) {
        	repo_opt += " created:" + createDate;
        	Input.createDate = createDate;
        }
        
        if(recentDate != null) {
        	repo_opt += " pushed:" + recentDate;
        	Input.recentDate = recentDate;
        }
       
        
        if(repo_opt.equals("")) {
        	new CommandLine(spec).usage(System.out);
            System.exit(64);
        }
        
        Input.findRepoOpt = new HashMap<>();
        Input.findRepoOpt.put("q", repo_opt);
        Input.findRepoOpt.put("sort", "updated");
        Input.findRepoOpt.put("page", "1");
        Input.findRepoOpt.put("per_page", "100");
        
        if (recentDate != null && recentDate.contains(">="))
        	Input.findRepoOpt.put("order", "asc");
        
        if(commitCountBase != null) {
        	Input.commitCountBase = commitCountBase;
        }
        
        if(authToken != null) {
        	Input.authToken = authToken;
        }
    
    }    

}
