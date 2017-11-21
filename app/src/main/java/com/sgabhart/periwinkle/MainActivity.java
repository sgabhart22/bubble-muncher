package com.sgabhart.periwinkle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private PuzzleDbHelper dbHelper;
    private ScrollView sv;
    private int buttonWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        dbHelper = new PuzzleDbHelper(this);
        sv = (ScrollView)(findViewById(R.id.scrollView));

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        buttonWidth = size.x;
        updateView();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Context cx = MainActivity.this;
                Intent downloadIntent = new Intent();
                downloadIntent.putExtra("url", new UrlBuilder().getWordsUrl());

                if(dbHelper.containsPuzzle()) {
                    Toast.makeText(cx, "Today's puzzle already downloaded.", Toast.LENGTH_SHORT).show();
                } else {
                    DownloadService.enqueueWork(cx, downloadIntent);
                    Toast.makeText(MainActivity.this, "Download started...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Listen for DownloadService to signal completion
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
                new IntentFilter("download-event"));
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()) {
            case R.id.action_cleanup:
                dbHelper.onUpgrade(dbHelper.getWritableDatabase(), 1, 1);
                updateView();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void updateView(){
        ArrayList<Puzzle> puzzles = dbHelper.selectAll();
        Log.w("updateView: ", "Puzzles in db " + puzzles.size());
        if(puzzles.size() > 0){
            sv.removeAllViewsInLayout();

            GridLayout grid = new GridLayout(this);
            grid.setRowCount((puzzles.size()));
            grid.setColumnCount(1);

            PuzzleButton[] buttons = new PuzzleButton[puzzles.size()];
            ButtonHandler bh = new ButtonHandler();

            int i = 0;

            for (Puzzle p:
                    puzzles) {
                buttons[i] = new PuzzleButton(this, p);
                buttons[i].setText(p.getId() + "\n" + p.getDate());

                buttons[i].setOnClickListener(bh);

                grid.addView(buttons[i], buttonWidth,
                        GridLayout.LayoutParams.WRAP_CONTENT);
                i++;
            }

            sv.addView(grid);
        } else {
            sv.removeAllViewsInLayout();
        }
    } // updateView

    public void startPuzzle(int id){
        Intent puzzleIntent = new Intent(this, PuzzleActivity.class);
        puzzleIntent.putExtra("id", id);
        startActivity(puzzleIntent);
    }

    private  class ButtonHandler implements View.OnClickListener{
        public void onClick(View v){
            int id = ((PuzzleButton) v).getId();

            // TODO: Start PuzzleActivity here. Put puzzle id in an Intent
            // TODO: and let the receiving activity pull from db.
            startPuzzle(id);
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateView();
        }
    };

    protected void onDestroy(){
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        super.onDestroy();
    }
}
