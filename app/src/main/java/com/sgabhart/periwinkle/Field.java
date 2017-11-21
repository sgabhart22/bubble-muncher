package com.sgabhart.periwinkle;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Admin on 11/12/2017.
 */

public class Field implements Serializable {

    /* TODO: Move all Paints and Graphics to a renderer.
        Great start for housing data structures, but this object needs to communicate
        with a dedicated graphics manipulation class. The Field class should ideally
        serve as an intermediary between the PuzzleView and the renderer.
     */

    private Puzzle puzzle;
    private ArrayList<Rect> labelRects, answerRects, finalRects;
    private Position selected = new Position(0, 0);
    private Box[][] boxes;
    private Box[] finalBoxes;
    private String responder;
    private HashMap<String, ArrayList<Integer>> map = new HashMap<>();
    private int width, height, sideLength, leftMargin;
    private Paint labelPaint, boxPaint, circlePaint, textPaint, selectedPaint;
    private Rect imageRect, background;
    private Bitmap cartoon;
    private Bitmap bitmap;

    // Dummy words/answers
    ArrayList<String> words = new ArrayList<>();
    // String[] tempWords = {"PHECR", "BLAFE", "RASPIN", "VIRTHE", "PEBESRNTE"};
    ArrayList<String> answers = new ArrayList<>();
    // String[] tempAnswers = {"PERCH", "FABLE", "SPRAIN", "THRIVE", "BE PRESENT"};
    // String testFormatAnswer = "\"HILL-BILLIES\"";

    public Field(Puzzle puzzle, int width, int height){
        this.puzzle = puzzle;
        selected = new Position(0, 0);

        this.width = width;
        this.height = height;
        leftMargin = (int)(width * .1);

        map = puzzle.getMap();
        boxes = puzzle.getBoxes();

        words = puzzle.getWords();
        answers = puzzle.getAnswers();

        // Create Paint for jumbled word containers
        labelPaint = new Paint();
        labelPaint.setColor(Color.GRAY);
        labelPaint.setAntiAlias(true);
        labelPaint.setStrokeWidth(5.0f);
        labelPaint.setStyle(Paint.Style.STROKE);

        // Create Paint for answer boxes
        boxPaint = new Paint();
        boxPaint.setColor(Color.BLACK);
        boxPaint.setAntiAlias(true);
        boxPaint.setStrokeWidth(5.0f);
        boxPaint.setStyle(Paint.Style.STROKE);

        // Create Paint for highlighted letter
        selectedPaint = new Paint();
        selectedPaint.setColor(Color.parseColor("#FFAE57"));

        // Create a Paint for circled boxes
        circlePaint = new Paint();
        circlePaint.setColor(Color.YELLOW);
        circlePaint.setAntiAlias(true);
        circlePaint.setStrokeWidth(5.0f);
        circlePaint.setStyle(Paint.Style.FILL);

        // Create Paint for lettering
        textPaint = new Paint();
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(50.0f);
        textPaint.setTypeface(Typeface.SANS_SERIF);

        // Instantiate array Rectangles representing word containers
        labelRects = new ArrayList<>();
        double yLabel = .05;
        int startLabelX = leftMargin;
        int endLabelX = (int)(width * .30);

        // Similar, but for answer boxes
        answerRects = new ArrayList<>();
        double yBox = .1;
        int startBox = leftMargin;
        sideLength = (int)(height * .13 - height * .1);
        int endBox = leftMargin + sideLength;

        // Finally, for the final answer boxes
        finalRects = new ArrayList<>();
        int answerLeftMargin = (int)(width * .3);
        double yAnswerBox = .55;

        // Create Rectangle to house cartoon bitmap
        imageRect = new Rect((int)(width * .55), (int)(height * .05), (int)(width * .95), (int)(height * .5));
        byte[] rawImage = puzzle.getImage();
        cartoon = BitmapFactory.decodeByteArray(rawImage, 0, rawImage.length);
        Log.w("From Field constructor", "cartoon config " + cartoon.getConfig());

        // Placeholder Collection to set circled letters
        ArrayList<Integer> circled = new ArrayList<>();

        // Create containers and answer boxes for each word in the puzzle
        for (int i = 0; i < words.size() - 1; i++) {
            labelRects.add(new Rect(startLabelX, (int)(height * yLabel), endLabelX, (int)(height * (yLabel + .03))));

            circled = map.get("Word" + (i + 1));

            String currentWord = answers.get(i);
            for(int j = 0; j < currentWord.length(); j++){
                boxes[i][j] = new Box(currentWord.charAt(j));
                if(circled.contains(j)){
                    boxes[i][j].setCircled(true);
                }
            }


            for (int k = 0; k < boxes[i].length; k++){
                answerRects.add(new Rect(startBox, (int)(height * yBox), endBox, (int)(height * (yBox + .03))));
                startBox += sideLength;
                endBox += sideLength;
            }


            yLabel += .10;
            yBox += .10;
            startBox = leftMargin;
            endBox = startBox + sideLength;
        } // for

        String finalAnswer = answers.get(answers.size() - 1);
        finalBoxes = new Box[finalAnswer.length()];

        for(int i = 0; i < finalAnswer.length(); i++){
            finalBoxes[i] = new Box(finalAnswer.charAt(i));
            finalBoxes[i].setCircled(true);

            finalRects.add(new Rect(answerLeftMargin, (int)(height * yAnswerBox),
                    answerLeftMargin + sideLength, (int)(height * (yAnswerBox + .03))));

            answerLeftMargin += sideLength;
        } // for

    } // Constructor

    public Bitmap draw(){

        if(bitmap == null){
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);

        int i = 0;
        for (Rect r:
                labelRects) {
            canvas.drawRect(r, labelPaint);
            canvas.drawText(words.get(i).toUpperCase(), r.left + 5, r.bottom - 10, textPaint);
            i++;
        }

        // Draw answer boxes from Box[][] boxes
        for(int j = 0; j < boxes.length; j++){
            for(int k = 0; k < boxes[j].length; k++){

                Box b = boxes[j][k];

                if(b != null){
                    Rect r = answerRects.get((j * 6) + k);
                    if(b.isSelected()){
                        canvas.drawRect(r, selectedPaint);
                    }


                    if(b.isCircled()){
                        canvas.drawRect(r, boxPaint);
                        canvas.drawCircle(r.exactCenterX(),
                                r.exactCenterY(), sideLength / 2, boxPaint);
                    }
                    else {
                        canvas.drawRect(r, boxPaint);
                    }
                }
            }
        }

        // Draw final answer boxes
        for(i = 0; i < finalBoxes.length; i++){
            Box b = finalBoxes[i];
            Rect r = finalRects.get(i);

            if(b.isSelected()){
                canvas.drawRect(r, selectedPaint);
            }

            if(b.getSolution() == '"'){
                canvas.drawText("\"", r.exactCenterX(),
                        r.exactCenterY(), textPaint);
            } else if(b.getSolution() == '-'){
                canvas.drawText("-", r.exactCenterX(),
                        r.exactCenterY() + 10.0f, textPaint);
            } else if(b.getSolution() != ' '){
                canvas.drawRect(r, boxPaint);
                canvas.drawCircle(r.exactCenterX(), r.exactCenterY(),
                        sideLength / 2, boxPaint);
            }
        }

        // Draw cartoon
        canvas.drawBitmap(cartoon, null, imageRect, new Paint());

        return bitmap;
    }

    public ArrayList<Rect> getLabelRects() {
        return labelRects;
    }

    public ArrayList<Rect> getAnswerRects() {
        return answerRects;
    }

    public ArrayList<Rect> getFinalRects() {
        return finalRects;
    }

    public Paint getLabelPaint() {
        return labelPaint;
    }

    public Paint getBoxPaint() {
        return boxPaint;
    }

    public Paint getCirclePaint() {
        return circlePaint;
    }

    public Paint getTextPaint() {
        return textPaint;
    }

    public Paint getSelectedPaint() {
        return selectedPaint;
    }

    public Box[] getFinalBoxes() {
        return finalBoxes;
    }

    public Rect getImageRect() {
        return imageRect;
    }

    public Bitmap getCartoon() {
        return cartoon;
    }

    public int getSideLength() {
        return sideLength;
    }

    public void setLabelRects(ArrayList<Rect> newLabelRects){ this.labelRects = newLabelRects; }

    public void setAnswerRects(ArrayList<Rect> newAnswerRects){ this.answerRects = newAnswerRects; }

    public void setFinalRects(ArrayList<Rect> newFinalRects){ this.finalRects = newFinalRects; }

    public Box getCurrentBox() {
        return this.boxes[this.selected.x][this.selected.y];
    }

    public Box[][] getBoxes(){ return boxes; }

    public Word getCurrentWord() {
        Word w = new Word();
        w.start = this.getCurrentWordStart();
        w.length = this.getWordRange();

        return w;
    }

    public Position getCurrentWordStart(){
        return new Position(this.selected.x, 0);
    }

    public int getWordRange(Position start){
        Box[] wordBoxes = this.getBoxes()[start.x];
        int range = 0;

        for(Box b: wordBoxes){
            if(b != null) range++;
        }

        return range;
    }

    public int getWordRange() {
        return getWordRange(this.getCurrentWordStart());
    }

    public Position getSelected(){ return selected; }

    public void setSelected(Position selected){ this.selected = selected; }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public String getResponder() {
        return responder;
    }

    public Puzzle getPuzzle(){ return  puzzle; }


    public static class Position implements Serializable {
        public int x; // Word #
        public int y; // Letter #

        protected Position(){
        }

        public Position(int x, int y) {
            this.y = y;
            this.x = x;
        }


        @Override
        public boolean equals(Object o) {
            if ((o == null) || (o.getClass() != this.getClass())) {
                return false;
            }

            Position p = (Position) o;

            return ((p.y == this.y) && (p.x == this.x));
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(new int[] {x, y});
        }

        @Override
        public String toString() {
            return "[" + this.x + " x " + this.y + "]";
        }
    } // Position

    public static class Word implements Serializable {
        public Position start;
        public int length;

        public boolean checkInWord(int x) {
            int ranging = x;
            int startPos = start.x;

            return (startPos <= ranging && ((startPos + length) > ranging));
        }

        @Override
        public boolean equals(Object o) {
            if (o.getClass() != Word.class) {
                return false;
            }

            Word check = (Word) o;

            return check.start.equals(this.start) && (check.length == this.length);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = (29 * hash) + ((this.start != null) ? this.start.hashCode() : 0);
            hash = (29 * hash) + this.length;

            return hash;
        }
    }
}