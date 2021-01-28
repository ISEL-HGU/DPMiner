package edu.handong.csee.isel.repo.collector;

import java.io.IOException;
import java.util.HashSet;

import javax.annotation.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import edu.handong.csee.isel.repo.RepoCollectable;
import edu.handong.csee.isel.repo.control.GithubService;
import edu.handong.csee.isel.repo.control.RetroBasic;

public class RepoCollector implements RepoCollectable {

	private Retrofit retrofit;
	private boolean isEndOfData = false;
	private boolean pageLoadBlocked = false; // indicate which the page is over 10 or not.
	private String lastDate; // standard
	private HashSet<String> repoResult;

	// for input
	public String authToken;
	public HashMap<String, String> findRepoOpt;

	public RepoCollector(String authToken, HashMap<String, String> findRepoOpt) {
		this.authToken = authToken;
		this.findRepoOpt = findRepoOpt;
		repoResult = new HashSet<>();
		retrofit = new RetroBasic().createObject(authToken); // Retrofit turns your REST API into a Java // interface.
	}

	
	
//	@Nullable 
//	public void setOption(String languageType, String forkNum, String createDate, String recentDate) {
//		String repo_opt = "";
//
//		if (languageType != null) {
//			repo_opt += "language:" + languageType;
//		}
//
//		if (forkNum != null) {
//			repo_opt += " forks:" + forkNum;
//		}
//
//		if (createDate != null) {
//			repo_opt += " created:" + createDate;
//		}
//
//		if (recentDate != null) {
//			repo_opt += " pushed:" + recentDate;
//		}
//
//		if (repo_opt.equals("")) {
//			System.exit(64);
//		}
//
//		this.findRepoOpt = new HashMap<>();
//		this.findRepoOpt.put("q", repo_opt);
//		this.findRepoOpt.put("sort", "updated");
//		this.findRepoOpt.put("page", "1");
//		this.findRepoOpt.put("per_page", "100");
//
//		if (recentDate != null && recentDate.contains(">="))
//			findRepoOpt.put("order", "asc");
//	}

	@Override
	public HashSet<String> collectFrom() throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		double randomRange;
		int randomSec;
		int pages = 1;

		while (!isEndOfData) { // repo값을 받아온 경우 실행됨. while문 하나로 바꾸기, null로 바꾸자

			while (pages != 11 && !isEndOfData) { // 페이지 10페이지를 넘기지 않음. getRepoResult에 값이 없으면 멈춤 즉 더이상 가져올 데이터가 없는 경우
													// 멈춘다.

				// random sleep time to 1~3s, 왜 sleep하는가?
				randomRange = Math.random();
				randomSec = (int) (randomRange * 2000) + 100;
				Thread.sleep(randomSec);

				findRepoOpt.replace("page", String.valueOf(pages)); // repoOpt에 있는 page를 계속 업데이트 해준다.

				getOneQueryData(""); // 함수호출

//				System.out.println("current page : " + pages);
//				System.out.println(findRepoOpt.get("q"));

				if (!pageLoadBlocked) //
					pages++;

			}

			changeRepoUpdate(findRepoOpt, lastDate); //
			pages = 1;
		}
		String base_github = "https://github.com/";

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("test" + "_Repo_list.csv"), true));
		PrintWriter pw = new PrintWriter(bw, true);
		pw.write("REPO" + "\n");
		pw.flush();

		for (String result : repoResult) {
			pw.write(base_github + result + "," + "\n");
			pw.flush();
		}

//		System.out.println(repoResult.size() + " results are stored in " + "test" + "_Repo_list.csv");
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
