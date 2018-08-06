package edu.handong.csee.isel.parsers;

public class ExampleRunner {

	public static void main(String[] args) {
		String testString = "diff --git a/hbase-backup/src/main/java/org/apache/hadoop/hbase/backup/util/BackupUtils.java b/hbase-backup/src/main/java/org/apache/hadoop/hbase/backup/util/BackupUtils.java\n"
				+ "index e01849a..12f2f2f 100644\n"
				+ "--- a/hbase-backup/src/main/java/org/apache/hadoop/hbase/backup/util/BackupUtils.java\n"
				+ "+++ b/hbase-backup/src/main/java/org/apache/hadoop/hbase/backup/util/BackupUtils.java\n"
				+ "@@ -695,7 +695,7 @@\n" + "       throws IOException {\n"
				+ "     FileSystem fs = FileSystem.get(conf);\n"
				+ "     String tmp = conf.get(HConstants.TEMPORARY_FS_DIRECTORY_KEY,\n"
				+ "-            HConstants.DEFAULT_TEMPORARY_HDFS_DIRECTORY);\n"
				+ "+            fs.getHomeDirectory() + \"/hbase-staging\");\n" + "     Path path =\n"
				+ "         new Path(tmp + Path.SEPARATOR + \"bulk_output-\" + tableName + \"-\"\n"
				+ "             + EnvironmentEdgeManager.currentTime());";

		Parser parser = new Parser();
		
		
		/* convertStringAsLineList */
		String[] newStrings = parser.convertStringAsLineList(testString);
		for (String str : newStrings) {
			if(parser.isStartWithMinus(str) || parser.isStartWithPlus(str))
				System.out.println(str);
			
			
//			System.out.println(str);

		}
		/* convertStringAsLineList */
		
		
		
	}

}
