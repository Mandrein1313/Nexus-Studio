package com.dev.ministudio;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.json.JSONArray;
import org.json.JSONObject;

public class BuildTaskManager {

    public interface BuildListener {
        void onLogAppend(String text, int color);
        void onBuildStarted();
        void onBuildFinished(boolean success, String apkPath);
    }

    private final Context context;
    private final String projectPath;
    private final BuildEnvironmentManager envManager;
    private final BuildListener listener;
    private final Handler uiHandler = new Handler(Looper.getMainLooper());

    // ===== Tokyo Night colors =====
    private final int COLOR_CMD     = Color.parseColor("#7DCFFF"); // ฟ้าอ่อน — คำสั่ง $
    private final int COLOR_INFO    = Color.parseColor("#7AA2F7"); // ฟ้า
    private final int COLOR_SUCCESS = Color.parseColor("#9ECE6A"); // เขียว
    private final int COLOR_ERROR   = Color.parseColor("#F7768E"); // แดง
    private final int COLOR_WARNING = Color.parseColor("#E0AF68"); // ส้ม
    private final int COLOR_TASK    = Color.parseColor("#BB9AF7"); // ม่วง — > Task
    private final int COLOR_MUTED   = Color.parseColor("#565F89"); // เทา
    private final int COLOR_APK     = Color.parseColor("#73DACA"); // มิ้นต์ — APK path

    private BuildSummaryAnalyzer externalAnalyzer;

    public BuildTaskManager(Context context, String projectPath, BuildListener listener) {
        this.context = context;
        this.projectPath = projectPath;
        this.envManager = new BuildEnvironmentManager(context);
        this.listener = listener;
    }

    public void setAnalyzer(BuildSummaryAnalyzer analyzer) {
        this.externalAnalyzer = analyzer;
    }

    public void startCloudBuild(final String githubToken, final String repoUrl,
                                final String projectName, final String packageName) {
        postUiEvent(BuildListener::onBuildStarted);

        new Thread(() -> {
            try {
                sendProgress("$ ./gradlew :app:assembleDebug\n", COLOR_CMD);
                sendProgress("Starting Gradle Daemon...\n", COLOR_MUTED);

                File projectDir = new File(projectPath);

                createGitIgnore(projectDir);
                envManager.prepareGitHubWorkflow(projectPath, projectName, packageName, "Java", 21);

                sendProgress("> Configure project :app\n", COLOR_TASK);
                sendProgress("Uploading sources to GitHub remote...\n", COLOR_INFO);

                Git git;
                File gitDir = new File(projectDir, ".git");
                if (!gitDir.exists()) {
                    git = Git.init().setDirectory(projectDir).call();
                } else {
                    git = Git.open(projectDir);
                }

                git.add().addFilepattern(".").call();
                git.commit().setMessage("Cloud Build Request - "
                        + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())).call();

                StoredConfig config = git.getRepository().getConfig();
                config.setString("remote", "origin", "url", repoUrl);
                config.save();

                UsernamePasswordCredentialsProvider credentials =
                        new UsernamePasswordCredentialsProvider(githubToken, "");
                PushCommand push = git.push();
                push.setCredentialsProvider(credentials);
                push.setForce(true);
                push.setRemote("origin");
                push.add("master").add("main");
                push.call();

                sendProgress("> Task :app:preBuild\n", COLOR_TASK);
                sendProgress("> Task :app:preDebugBuild\n", COLOR_TASK);
                sendProgress("Sources uploaded. Waiting for cloud build...\n", COLOR_SUCCESS);

                String repoPath = repoUrl.replace("https://github.com/", "").replace(".git", "");
                monitorWorkflowRuns(githubToken, repoPath, projectName);

            } catch (Exception e) {
                sendProgress("\nBUILD FAILED\n", COLOR_ERROR);
                sendProgress("Execution failed: " + e.getMessage() + "\n", COLOR_ERROR);
                postUiEvent(l -> l.onBuildFinished(false, null));
            }
        }).start();
    }

    private void monitorWorkflowRuns(String token, String repoPath, String projectName) {
        try {
            String urlStr = "https://api.github.com/repos/" + repoPath + "/actions/runs?per_page=1";
            sendProgress("> Task :app:mergeDebugResources\n", COLOR_TASK);
            sendProgress("Waiting for GitHub Actions runner...\n", COLOR_MUTED);

            long startTime = System.currentTimeMillis();
            long runId = -1;

            while (System.currentTimeMillis() - startTime < 180000) {
                Thread.sleep(5000);
                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) sb.append(line);
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    JSONArray runs = json.getJSONArray("workflow_runs");
                    if (runs.length() > 0) {
                        JSONObject latestRun = runs.getJSONObject(0);
                        runId = latestRun.getLong("id");
                        String status = latestRun.getString("status");

                        if ("in_progress".equals(status) || "queued".equals(status)) {
                            sendProgress("Pipeline status: [" + status.toUpperCase() + "]\n", COLOR_WARNING);
                        } else {
                            sendProgress("Pipeline status: [" + status.toUpperCase() + "]\n", COLOR_INFO);
                        }

                        if ("completed".equals(status)) {
                            String conclusion = latestRun.optString("conclusion", "unknown");
                            boolean ok = "success".equals(conclusion);
                            sendProgress("Conclusion: [" + conclusion.toUpperCase() + "]\n",
                                    ok ? COLOR_SUCCESS : COLOR_ERROR);

                            if (ok) {
                                sendProgress("> Task :app:processDebugManifest\n", COLOR_TASK);
                                sendProgress("> Task :app:compileDebugJavaWithJavac\n", COLOR_TASK);
                                sendProgress("> Task :app:dexBuilderDebug\n", COLOR_TASK);
                                sendProgress("> Task :app:packageDebug\n", COLOR_TASK);
                                sendProgress("> Task :app:assembleDebug\n", COLOR_TASK);
                                sendProgress("\nBUILD SUCCESSFUL\n", COLOR_SUCCESS);
                                sendProgress("Fetching APK artifact...\n", COLOR_INFO);

                                DownloadTaskManager downloadTask = new DownloadTaskManager(
                                        context, projectName,
                                        new DownloadTaskManager.DownloadListener() {
                                            @Override
                                            public void onDownloadLog(String text, int color) {
                                                sendProgress(text.endsWith("\n") ? text : text + "\n", color);
                                            }

                                            @Override
                                            public void onDownloadFinished(boolean success, File apkFile) {
                                                if (success && apkFile != null) {
                                                    sendProgress(
                                                            "APK → " + apkFile.getAbsolutePath() + "\n",
                                                            COLOR_APK);
                                                } else if (!success) {
                                                    sendProgress("APK download failed\n", COLOR_ERROR);
                                                }
                                                postUiEvent(l -> l.onBuildFinished(
                                                        success,
                                                        apkFile != null ? apkFile.getAbsolutePath() : null));
                                            }
                                        });
                                downloadTask.startFetchAndInstall();

                            } else {
                                sendProgress("\nBUILD FAILED\n", COLOR_ERROR);
                                sendProgress("Conclusion: " + conclusion + "\n", COLOR_ERROR);
                                sendProgress("Fetching error logs from GitHub Actions...\n", COLOR_WARNING);

                                fetchAndParseBuildLogs(token, repoPath, runId);

                                sendProgress(
                                        "Full log: https://github.com/" + repoPath
                                                + "/actions/runs/" + runId + "\n",
                                        COLOR_INFO);

                                postUiEvent(l -> l.onBuildFinished(false, null));
                            }
                            return;
                        }
                    }
                }
            }
            sendProgress("\nBUILD FAILED\n", COLOR_ERROR);
            sendProgress("Timeout waiting for GitHub Actions (3 min)\n", COLOR_ERROR);
            postUiEvent(l -> l.onBuildFinished(false, null));
        } catch (Exception e) {
            sendProgress("\nBUILD FAILED\n", COLOR_ERROR);
            sendProgress("Status check error: " + e.getMessage() + "\n", COLOR_ERROR);
            postUiEvent(l -> l.onBuildFinished(false, null));
        }
    }

    private void fetchAndParseBuildLogs(String token, String repoPath, long runId) {
        try {
            String jobsUrl = "https://api.github.com/repos/" + repoPath
                    + "/actions/runs/" + runId + "/jobs";
            HttpURLConnection conn = (HttpURLConnection) new URL(jobsUrl).openConnection();
            conn.setRequestProperty("Authorization", "Bearer " + token);
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (conn.getResponseCode() != 200) {
                sendProgress("Could not list jobs (HTTP " + conn.getResponseCode() + ")\n", COLOR_ERROR);
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            reader.close();

            JSONObject json = new JSONObject(sb.toString());
            JSONArray jobs = json.getJSONArray("jobs");
            if (jobs.length() == 0) {
                sendProgress("No jobs found in this run\n", COLOR_ERROR);
                return;
            }

            JSONObject job = jobs.getJSONObject(0);
            long jobId = job.getLong("id");
            String jobConclusion = job.optString("conclusion", "unknown");
            sendProgress("Job conclusion: " + jobConclusion + "\n", COLOR_WARNING);

            String logUrl = "https://api.github.com/repos/" + repoPath
                    + "/actions/jobs/" + jobId + "/logs";
            HttpURLConnection logConn = (HttpURLConnection) new URL(logUrl).openConnection();
            logConn.setRequestProperty("Authorization", "Bearer " + token);
            logConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
            logConn.setRequestProperty("Accept-Encoding", "gzip, deflate");

            int logCode = logConn.getResponseCode();
            if (logCode != 200) {
                sendProgress("Could not fetch job logs (HTTP " + logCode + ")\n", COLOR_ERROR);
                sendProgress("Open Actions tab on GitHub to see compile errors\n", COLOR_INFO);
                return;
            }

            InputStream raw = logConn.getInputStream();
            String encoding = logConn.getContentEncoding();
            if (encoding != null && encoding.toLowerCase().contains("gzip")) {
                raw = new GZIPInputStream(raw);
            }

            BufferedReader logReader = new BufferedReader(new InputStreamReader(raw, "UTF-8"));
            final BuildSummaryAnalyzer analyzer =
                    (externalAnalyzer != null) ? externalAnalyzer : new BuildSummaryAnalyzer();
            analyzer.clearErrors();

            int errorLinesShown = 0;
            final int MAX_RAW_ERROR_LINES = 40;

            while ((line = logReader.readLine()) != null) {
                analyzer.analyzeLine(line, COLOR_WARNING, (txt, col) -> { /* internal */ });

                String lower = line.toLowerCase();
                boolean looksError = lower.contains("error:")
                        || lower.contains("e: ")
                        || lower.contains("failure:")
                        || lower.contains("what went wrong")
                        || lower.contains("execution failed")
                        || lower.contains("aapt")
                        || (line.contains(".java:") && lower.contains("error"))
                        || (line.contains(".kt:") && lower.contains("error"))
                        || (line.contains(".xml:") && lower.contains("error"));

                if (looksError && errorLinesShown < MAX_RAW_ERROR_LINES) {
                    sendProgress(line + "\n", COLOR_ERROR);
                    errorLinesShown++;
                }
            }
            logReader.close();

            if (analyzer.hasError()) {
                analyzer.printSummary((txt, col) -> sendProgress(txt, col));
            } else if (errorLinesShown == 0) {
                sendProgress("No parseable error lines found in log\n", COLOR_WARNING);
                sendProgress("Check the Actions tab on GitHub for full details\n", COLOR_INFO);
            } else {
                sendProgress("\n(See error lines above)\n", COLOR_WARNING);
            }

        } catch (Exception e) {
            sendProgress("Log parse error: " + e.getMessage() + "\n", COLOR_ERROR);
            sendProgress("Open GitHub → Actions to view the full build log\n", COLOR_INFO);
        }
    }

    private void createGitIgnore(File projectDir) {
        try {
            File gitIgnoreFile = new File(projectDir, ".gitignore");
            String content = ".gradle/\nbuild/\napp/build/\n*.iml\nlocal.properties\n";
            try (FileOutputStream fos = new FileOutputStream(gitIgnoreFile)) {
                fos.write(content.getBytes("UTF-8"));
            }
        } catch (Exception ignored) {
        }
    }

    private void sendProgress(final String text, final int color) {
        uiHandler.post(() -> listener.onLogAppend(text, color));
    }

    private interface UiEventAction {
        void run(BuildListener listener);
    }

    private void postUiEvent(final UiEventAction action) {
        uiHandler.post(() -> action.run(listener));
    }
}