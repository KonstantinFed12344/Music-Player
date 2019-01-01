/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package musicplayer;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.media.*;
import javafx.scene.control.*;
import javafx.util.Duration;

/**
 *
 * @author Konstantin
 */
public class MusicPlayerController implements Initializable {

    @FXML
    private Button Play;
    @FXML
    private Button Stop;
    @FXML
    private Button Pause;
    @FXML
    private Label title;
    @FXML
    private ListView<String> musicList;
    @FXML
    private ProgressBar bar;
    @FXML
    private Slider slider;
    @FXML
    private ChoiceBox playList;
    @FXML
    private Button plusVolume;
    @FXML
    private Button minusVolume;
    @FXML
    private Label time;
    @FXML
    private ChoiceBox add;

    private MediaPlayer mediaPlayer;
    private Media hit;
    private String sound;
    private ArrayList<String> music;
    private String setting;
    private double duration;
    private ScheduledExecutorService executorService;
    private boolean start;
    private double minutes;
    private double seconds;
    private double currentMinutes;
    private double currentSeconds;
    private boolean sliderMoving;
    private double volume;

    @FXML
    private void handleButtonAction(ActionEvent event) {
        if (event.getSource().equals(Play) && !start && musicList.getSelectionModel().getSelectedItems().get(0) != null) {
            this.play();

            start = true;

        } else if (event.getSource().equals(Play) && !sound.equals(musicList.getSelectionModel().getSelectedItems().get(0))) {
            mediaPlayer.stop();
            executorService.shutdown();
            this.play();
            bar.setProgress(0);

        } else if (event.getSource().equals(Play)) {
            title.setText(sound);
            mediaPlayer.play();
            setting = "Play";
        }
        if (event.getSource().equals(Stop)) {
            mediaPlayer.stop();
            setting = "Stop";
            bar.setProgress(0);
        }
        if (event.getSource().equals(Pause)) {
            mediaPlayer.pause();
            setting = "Pause";
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        new File("C:/Sound").mkdir();// Creates Sound folder in C: drive if it doesn't exist
        new File("C:/Sound/playlists").mkdir();
        music = new ArrayList<>();
        this.updateMusicBank();
        sound = music.get(0);
        setting = "Stop";
        start = false;
        sliderMoving = false;
        volume = 0.5;
        time.setText("---/---");
        playList.getItems().remove(0, 3);
        playList.getItems().addAll("All", "RecentlyAdded");
        hit = new Media(new File("C:/Sound/" + sound).toURI().toString());
        mediaPlayer = new MediaPlayer(hit);
    }

    private void updateMusicBank() {

        File soundFolder = new File("c:/sound");
        File[] listOfFiles = soundFolder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {

            if (!music.contains(listOfFiles[i].getName()) && this.isMP3(listOfFiles[i].getName())) {
                music.add(listOfFiles[i].getName());

                musicList.getItems().add(listOfFiles[i].getName());
            }
        }
    }

    private void progressBar() {
        switch (setting) {
            case "Play":
                if (duration != 0.0) {

                    bar.setProgress(mediaPlayer.getCurrentTime().toSeconds() / duration);
                    slider.setValue((mediaPlayer.getCurrentTime().toSeconds() / duration) * 100);

                }
                break;

            case "Stop":

                break;

            case "Pause":

                break;

        }

        System.out.println(duration + " " + mediaPlayer.getCurrentTime().toSeconds() + " " + slider.getValue() + " " + mediaPlayer.getVolume());

    }
    
    /**
     *When play button is pressed for a new song, file is loaded into media player
     *The counter is updated to display the new play length
     */
    private void play() {
        sound = musicList.getSelectionModel().getSelectedItems().get(0);
        hit = new Media(new File("C:/Sound/" + sound).toURI().toString());
        mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
        setting = "Play";

        mediaPlayer.setOnReady(new Runnable() {
            @Override
            public void run() {

                duration = mediaPlayer.getCycleDuration().toSeconds();
                minutes = duration / 60;
                seconds = duration - Math.floor(minutes) * 60;
                mediaPlayer.setVolume(volume);
                if ((int) Math.floor(seconds) < 10) {
                    time.setText("---/" + (int) Math.floor(minutes) + ":0" + (int) Math.floor(seconds));
                } else {
                    time.setText("---/" + (int) Math.floor(minutes) + ":" + (int) Math.floor(seconds));
                }

                slider.setOnMousePressed(slide -> {
                    sliderMoving = true;
                });

                slider.setOnMouseReleased(slide -> {
                    mediaPlayer.pause();
                    mediaPlayer.setStartTime(Duration.seconds((slider.getValue() / 100) * duration));
                    mediaPlayer.play();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            time();
                        }

                    });
                    sliderMoving = false;
                });
            }
        });

        executorService = Executors.newSingleThreadScheduledExecutor();

        executorService.scheduleAtFixedRate(new Runnable() { //Updates the progress bar and the indicator showing remaining time every quarter second
            @Override
            public void run() {
                if (!sliderMoving) {
                    progressBar();
                }
                updateMusicBank(); //This checks the location of the folder where music is stored for new files, and adds them to playable list
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        time();
                    }

                });
            }
        }, 0, 250, TimeUnit.MILLISECONDS);

        title.setText(sound);
    }

    /**
     *Determines if the file is of type mp3 
     * Will be expanded to include other audio file types
     */
    private boolean isMP3(String file) {
        String fileType = "";
        fileType += file.charAt(file.length() - 4);
        fileType += file.charAt(file.length() - 3);
        fileType += file.charAt(file.length() - 2);
        fileType += file.charAt(file.length() - 1);

        if (fileType.equals(".mp3")) {
            return true;
        }

        return false;
    }
    
    /**
     *Updates the counter showing current progress of play time and remaining time
     *
     */
    private void time() {
        currentMinutes = mediaPlayer.getCurrentTime().toSeconds() / 60;
        currentSeconds = mediaPlayer.getCurrentTime().toSeconds() - Math.floor(currentMinutes) * 60;
        String currentTimeSecs = "" + (int) Math.floor(currentSeconds);
        String timeSecs = "" + (int) Math.floor(seconds);
        if ((int) Math.floor(currentSeconds) < 10) {
            currentTimeSecs = "0" + (int) Math.floor(currentSeconds);
        }
        if ((int) Math.floor(seconds) < 10) {
            timeSecs = "0" + (int) Math.floor(seconds);
        }
        time.setText((int) Math.floor(currentMinutes) + ":" + currentTimeSecs
                + "/" + (int) Math.floor(minutes) + ":" + timeSecs);
    }

    @FXML
    private void volumeAction(ActionEvent event) {//Controls the volume when buttons pressed on GUI
        if (event.getSource().equals(plusVolume)) {
            if (mediaPlayer.getVolume() >= 1.0) {
                mediaPlayer.setVolume(volume = 1.0);
                
            } else {
                mediaPlayer.setVolume(volume = mediaPlayer.getVolume() + 0.1);
            }
        }
        if (event.getSource().equals(minusVolume)) {
            if (mediaPlayer.getVolume() <= 0) {
                mediaPlayer.setVolume(volume = 0);
            } else {
                mediaPlayer.setVolume(volume = mediaPlayer.getVolume() - 0.1);
            }
        }
    }
}
