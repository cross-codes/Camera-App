package com.github.cross;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class CameraInitializer extends Application {

  // Default image and font storage location
  public static String imageLocation = "/home/cross/Pictures/Camera/";
  public static String fontLocation = "/fonts/JuliaMono-Regular.ttf";

  private Font juliaMono = Font.loadFont(getClass().getResourceAsStream(fontLocation), 12);;

  public static void main(String[] args) {
    launch(args);
  }

  @Override
  public void start(Stage primaryStage) {
    WebcamController controller = new WebcamController(imageLocation);
    StageDesigner designer = new StageDesigner(primaryStage, juliaMono, controller);
    Platform.runLater(() -> designer.setImageViewSize());
  }
}
