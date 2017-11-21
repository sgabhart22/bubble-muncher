package com.sgabhart.periwinkle;

/**
 * Created by Admin on 11/21/2017.
 */
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DownloadService extends JobIntentService {
    static final int JOB_ID = 1000;

    static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, DownloadService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(Intent intent){
        ArrayList<String> words = new ArrayList<>();
        ArrayList<String> answers = new ArrayList<>();
        String url = intent.getStringExtra("url");
        Context cx = this;

        // Get scrambled words first
        try {
            org.jsoup.nodes.Document wordDoc = Jsoup.connect(url).get();
            org.jsoup.select.Elements wordElements = wordDoc.select("button.word");

            // Collect jumbled words
            for (int i = 0; i < 5; i++) {
                words.add(wordElements.get(i).text());
            }

        } catch (IOException e) {
            System.err.println();
        }

        // Now get solved words
        for (String s:
                words) {
            url = new UrlBuilder().getAnswerUrl(s);
            try{
                Document answerDoc = Jsoup.connect(url).get();
                Element answerElement = answerDoc.select("button.word").first();
                answers.add(answerElement.text());
            } catch (IOException e) {
                System.err.println(e);
            }
        }

        // Finally, get cartoon image
        try {
            Document openingDoc = Jsoup.connect(new UrlBuilder().IMAGE_BASE_URL).get();
            Elements linkElements = openingDoc.select("a");
            String cartoonUrl = "No link", srcUrl = "No source";
            Map<String, String> cookies = new HashMap<>();

            for (Element e:
                    linkElements) {
                if(e.text().equals("jumble")){
                    cartoonUrl = e.attr("abs:href");
                    break;
                }
            }

            Document finalDoc = Jsoup.connect(cartoonUrl).get();
            Elements picElements = finalDoc.select("img.img-responsive");

            for (Element e:
                    picElements) {
                if(e.attr("alt").equals("jumble")){
                    srcUrl = e.attr("src");
                    break;
                }
            }

            // Cartoon image as byte array
            byte[] rawBytes = Jsoup.connect(srcUrl).ignoreContentType(true).execute().bodyAsBytes();

            // Crop cartoon
            Bitmap bmp = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.length);
            Bitmap cropped = Bitmap.createBitmap(bmp, 245, 50, 240, 368);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            cropped.compress(Bitmap.CompressFormat.PNG, 100, os);
            byte[] bytes = os.toByteArray();

            // Put information in db
            PuzzleDbHelper dbHelper = new PuzzleDbHelper(cx);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            ContentValues cv = new ContentValues();
            cv.put(PuzzleContract.DailyPuzzle.DATE, new UrlBuilder().getFormattedDate());
            cv.put(PuzzleContract.DailyPuzzle.WORD1, words.get(0));
            cv.put(PuzzleContract.DailyPuzzle.WORD2, words.get(1));
            cv.put(PuzzleContract.DailyPuzzle.WORD3, words.get(2));
            cv.put(PuzzleContract.DailyPuzzle.WORD4, words.get(3));
            cv.put(PuzzleContract.DailyPuzzle.ANSWER1, answers.get(0));
            cv.put(PuzzleContract.DailyPuzzle.ANSWER2, answers.get(1));
            cv.put(PuzzleContract.DailyPuzzle.ANSWER3, answers.get(2));
            cv.put(PuzzleContract.DailyPuzzle.ANSWER4, answers.get(3));
            cv.put(PuzzleContract.DailyPuzzle.FINAL_WORD, words.get(4));
            cv.put(PuzzleContract.DailyPuzzle.FINAL_ANSWER, answers.get(4));
            cv.put(PuzzleContract.DailyPuzzle.IMAGE, bytes);

            long newRowId = db.insert(PuzzleContract.DailyPuzzle.TABLE_NAME, null, cv);
            System.out.println(newRowId);

        } catch (IOException e){
            System.err.println(e);
        }

        // Let MainActivity know it should update its view
        Intent broadcastIntent = new Intent("download-event");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    } //onHandleWork

    @Override
    public void onDestroy() {
        super.onDestroy();
        toast("Download completed.");
    }

    final Handler mHandler = new Handler();

    void toast(final CharSequence text) {
        mHandler.post(new Runnable() {
            @Override public void run() {
                Toast.makeText(DownloadService.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
