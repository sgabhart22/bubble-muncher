package com.sgabhart.periwinkle;

import android.content.Intent;
import android.graphics.Point;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

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

        View.OnKeyListener onKeyListener = new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                return event.getAction() == KeyEvent.ACTION_UP;
            }
        };

        kb = new Keyboard(this, R.xml.keyboard);
        kbView = (KeyboardView)(findViewById(R.id.keyboard));
        kbView.setOnKeyListener(onKeyListener);
        kbView.setOnKeyboardActionListener(new KeyboardView.OnKeyboardActionListener() {
            @Override
            public void onPress(int primaryCode) {}

            @Override
            public void onRelease(int primaryCode) {}

            @Override
            public void onKey(int primaryCode, int[] keyCodes) {
                KeyEvent event = new KeyEvent(KeyEvent.ACTION_UP, primaryCode);
                PuzzleActivity.this.onKeyUp(primaryCode, event);
            }

            @Override
            public void onText(CharSequence text) {}

            @Override
            public void swipeLeft() {}

            @Override
            public void swipeRight() {}

            @Override
            public void swipeDown() {}

            @Override
            public void swipeUp() {}
        });

        kbView.setKeyboard(kb);

        fieldView.setBitmap(field.draw());
    }


    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
        char c = Character.toUpperCase((char)(keyCode));
        Log.w("PuzzleActivity onKeyUp", "Key pressed: " + c);

        Field.Position selected = field.getSelected();

        if(selected.x == 7){
            field.getFinalBoxes()[selected.y].setResponse(c);
        } else {
            field.getBoxes()[selected.x][selected.y].setResponse(c);
        }

        field.advance();
        fieldView.setBitmap(field.draw());

        return  super.onKeyUp(keyCode, event);
    }

}
