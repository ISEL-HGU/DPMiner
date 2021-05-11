package edu.handong.csee.isel.repo.collector;

import java.io.IOException;
import java.util.HashSet;

import javax.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import edu.handong.csee.isel.data.Input;
import edu.handong.csee.isel.repo.RepoCollectable;
import edu.handong.csee.isel.repo.control.GithubService;
import edu.handong.csee.isel.repo.control.RetroBasic;

public class RepoCollector implements RepoCollectable {

	private Retrofit retrofit;
	private boolean isEndOfData = false;
	private boolean pageLoadBlocked = false; // indicate which the page is over 10 or not.
	private String lastDate; // standard
	private HashSet<String> repoResult;
	private String outPath;

	// for input
	public String authToken;
	public HashMap<String, String> findRepoOpt;

	public RepoCollector(String authToken, HashMap<String, String> findRepoOpt, String outPath) {
		this.authToken = authToken;
		this.findRepoOpt = findRepoOpt;
		this.outPath = outPath;
		repoResult = new HashSet<>();
		retrofit = new RetroBasic().createObject(authToken); // Retrofit turns your REST API into a Java // interface.
	}


	@Override
	public HashSet<String> collectFrom() throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		double randomRange;
		int randomSec;
		int pages = 1;
		long start = System.currentTimeMillis();

		while (!isEndOfData) { 

			while (pages != 11 && !isEndOfData) { 
				try {
					randomRange = Math.random();
					randomSec = (int) (randomRange * 2000) + 100;
					Thread.sleep(randomSec);
					System.out.println(randomSec);
				}catch(InterruptedException e) {
	                e.printStackTrace();
	                return null;
	            }

				findRepoOpt.replace("page", String.valueOf(pages));

				long start2 = System.currentTimeMillis();
				getOneQueryData(""); // 함수호출
				long end2 = System.currentTimeMillis();

				System.out.println( "하나의 쿼리 실행 시간 : " + ( end2 - start2 )/1000.0 );

//				System.out.println("current page : " + pages);
//				System.out.println(findRepoOpt.get("q"));

				if (!pageLoadBlocked) //
					pages++;

			}

			changeRepoUpdate(findRepoOpt, lastDate); 
			pages = 1;
		}
		long end = System.currentTimeMillis();

		System.out.println( "실행 시간 : " + ( end - start )/1000.0 );


		SimpleDateFormat sd = new SimpleDateFormat( "MM-dd" );
		Date time = new Date();
		String today = sd.format(time);
		String base_github = "https://github.com/";
		
		File resultFile = new File(Input.outPath);
		resultFile.mkdirs();
		String path = Input.outPath + File.separator + today + "_repository_list.csv";
		
		BufferedWriter bw = Files.newBufferedWriter(Paths.get(path));

		PrintWriter pw = new PrintWriter(bw, true);
		pw.write("REPO" + "\n");
		pw.flush();

		for (String result : repoResult) {
			pw.write(base_github + result + "," + "\n");
			pw.flush();
		}

//		System.out.println(repoResult.size() + " results are stored in " + today + "_Repo_list.csv");
		bw.close();
		pw.close();

		return repoResult;
	}

	@Override
	public void getOneQueryData(String query) {
		// TODO Auto-generated method stub
		GithubService service = retrofit.create(GithubService.class);
		Call<JsonObject> request = service.getJavaRepositories(findRepoOpt);

		try {
			Response<JsonObject> response = request.execute();

			if (response.message().equals("Forbidden")) { // Status: 403 Forbidden
//				System.out.println("Repo : Waiting for request ...");
				pageLoadBlocked = true;
				return;
			}

			else
				pageLoadBlocked = false;

			JsonArray jsArr = new Gson().fromJson(response.body().get("items"), JsonArray.class);

			if (jsArr.size() == 0) {
//				System.out.println("There is no content ever.");
				isEndOfData = true;
				return;
			}

			for (int i = 0; i < jsArr.size(); i++) {
				JsonObject item = new Gson().fromJson(jsArr.get(i), JsonObject.class);
				repoResult.add(item.get("full_name").getAsString());

				if (i == jsArr.size() - 1)
					lastDate = item.get("pushed_at").getAsString();
			}

		}

		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void changeRepoUpdate(HashMap<String, String> options, String last_pushed) {
		String chg = options.get("q");
		String changed_date = last_pushed.substring(0, last_pushed.indexOf('T'));

		if (chg.contains("pushed")) {
			String origin_date = chg.substring(chg.length() - 10, chg.length());

			if (origin_date.equals(changed_date))
				changed_date = modifyIfSame(chg, changed_date);

			chg = chg.replace(origin_date, changed_date);
		}

		else
			chg += " pushed:<=" + changed_date;

		options.replace("q", chg);

	}

	private String modifyIfSame(String query, String date) {
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7));
		String day = date.substring(8, 10);
		String returnDate;

		if (query.contains(">=")) {
			if (month == 12) {
				year++;
				month = 1;
			} else
				month++;

			if (month < 10)
				returnDate = year + "-0" + month + "-" + day;

			else
				returnDate = year + "-" + month + "-" + day;
		}

		else {
			if (month == 1) {
				year--;
				month = 12;
			} else
				month--;

			if (month < 10)
				returnDate = year + "-0" + month + "-" + day;

			else
				returnDate = year + "-" + month + "-" + day;
		}

		return returnDate;
	}

}
