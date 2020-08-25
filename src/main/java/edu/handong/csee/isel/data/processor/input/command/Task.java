package edu.handong.csee.isel.data.processor.input.command;                                                                                                                                                        
 
 import static picocli.CommandLine.*;
 import picocli.CommandLine;
 import picocli.CommandLine.Model.CommandSpec;
 
 
 @Command(
           subcommands = { 
               BICCommand.class,
               PatchCommand.class,
               MetricCommand.class,
               DeveloperMetricCommand.class
           },  
           mixinStandardHelpOptions = true,
           usageHelpAutoWidth = true,
           sortOptions = false,
           exitCodeOnInvalidInput = 64, 
           exitCodeOnExecutionException = 70, 
           exitCodeListHeading = "Exit Codes:%n",
           exitCodeList = { 
                         " 0:Successful program execution",
                         "64:Usage error: user input for the command was incorrect, " +
                         "e.g., the wrong number of arguments, a bad flag, " +
                         "a bad syntax in a parameter, etc.",
                         "70:Internal software error: an exception occurred when invoking " +
                         "the business logic of this command."}
         )   
 
 public class Task implements Runnable {
 
     @Spec CommandSpec spec;
     
     @Override
     public void run() {
         new CommandLine(spec).usage(System.out);
         System.exit(64);
     }   
 }
