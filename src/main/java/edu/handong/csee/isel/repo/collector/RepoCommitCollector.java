package edu.handong.csee.isel.repo.collector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import edu.handong.csee.isel.repo.RepoCollectable;
import edu.handong.csee.isel.repo.control.GithubService;
import edu.handong.csee.isel.repo.control.RetroBasic;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RepoCommitCollector implements RepoCollectable {

	private Retrofit retrofit;
	private boolean pageLoadBlocked = false; // indicate which the page is over 10 or not.
	private HashSet<String> repoResult;
	private HashSet<String> finalResult = new HashSet<>();
	private int commitRange;
	private boolean excess = false;

	private String commitCountBase;
	private String authToken;
	private String outPath;

	public RepoCommitCollector(HashSet<String> repoResult, String authToken, String commitCountBase, String outPath) {
		this.authToken = authToken;
		this.commitCountBase = commitCountBase;
		this.retrofit = new RetroBasic().createObject(this.authToken);
		this.repoResult = repoResult;
		this.outPath = outPath;

		if (this.commitCountBase.contains("less") || this.commitCountBase.contains("over")) {
			if (this.commitCountBase.contains("over"))
				excess = true;
			commitRange = Integer.parseInt(this.commitCountBase.substring(4, this.commitCountBase.length()));
		} else {
			commitRange = Integer.parseInt(this.commitCountBase);
		}
	}

	@Override
	public HashSet<String> collectFrom() throws InterruptedException, IOException {
		// TODO Auto-generated method stub
		if (this.commitCountBase != null) {
			double progress = 0, percentage = 0;

			for (String query : repoResult) {
				Thread.sleep(300);
				if (!pageLoadBlocked) {
					percentage = progress / repoResult.size() * 100.0;
//					System.out.println(String.format("%.1f", percentage) + "% work completed.");
					progress++;
					getOneQueryData(query);
				}

				else {
					while (pageLoadBlocked) {
						Thread.sleep(300);
						getOneQueryData(query);
					}

				}
			}

			SimpleDateFormat sd = new SimpleDateFormat("MM-dd");
			Date time = new Date();
			String today = sd.format(time);
			String base_github = "https://github.com/";

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outPath + File.separator + today + "__commit__list.csv"), true));
			PrintWriter pw = new PrintWriter(bw, true);
			pw.write("REPO" + "," + "COMMITS" + "\n");

			pw.flush();

			for (String result : finalResult) {
				String[] arr = result.split("#");
				pw.write(base_github + arr[0] + "," + arr[1] + "," + "\n");
				pw.flush();
			}

			bw.close();
			pw.close();

//			System.out.println(finalResult.size() + " results are finally stored.\n");

		}
		return null;
	}

	@Override
	public void getOneQueryData(String query) {
		// TODO Auto-generated method stub
		String[] arr = query.split("\\/");
		int yearlyCommit = 0;

		GithubService service = retrofit.create(GithubService.class);
		Call<JsonArray> request = service.getUserCommits(arr[0], arr[1]);

		try {
//			System.out.println(request.toString());
			Response<JsonArray> response = request.execute();

			if (response.message().equals("Forbidden")) {
//				System.out.println("Commit : Waiting for request ...");
				pageLoadBlocked = true;
				return;
			}

			else
				pageLoadBlocked = false;

			for (int i = 0; i < response.body().size(); i++) {
				JsonObject total = new Gson().fromJson(response.body().get(i), JsonObject.class);
				yearlyCommit += total.get("total").getAsInt();
			}

			if (excess) { // over일 경우 true
				if (yearlyCommit >= commitRange)
					finalResult.add(query + "#" + yearlyCommit);
			} else {
				if (yearlyCommit <= commitRange)
					finalResult.add(query + "#" + yearlyCommit);
			}
		}

		catch (JsonSyntaxException jse) {

		}

		catch (Exception ex) {
			ex.printStackTrace();
		}

	}

}
