package com.example.hw4_denfece_commander;

import android.os.AsyncTask;
import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseTopScoreAsyncTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "DatabaseTopScoreAsyncTa";
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static String dbURL;
    private static final String TABLE = "AppScores";
    private static final String USERNAME = "chri5558_student";
    private static final String PASSWORD = "ABC.123";
    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm", Locale.getDefault());
    private MainActivity context;
    private Connection conn;
    private List<Integer> scoreList = new ArrayList<>();

    public DatabaseTopScoreAsyncTask(MainActivity mainActivity) {
        this.context = mainActivity;
        dbURL = "jdbc:mysql://christopherhield.com:3306/chri5558_missile_defense";
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d(TAG, "onPostExecute: "+s);
        context.setLeaderboard(s,scoreList);
    }

    @Override
    protected String doInBackground(String... strings) {
        Log.d(TAG, "doInBackground: ");
        try {
            Class.forName(JDBC_DRIVER);

            conn = DriverManager.getConnection(dbURL, USERNAME, PASSWORD);
            StringBuilder sb = new StringBuilder();
            sb.append(getScores());
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
            scoreList.add(score);
            int level = rs.getInt(4);
            Log.d(TAG, "getScores: Level: "+level+" Score: "+score+" init: "+initials+" Date: "+millis);
            sb.append(String.format(Locale.getDefault(),
                    "%1$-10d\t%2$-10s\t%3$-10d\t%4$-10d\t%5$-15s%n", leaderboardPosition++, initials, level, score, sdf.format(new Date(millis))));
        }
        rs.close();
        stmt.close();
        return sb.toString();
    }
}
