package com.company;

import com.jsyn.unitgen.SawtoothOscillatorBL;
import javafx.animation.AnimationTimer;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.CheckBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


import javax.imageio.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**Main controller class for the application
 *
 */

public class MainController {


    /**
     * Image of a clef
     */
    Image clef = new Image(new File("./data/clef 50p.png").toURI().toString());


    /*
     * Row variable. Keeps track of rows.
     */
    private int row = 0;

    /*
     * Offset is used to determine how much we need to set the amount of distance in which we have to go out of line in relation to the previous ROW on the y axis.
     */
    private int offset = 0;
    /*
     * xPos is used to store which position we are at on the x-axis.
     */

    /**
     * Total notes played variable. Used to place notes correctly on the x-axis.
     */

    private int xTotal =0;

    /**this is for the sample recorder object which is used to record .wav files.
     *
     */
    private SampleRecorder recorder = new SampleRecorder();

    /**this hashmap is used to connect musical notation of scales ie. C3 to their respective frequencies, for use in the scalegenerator afterwards.
     *
     */
    private FrequencyHashMap frequencyMap = new FrequencyHashMap();

    /**this variable is the play counter, it will go up for every note that should be played.
     *
     */
    private int counter = 0;
    /**This is the initial scale that gets played
     *
     */
    private String inputRootNote;

    /**scaletype
     *
     */

    private String scaleType;
    /**set all the lengths of all scales being generated with one variable
     *
     */
    private int scaleLengths = 13;

    /**Instance of the oscillator we play from, from the JSYN API
     *
     */
    private SoundClass osc;

    /**instancing class variable MajorChromaticScale, with null.
     *
     */
    private MajorScale majorScala;
    /**instancing class variable MajorChromaticScale, with null.
     *
     */
    private MinorScale minorScala;
    /**instancing class variable HarmonicMinorChromaticScale, with null.
     *
     */
    private HarmonicMinorScale harmonicMinorScale;

    //booleans that the user can manipulate through the GUI, which are input parameters for the program.

    /**
     * If we are playing right now.
     */
    private boolean isPlaying = false;
    /**
     * If we are using a major scale.
     */
    private boolean isMajor;
    /**
     * If we are using a minor scale.
     */
    private boolean isMinor;
    /**
     * If we are using a harmonic minor scale.
     */
    private boolean isHarmonicMinor;
    /**
     * Mute button. Can be useful when debugging.
     */
    private boolean isMuted;
    /**
     * A clear method that clears the canvas using the clearRect() method.
     */
    private boolean toClear;
    /**
     * Boolean to notify that we are recording.
     */
    private boolean isRecording;

    /**the root note, we need to know the root note as all other notes in a scale relate to this. This is used to place the notes on the XY axis.
     *
     */
    private double rootNote;

    /**rhythm complexity
     *
     */
    private String complexity = "medium complexity";

    /**arraylist of notes to be played.
     *
     */
    private ArrayList<Note> notes = new ArrayList<>();

    @FXML
    CheckBox mute;

    @FXML
    ChoiceBox<String> choiceBox;

    @FXML
    ChoiceBox<String> choiceBox1;

    @FXML
    Canvas canvas;
    @FXML
    TextFlow textFlow;

    @FXML
    TextField textField;

    @FXML
    TextField writeField;

    /**the play button is where the main programme is launched, and melody is supposed to play when it is clicked. This happens when the boolean isPlaying is set to true
     * There is a switch case that depends on which scale the user chooses to play from. The correct scale will then be generated, dependant on the root note that the user has specified.
     **/


    @FXML
    private void playButton(){

//osc.lineOut.start();
        osc.synth.start();
        inputRootNote = textField.getText().toUpperCase();
        complexity = choiceBox.getSelectionModel().getSelectedItem();
        scaleType = choiceBox1.getSelectionModel().getSelectedItem();

        switch (scaleType){
            default :
            case "major scale":
                majorScala = new MajorScale(scaleLengths, frequencyMap.frequencyFinder(inputRootNote));
                rootNote = frequencyMap.noteFinder(majorScala.getScale().get(0));
                isMajor = true;
                isMinor = false;
                isHarmonicMinor = false;
                isPlaying = true;
                break;
            case "minor scale":
                //   inputRootNote = textField.getText();
                complexity = choiceBox.getSelectionModel().getSelectedItem();
                minorScala = new MinorScale(scaleLengths, frequencyMap.frequencyFinder(inputRootNote));
                rootNote = frequencyMap.noteFinder(minorScala.getScale().get(0));
                isMinor = true;
                isMajor = false;
                isHarmonicMinor = false;
                isPlaying = true;
                break;
            case "harmonic minor scale":
                // inputRootNote = textField.getText();
                complexity = choiceBox.getSelectionModel().getSelectedItem();
                harmonicMinorScale = new HarmonicMinorScale(scaleLengths, frequencyMap.frequencyFinder(inputRootNote));
                rootNote = frequencyMap.noteFinder(harmonicMinorScale.getScale().get(0));
                isHarmonicMinor = true;
                isMajor = false;
                isMinor = false;
                isPlaying = true;
                break;
        }

        if (isRecording) {
            recorder.startRecording(osc);
        }

        if (!osc.synth.isRunning()) {
            osc.synth.start();
        }

    }

    /**
     * This function stops everything that is playing. It also makes sure to stop any audio that is being recorded if it is happening.
     *
     * @throws IOException
     * ttt
     */

    @FXML

    public void stopButton() throws IOException {

        isPlaying = false;
        // osc.lineOut.stop();
        if (isRecording) {
            recorder.pauseRecording(osc);
        }
        osc.synth.stop();
    }

    /**
     * This function resets everything that needs to be reset and stops the oscillators, essentially resetting the program so the user can play something new.
     * @throws IOException
     * Exception needed for recording.
     */

    @FXML
    public void resetButton() throws IOException {
        isPlaying = false;
        osc.lineOut.stop();
        if (isRecording) {
            isRecording = false;
            recorder.stopRecording(osc);
        }
        counter = 0;
        toClear = true;
        xTotal = 0;
        row = 0;
        offset = 0;

    }

    /**
     * This function allows the user to write new data in textformat to the file. It also runs the RESET function.
     * @throws IOException
     * Exception needed for recording.
     */
    @FXML
    public void refresh() throws IOException {


        try {
            FileWriter writer = new FileWriter(".idea/data");
            BufferedWriter buffer = new BufferedWriter(writer);
            buffer.write(writeField.getText());
            buffer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        osc.refreshFileReader();
    }


    /**
     * This function simply takes a screenshot of the canvas. Which is handy if the user wants to print out the sheets on physical paper for playing on physical instruments as well.
     */

    @FXML
    public void pictureBtn() {

        try {
            WritableImage snapshot = new WritableImage(660,546);
            WritableImage snapshot2 = canvas.getScene().snapshot(snapshot);

            File picFile = new File("./data/canvasPicture.png");
            ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", picFile);

            File outputFile;
            FileChooser fileChooser = new FileChooser();
            try {
                String userDir = System.getProperty("user.home");
                fileChooser.setInitialDirectory(new File(userDir + "/Desktop"));
            } catch (IllegalArgumentException e) {
                System.out.println("Could not find Desktop folder");
            }
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG","*.png"));
            outputFile = fileChooser.showSaveDialog(new Stage());

            if (picFile != null) {
                try {
                    System.out.println("Picture saved as: " + outputFile.getName());
                    System.out.println("Picture saved to: " + outputFile.getAbsolutePath());
                    picFile.renameTo(outputFile);
                } catch (NullPointerException e) {
                    System.out.println("The picture was not saved");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * This function is a button for recording audio.
     * @throws IOException
     * test
     */

    @FXML
    public void recBtn() throws IOException {
        recorder.initRecording(osc);
        isRecording = true;

        if (isPlaying) {
            recorder.startRecording(osc);
        }
    }

    /**
     * This method is what gets run first time javafx is started.
     */

    @FXML
    public void initialize() {
        GraphicsContext g = canvas.getGraphicsContext2D();

        g.setFill(Color.GREY);
        osc = new SoundClass();
        osc.OscSetup(new SawtoothOscillatorBL());

        // Start and control game loop
        new AnimationTimer() {
            long lastUpdate;

            public void handle(long now) {

                if (now > lastUpdate + 10 * 1000000) {
                    lastUpdate = now;
                    drawCanvas();
                }
            }
        }.start();

    }

    /**
     * This is the draw function where all graphics are handled. It is run 30 times every second.
     */


    private void drawCanvas() {
        int randomness= 0;
        if (complexity.equals("medium complexity"))
        {
            randomness = 30;
        }
        if (complexity.equals("high complexity"))
        {
            randomness = 60;
        }
        GraphicsContext g = canvas.getGraphicsContext2D();

        /**
         * Gets ready to clear the canvas.
         */
        if (toClear) {
            g.clearRect(0, 0, 1000, 1000);
            toClear = false;
        }

/*        //clears note paper to make room for new notes
        if (row == 5) {
            toClear = true;
            xTotal = 0;
            row = 0;
            offset = 0;
        }
*/

        /**if the button major or minor are pressed
         *
         */
        if (isPlaying) {
            complexity = choiceBox.getSelectionModel().getSelectedItem();

            isMuted= mute.isSelected();

            //this triggers a noteplay event every time we run through the draw method. Which one that is triggered depends on which scale we are playing from. Major, minor... etc.
            if (counter < osc.noteList.size()-1) {

                if (isMajor) {
                    osc.PlayLoop(majorScala.getScale(),isMuted,0.1,1,randomness,counter);
                    notes.add(new Note(majorScala.getScale(), counter, complexity));
                }
                if(isMinor) {
                    osc.PlayLoop(minorScala.getScale(),isMuted,0.1,1,randomness,counter);
                    notes.add(new Note(minorScala.getScale(), counter, complexity));

                }
                if(isHarmonicMinor){
                    osc.PlayLoop(harmonicMinorScale.getScale(),isMuted,0.1,1,randomness,counter);
                    notes.add(new Note(harmonicMinorScale.getScale(), counter, complexity));

                }

            } else {
                counter=0;
                isPlaying = false;
                osc.synth.stop();
            }
            /* We do this because we need to get the exact note that we are playing. The way the programme is built the maincontroller doesn't know exactly what notes we have to play. Thats taken care off in the oscgenerator.java. This is in relation to the root note.*/
            g.setFill(Color.RED);

            /* We do this because we need to get the exact note that we are playing. The way the programme is built the maincontroller doesn't know exactly what notes we have to play. Thats taken care off in the oscgenerator.java. This is in relation to the root note.*/
            int playingNoteNum = osc.getPlayingNoteNum(counter);
            //Actually draws the sheet and notes! Handles the visual part.
            drawSheet(g, playingNoteNum);
            //Actually draws the sheet and notes! Handles the visual part.
            counter++;

        }
    }

    /**
     * This function draws the sheet which holds all the notes. Its taking part of all the visuals on the canvas.
     * @param g
     * graphicscontext g parameter, used for canvas.
     * @param playingNoteNum
     * the notes that are playing and which need to be drawn, in relation to their root note.
     */

    private void drawSheet(GraphicsContext g, int playingNoteNum) {

        g.setFill(Color.BLACK);

        //rhythmvalue the rhyth values have to comply with the music theoretical halving-princippe that note lenghts are defined by. We chose 32, as
        //the style seemed to fit. But if we wanted less space between the notes we could have chosen 16 pixels distance for the quarter notes, gone down from there.
        int space=0;
        if(osc.getRhythmValue().equals("quarter")) {
            space = 32;
            g.setFill(Color.GREEN);
        }

        if(osc.getRhythmValue().equals("eight")) {
            space = 16;
            g.setFill(Color.BLUE);

        }

        if(osc.getRhythmValue().equals("sixteenth")) {
            space = 8;
            g.setFill(Color.RED);
        }

        //en takt er 64 pixels lang
        //taktart


        /* If we get above 512-space, just so we don't go out of bounds with the last note. (128*5)  we need to switch to the second row. etc. */
        //row switcher
        if (xTotal > 512-space) {
            row++;
            offset = 100;
            xTotal=0;
        }

        /*
         * Setting the xPosition. In relation to the offset and the row. Each row we get 60 pixels too far to the right in relation to the sheet so we need to correct for that by subtracting xOffset with the row.
         */

        //This is where the note objects get created.
        // X positions are set first. 10 is the amount of space between each note. 40 is the start position of the first note.
        notes.get(this.counter).setxPos(xTotal +128);
        xTotal = xTotal + space;

        //Y positions are set next. They are a bit more complex because the notes need to be able to be placed correctly on the sheets no matter which scale we are playing in.
        notes.get(this.counter).setyPos(215 - rootNote - playingNoteNum * 5 + row * offset);

        //This is where the note objects get drawn.
        double getyPos = notes.get(this.counter).getyPos();
        double getxPos = notes.get(this.counter).getxPos();

        g.fillOval(getxPos, getyPos, 6, 6);
        g.fillRect(getxPos, getyPos - 12, 2, 15);
        g.fillRect(getxPos, getyPos - 14, 10, 1);

        g.setFill(Color.BLACK);

        //line under notes too high up bylines // help lines

        if(getyPos >30+offset*row&& getyPos <70+offset*row) {
            g.fillRect(getxPos - 3, 66 + offset * row, 12, 2);
            if(getyPos <58+ offset * row){
                g.fillRect(getxPos - 3, 56+offset*row, 12, 2);
            }
        }
        //line under notes too far down
        if(getyPos >118+offset*row&& getyPos <150+offset*row) {
            g.fillRect(getxPos - 3, 124+offset*row, 12, 2);
            if(getyPos >130+offset*row){
                g.fillRect(getxPos - 3, 134+offset*row, 12, 2);
            }

        }

        // G-clef for the user to read placement of notes
        g.drawImage(clef,1,65 + row * offset);

        //Visual implementation of the time signature
        Font font = new Font("Arial", 25);
        Font font2 = new Font("Arial", 12);

        g.setFont(font);
        g.fillText("4 " , 40, 95);
        g.fillText("4 " , 40, 115);

        g.setFont(font2);

        //Note paper. Its simply some lines! 5 of them. But they move down which each row.
        for (int i = 0; i <5 ; i++) {
            g.fillRect(0, 75 + row * offset+i*10, 640, 1);
        }


        //line seperator, every measure is 128 pixels wide.

        for (int i = 0; i <6 ; i++) {

            g.fillRect(i*128, 75 + row * offset, 3, 41);

        }

        //beatcounter
        g.clearRect(0, 0, 150, 50);
        g.fillText("Notes played: " + Integer.toString(this.counter), 5, 15);
        g.fillText( inputRootNote + " "+ scaleType, 5, 30);

        g.setFill(Color.RED);

        //keySignatures
        keySignatures(g, row, offset);

    }

    /**
     * This is a private function that only makes sense to be used in conjunction with the sheet function.
     * @param g
     * Graphicscontext g
     * @param row
     * The row parameter is also needed to place the keySignatures on every row correctly.
     * @param offset
     * Offset on the y axis.
     * All the key signatures need to be accounted for. These are determined by their flats and keySignatures.
     * A description can be found here.
     * http://musictheoryfundamentals.com/MusicTheory/keySignatures.php
     */


    private void keySignatures(GraphicsContext g, int row, int offset) {
        //If we are playing in Major we will get the following keySignatures. We also reuse the user chose of key from earlier by doing a regex function on the textfield, to see what scale we are playing.
        if (isMajor) {
            //holds all the sharps/flats
            ArrayList<KeySignature> keySignatures = new ArrayList<>();

            //øverste linje = 0, for hver 10 man går op går man en linje ned.
            if (inputRootNote.contains("A")) {
                keySignatures.add(new KeySignature(g, row, offset, 15, -5));
                keySignatures.add(new KeySignature(g, row, offset, 25, 0));
                keySignatures.add(new KeySignature(g, row, offset, 0, 15));
            }

            if (inputRootNote.contains("B")) {
                keySignatures.add(new KeySignature(g, row, offset, 15, -5));
                keySignatures.add(new KeySignature(g, row, offset, 0, 0));
                keySignatures.add(new KeySignature(g, row, offset, 25, 10));
                keySignatures.add(new KeySignature(g, row, offset, 11, 15));
                keySignatures.add(new KeySignature(g, row, offset, 35, 25));
            }

            if (inputRootNote.contains("D")) {
                keySignatures.add(new KeySignature(g, row, offset, 15, 0));
                keySignatures.add(new KeySignature(g, row, offset, 0, 15));
            }

            if (inputRootNote.contains("E")) {
                keySignatures.add(new KeySignature(g, row, offset, 15, -5));
                keySignatures.add(new KeySignature(g, row, offset, 0, 0));
                keySignatures.add(new KeySignature(g, row, offset, 25, 10));
                keySignatures.add(new KeySignature(g, row, offset, 11, 15));
            }

            if (inputRootNote.contains("F")) {
                keySignatures.add(new KeySignature(g, row, offset, 0, 20, true));
            }
            if (inputRootNote.contains("G")) {
                keySignatures.add(new KeySignature(g, row, offset, 0, 0));
            }
            for (KeySignature keySignature : keySignatures) {
                keySignature.invoke();
            }
//we clear it afterwards so it doesn't get unessecarily long.
            keySignatures.clear();
        }
//minor
        if (isMinor||isHarmonicMinor) {
            ArrayList<KeySignature> keySignatures = new ArrayList<>();

            if (inputRootNote.contains("B")) {

                keySignatures.add(new KeySignature(g, row, offset, 0, 0));
                keySignatures.add(new KeySignature(g, row, offset, 0, 15));
            }

            if (inputRootNote.contains("C")) {

                keySignatures.add(new KeySignature(g, row, offset, 5, 5,true));
                keySignatures.add(new KeySignature(g, row, offset, 0, 20,true));
                keySignatures.add(new KeySignature(g, row, offset, 10, 25,true));
            }

            if (inputRootNote.contains("D")) {

                keySignatures.add(new KeySignature(g, row, offset, 0, 20,true));
            }

            if (inputRootNote.contains("E")) {

                keySignatures.add(new KeySignature(g, row, offset, 0, 0));
            }

            if (inputRootNote.contains("F")) {
                keySignatures.add(new KeySignature(g, row, offset, 5, 5,true));
                keySignatures.add(new KeySignature(g, row, offset, 15, 10,true));
                keySignatures.add(new KeySignature(g, row, offset, 0, 25,true));
                keySignatures.add(new KeySignature(g, row, offset, 10, 30,true));
            }
            if (inputRootNote.contains("G")) {

                keySignatures.add(new KeySignature(g, row, offset, 5, 5,true));
                keySignatures.add(new KeySignature(g, row, offset, 0, 20,true));
            }

            for (KeySignature keySignature : keySignatures) {
                keySignature.invoke();
            }
            keySignatures.clear();
            //we clear it afterwards so it doesn't get unessecarily long.
        }
    }




    /**
     * This is a private class of the private function that is wholly dependent on the private class, keySignatures. A sharp, is thus an object of the type; sharp, that is added to the list of keySignatures.
     */

    private class KeySignature {
        private GraphicsContext g;
        private int row;
        private int offset;
        private int startX;
        private int startY;
        private boolean flat; //if its a flat....

        public KeySignature(GraphicsContext g, int row, int offset, int startX, int startY) {
            this.g = g;
            this.row = row;
            this.offset = offset;
            this.startX = startX;
            //This is simply to make placing the keySignatures easier. So the first line is 0. The next is 10, next 20, next 30. Makes placing the keySignatures and flats mentally much easier.
            this.startY = startY+72;
        }

        public KeySignature(GraphicsContext g, int row, int offset, int startX, int startY, boolean flat) {
            this.g = g;
            this.row = row;
            this.offset = offset;
            this.startX = startX;
            this.startY = startY+72;
            this.flat = flat;

        }



        public void invoke() {
            //visual approximation of a sharp sign. (#)
            if(!flat) {
                g.fillRect(startX + 2, startY + row * offset, 16, 2);
                g.fillRect(startX + 2, startY + row * offset + 5, 16, 2);
                g.fillRect(startX + 5 + 2, startY + row * offset - 4, 2, 17);
                g.fillRect(startX + 10 + 2, startY + row * offset - 4, 2, 17);
            }
            //visual approximation of a flat sign. (b)
            if(flat){
                g.fillRect(startX , startY + row * offset-13, 2, 18);
                g.fillRect(startX + 2, startY + row * offset , 6, 6);
            }
        }

    }

}
