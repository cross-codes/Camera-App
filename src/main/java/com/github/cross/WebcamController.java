package com.github.cross;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;

import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;

public class WebcamController {

  private Webcam webcam;
  private boolean stop;
  private ObjectProperty<Image> imageProperty;
  protected String imageLocation;

  protected Button pictureButton;
  protected Button resumeButton;
  protected Button exitButton;

  public WebcamController(String imageLocation) {
    this.webcam = null;
    this.stop = false;
    this.imageProperty = new SimpleObjectProperty<Image>();
    this.imageLocation = imageLocation;
    this.pictureButton = new Button();
    this.resumeButton = new Button();
    this.exitButton = new Button();
  }

  protected void initializeWebCam(final int webcamIdx, FlowPane bottomControlPane, ImageView capturedImageView) {

    Task<Void> webCamTask = new Task<Void>() {

      @Override
      protected Void call() throws Exception {

        webcam = Webcam.getWebcams().get(webcamIdx);
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcam.open();
        startWebCamStream(capturedImageView);
        return null;
      }
    };

    Thread webCamThread = new Thread(webCamTask);
    webCamThread.setDaemon(true);
    webCamThread.start();

    bottomControlPane.setDisable(false);
    resumeButton.setDisable(true);
  }

  protected void startWebCamStream(ImageView capturedImageView) {
    stop = false;
    Task<Void> task = new Task<Void>() {
      @Override
      protected Void call() throws Exception {

        final AtomicReference<WritableImage> ref = new AtomicReference<>();
        BufferedImage img = null;

        while (!stop) {
          try {
            if ((img = webcam.getImage()) != null) {
              ref.set(SwingFXUtils.toFXImage(img, ref.get()));
              img.flush();
              Platform.runLater(() -> imageProperty.set(ref.get()));
            }
          } catch (Throwable ex) {
            ex.printStackTrace();
          }
        }

        return null;
      }
    };

    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
    pictureButton.setDisable(false);
    capturedImageView.imageProperty().bind(imageProperty);
  }

  public void createCameraControls(Font font, FlowPane bottomControlPane,
      ImageView capturedImageView) {
    this.pictureButton.setFont(font);
    this.pictureButton.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {

            try {
              clickPicture();
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        });

    this.pictureButton.setText("Snap");
    this.resumeButton.setOnAction(
        new EventHandler<ActionEvent>() {
          @Override
          public void handle(ActionEvent arg0) {
            resumeThread(capturedImageView);
          }
        });

    this.resumeButton.setFont(font);
    this.resumeButton.setText("Resume thread");

    this.exitButton.setFont(font);
    this.exitButton.setText("Quit");
    this.exitButton.setOnAction(
        new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent arg0) {
            exit();
          }
        });

    this.resumeButton.setDisable(true);
    this.pictureButton.setDisable(true);
    this.exitButton.setDisable(false);
    bottomControlPane.getChildren().add(resumeButton);
    bottomControlPane.getChildren().add(pictureButton);
    bottomControlPane.getChildren().add(exitButton);
  }

  protected void clickPicture() throws IOException {
    LocalDateTime date = java.time.LocalDateTime.now();
    String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    StringBuffer fileNameBuffer = new StringBuffer(this.imageLocation);
    fileNameBuffer.append("Camera-").append(dateStr).append(".jpg");
    ImageIO.write(
        webcam.getImage(),
        "JPG",
        new File(fileNameBuffer.toString()));
    this.stop = true;
    this.pictureButton.setDisable(true);
    this.resumeButton.setDisable(false);
  }

  protected void resumeThread(ImageView capturedImageView) {
    startWebCamStream(capturedImageView);
    this.pictureButton.setDisable(false);
    this.resumeButton.setDisable(true);
  }

  protected void exit() {
    this.stop = true;
    try {
      this.webcam.close();
    } catch (Exception e) {
      System.exit(0);
    }
    System.exit(0);
  }
}
