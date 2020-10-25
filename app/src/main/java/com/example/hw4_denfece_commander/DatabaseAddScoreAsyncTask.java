package com.example.hw4_denfece_commander;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseAddScoreAsyncTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "DatabaseAddScoreAsyncTa";
    private static final String Driver = "com.mysql.jdbc.Driver";
    private MainActivity context;
    private Connection conn;
    private static final String URL = "jdbc:mysql://christopherhield.com:3306/chri5558_missile_defense";
    private static final String USERNAME = "chri5558_student";
    private static final String TABLE = "AppScores";
    private static final String PASSWORD = "ABC.123";
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    private boolean settingNewHighScore = false;

    DatabaseAddScoreAsyncTask(MainActivity mainActivity) {
        context = mainActivity;
    }

    @Override
    protected void onPostExecute(String leaderboard) {
        context.updateLeaderBoard(leaderboard);
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            Log.d(TAG, "doInBackground: Adding in New TOP Score!");
                settingNewHighScore = true;
                conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                //Initials
                String initials = strings[0];
                //Score
                int score = Integer.parseInt(strings[1]);
                //Level
                int level = Integer.parseInt(strings[2]);
            Log.d(TAG, "doInBackground: Init: "+initials+" Score: "+score+" Level: "+level);
                return addScore(initials, score, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String addScore(String initials, int score, int level) throws SQLException {
        Statement stmt = conn.createStatement();
        String sql = "insert into " + TABLE + " values (" +
                System.currentTimeMillis() + ", '" + initials + "', " + score + ", " +
                level +
                ")";
        stmt.executeUpdate(sql);
        stmt.close();
        return getScores();
    }

    private String getScores() throws SQLException {
        int leaderboardPosition = 1;
        Statement stmt = conn.createStatement();
        String sql = "select * from " + TABLE + " order by Score desc LIMIT 10";
        StringBuilder sb = new StringBuilder();
        ResultSet rs = stmt.executeQuery(sql);
        while (rs.next()) {
            long millis = rs.getLong(1);
            String initials = rs.getString(2);
            int score = rs.getInt(3);
            int level = rs.getInt(4);
            sb.append(String.format(Locale.getDefault(),
                    "%1$-10d\t%2$-10s\t%3$-10d\t%4$-10d\t%5$-15s%n", leaderboardPosition++, initials, level, score, sdf.format(new Date(millis))));
        }
        rs.close();
        stmt.close();
        return sb.toString();
    }
   /*  Ended up not using
    private String getScores() throws SQLException {
        try {
            int leaderboardPosition = 1;
            JSONObject jsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            Statement stmt = conn.createStatement();
            String sql = "select * from " + TABLE + " order by Score desc LIMIT 10";
            StringBuilder sb = new StringBuilder();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                JSONObject jS = new JSONObject();
                long millis = rs.getLong(1);
                String initials = rs.getString(2);
                int score = rs.getInt(3);
                int level = rs.getInt(4);
                jS.put("position", leaderboardPosition++);
                jS.put("initials", initials);
                jS.put("level", level);
                jS.put("score", score);
                jS.put("date", sdf.format(new Date(millis)));
                sb.append(String.format(Locale.getDefault(),
                        "%2d %3s %2d %3d %12s%n", leaderboardPosition++, initials, level, score, sdf.format(new Date(millis))));
                jsonArray.put(jS);
            }
            rs.close();
            stmt.close();
            jsonObject.put("scores", jsonArray);
            return sb.toString();
        } catch (JSONException e) {e.printStackTrace();}
        return null;
    }
    private JSONObject parseResults(String s) throws JSONException {
        String[] list = s.split("\n");
        JSONObject jsonObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();
        for (String entry : list) {
            JSONObject scores = new JSONObject();
            String[] e = entry.split(",");
            scores.put("position", e[0]);
            scores.put("initials", e[1]);
            scores.put("level", e[2]);
            scores.put("score", e[3]);
            scores.put("date", e[4]);
            jsonArray.put(scores);
        }
        jsonObject.put("leaderboard", jsonArray);
        return jsonObject;
    }
 */
}
