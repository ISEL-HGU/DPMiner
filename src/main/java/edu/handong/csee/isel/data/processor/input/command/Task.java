package edu.handong.csee.isel.data.processor.input.command;                                                                                                                                                        
 
 import static picocli.CommandLine.*;
 import picocli.CommandLine;
 import picocli.CommandLine.Model.CommandSpec;
 
 
 @Command(
           subcommands = { 
        	   FindRepoCommand.class,
               BICCommand.class,
               PatchCommand.class,
               MetricCommand.class
//               DeveloperMetricCommand.class
           },  
           mixinStandardHelpOptions = true,
           usageHelpAutoWidth = true,
           sortOptions = false,
           exitCodeOnInvalidInput = 64, 
           exitCodeOnExecutionException = 70
        
         )   
 
 public class Task implements Runnable {
 
     @Spec CommandSpec spec;
     
     @Override
     public void run() {
         new CommandLine(spec).usage(System.out);
         System.exit(64);
     }   
 }
