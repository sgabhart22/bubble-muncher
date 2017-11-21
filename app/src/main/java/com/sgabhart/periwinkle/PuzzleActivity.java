package com.sgabhart.periwinkle;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Admin on 11/21/2017.
 */

public class PuzzleActivity extends AppCompatActivity {

    int width, height, puzzleId;
    Point size;
    Puzzle puzzle;
    PuzzleDbHelper dbHelper;
    Field field;
    ScrollingImageView fieldView;
    Keyboard kb;
    KeyboardView kbView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puzzle);

        Intent intent = getIntent();
        puzzleId = intent.getIntExtra("id", 0);

        dbHelper = new PuzzleDbHelper(this);
        puzzle = dbHelper.selectById(puzzleId);

        size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        width = size.x;
        height = size.y;

        field = new Field(puzzle, width, height);
        fieldView = (ScrollingImageView)(findViewById(R.id.field));

        kb = new Keyboard(this, R.xml.keyboard);
        kbView = (KeyboardView)(findViewById(R.id.keyboard));

        kbView.setKeyboard(kb);

        fieldView.setBitmap(field.draw());
    }


}
