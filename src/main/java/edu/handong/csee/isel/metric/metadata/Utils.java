package edu.handong.csee.isel.metric.metadata;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.io.DisabledOutputStream;

import edu.handong.csee.isel.metric.metadata.DeveloperExperienceInfo;
//import edu.handong.csee.isel.weka.stringtovector.CommitInfo;
//import edu.handong.csee.isel.weka.stringtovector.CommitKey;
//import edu.handong.csee.isel.weka.stringtovector.WekaParser;

public class Utils {

	public static String getStringDateTimeFromCommit(RevCommit commit) {	
		
		SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date commitDate = commit.getAuthorIdent().getWhen();

		TimeZone GMT = commit.getCommitterIdent().getTimeZone();
		ft.setTimeZone(GMT);

		return ft.format(commitDate);
	}

	public static String getDayFromCommitTime(RevCommit commit) {
		
		
		SimpleDateFormat ft =  new SimpleDateFormat("EEEEEEEE");
		Date commitDate = commit.getAuthorIdent().getWhen();

		TimeZone GMT = commit.getCommitterIdent().getTimeZone();
		ft.setTimeZone(GMT);
		String weekDay = ft.format(commitDate);
		
		if(weekDay.equals("월요일")) weekDay = "Monday";
		if(weekDay.equals("화요일")) weekDay = "Tuesday";
		if(weekDay.equals("수요일")) weekDay = "Wednesday";
		if(weekDay.equals("목요일")) weekDay = "Thursday";
		if(weekDay.equals("금요일")) weekDay = "Friday";
		if(weekDay.equals("토요일")) weekDay = "Saturday";
		if(weekDay.equals("일요일")) weekDay = "Sunday";
		
		return weekDay;
	}
	
	public static String getHourFromCommitTime(RevCommit commit) {
		
		SimpleDateFormat ft =  new SimpleDateFormat ("HH");
		Date commitDate = commit.getAuthorIdent().getWhen();

		TimeZone GMT = commit.getCommitterIdent().getTimeZone();
		ft.setTimeZone(GMT);

		return ft.format(commitDate);
	}

	public static List<String> readBICCsvFile(String BICcsvPath) {
		TreeSet<String> buggyCommit = new TreeSet<String>();
		Reader reader;

		try {
			reader = Files.newBufferedReader(Paths.get(BICcsvPath));
			CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT);

			for (CSVRecord csvRecord : csvParser) {
				if(csvRecord.get(0) != null || csvRecord.get(1) != null) {
					String hash = csvRecord.get(0);
					String source = csvRecord.get(1);
					String key = hash + "-" + source;
					buggyCommit.add(key);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		List<String> resultList = new ArrayList<String>(buggyCommit);
		System.out.println("Size of BIC : "+resultList.size());
		return resultList;
	}

	public static int calDate(String first, String second) throws ParseException {
		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		Date FirstDate = transFormat.parse(first);
		Date SecondDate = transFormat.parse(second);

		long calDate = FirstDate.getTime() - SecondDate.getTime(); 

		long calDateDays = calDate / ( 24*60*60*1000); 

		calDateDays = Math.abs(calDateDays);

		return (int)(long)calDateDays;
	}


	public static String parseAuthorID(String authorId) {
		Pattern pattern = Pattern.compile(".+\\[(.+|),\\s([^\\s]+)(\\s.+)?,.+\\]");
		Matcher matcher = pattern.matcher(authorId);
		
		if(matcher.find()) {
			authorId = matcher.group(2);
		}
		
		if(authorId.startsWith("PersonIdent")) {
			authorId = "Anonymous";
		}
		
		authorId = authorId.replace(",", "___");
		
		return authorId;
	}
	
	public static void countDeveloperCommit(HashMap<String,DeveloperExperienceInfo> developerExperience, String authorId, String commitTime) {
		String[] TimeToken = commitTime.split("-");
		int year =  Integer.parseInt(TimeToken[0]);
		TreeMap<Integer, Integer> recentExperience;
		DeveloperExperienceInfo develperExperienceInfo;
		
		if(developerExperience.containsKey(authorId) == false) {
			develperExperienceInfo = new DeveloperExperienceInfo();
			developerExperience.put(authorId, develperExperienceInfo);
			
			recentExperience = new TreeMap<Integer, Integer>();
			recentExperience.put(year, 1);
		}else {
			develperExperienceInfo = developerExperience.get(authorId);
			recentExperience = develperExperienceInfo.getRecentExperiences();
			
			if(recentExperience.containsKey(year) == false) {
				recentExperience.put(year, 1);
			}else {
				recentExperience.put(year, recentExperience.get(year) + 1);
			}
		}
		
		develperExperienceInfo.setNumOfCommits();
		develperExperienceInfo.setRecentExperiences(recentExperience);
	}
	
	public static String fetchBlob(Repository repo, String revSpec, String path)
			throws Exception {

		// Resolve the revision specification
		final ObjectId id = repo.resolve(revSpec);

		// Makes it simpler to release the allocated resources in one go
		ObjectReader reader = repo.newObjectReader();

		// Get the commit object for that revision
		RevWalk walk = new RevWalk(reader);
		RevCommit commit = walk.parseCommit(id);
		walk.close();

		// Get the revision's file tree
		RevTree tree = commit.getTree();
		// .. and narrow it down to the single file's path
		TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);

		if (treewalk != null) {
			// use the blob id to read the file's data
			byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
			reader.close();
			return new String(data, "utf-8");
		} else {
			return "";
		}

	}
	
	public static List<DiffEntry> diff(RevCommit parent, RevCommit commit, Repository repo) {

  		DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
 		df.setRepository(repo);
 		df.setDiffAlgorithm(DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS));
 		df.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
 		df.setDetectRenames(true);
 		List<DiffEntry> diffs = null;
 		try {
 			diffs = df.scan(parent.getTree(), commit.getTree());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}

  		return diffs;
 	}
	
	public static String getStringDateTimeFromCommitTime(int commitTime){
		SimpleDateFormat ft =  new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
		Date commitDate = new Date(commitTime* 1000L);

		TimeZone GMT = TimeZone.getTimeZone("GMT");
		ft.setTimeZone(GMT);

		return ft.format(commitDate);
	}

}
