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

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class WebCamHandler extends Application {

  // Default image storage location
  public static String imageLocation = "/home/cross/Pictures";
  Font juliaMono;

  private FlowPane bottomCameraControlPane;
  private FlowPane topPane;
  private BorderPane root;
  private String cameraListPromptText = "Available cameras";
  private ImageView imgWebCamCapturedImage;
  private Webcam webcam = null;
  private boolean stopCamera = false;
  private ObjectProperty<Image> imageProperty = new SimpleObjectProperty<Image>();
  private BorderPane webCamPane;
  private Button btnCameraPicture;
  private Button btnCameraResume;
  private Button btnCameraExit;

  private StringBuffer imageName = new StringBuffer(imageLocation);

  // A Webcam info class
  private class WebCamInfo {

    private String webCamName;
    private int webCamIndex;

    public String getWebCamName() {
      return webCamName;
    }

    public void setWebCamName(String webCamName) {
      this.webCamName = webCamName;
    }

    public int getWebCamIndex() {
      return webCamIndex;
    }

    public void setWebCamIndex(int webCamIndex) {
      this.webCamIndex = webCamIndex;
    }

    @Override
    public String toString() {
      return webCamName;
    }
  }

  public static void main(String[] args) {
    // A method from the "Application" class
    launch(args); // Calls the start method
  }

  @Override
  public void start(Stage primaryStage) {

    // Load the font
    juliaMono = Font.loadFont(getClass().getResourceAsStream("/fonts/JuliaMono-Regular.ttf"), 14);

    primaryStage.setTitle("Camera");

    root = new BorderPane();
    // Top pane: Info message and dropdown for webcam choices
    topPane = new FlowPane();
    // Styles
    topPane.setStyle("-fx-background-color: #0e0f12;");
    topPane.setAlignment(Pos.CENTER);
    topPane.setHgap(30);
    topPane.setOrientation(Orientation.HORIZONTAL);
    topPane.setPrefHeight(40);
    topPane.setOpacity(0.9);
    root.setTop(topPane);

    // Camera window
    webCamPane = new BorderPane();
    webCamPane.setStyle("-fx-background-color: #ccc;");
    imgWebCamCapturedImage = new ImageView();
    webCamPane.setCenter(imgWebCamCapturedImage);
    root.setCenter(webCamPane);
    createTopPanel();

    // Bottom pane: Buttons
    bottomCameraControlPane = new FlowPane();
    bottomCameraControlPane.setStyle("-fx-background-color: #0e0f12;");
    bottomCameraControlPane.setOrientation(Orientation.HORIZONTAL);
    bottomCameraControlPane.setAlignment(Pos.CENTER);
    bottomCameraControlPane.setHgap(30);
    bottomCameraControlPane.setVgap(10);
    bottomCameraControlPane.setPrefHeight(40);
    bottomCameraControlPane.setOpacity(0.9);
    bottomCameraControlPane.setDisable(true);
    root.setBottom(bottomCameraControlPane);
    createCameraControls();

    primaryStage.setScene(new Scene(root));
    primaryStage.setHeight(380);
    primaryStage.setWidth(380);
    primaryStage.centerOnScreen();
    primaryStage.show();

    Platform.runLater(
        new Runnable() {

          @Override
          public void run() {
            setImageViewSize();
          }
        });
  }

  protected void setImageViewSize() {

    double height = webCamPane.getHeight();
    double width = webCamPane.getWidth();

    imgWebCamCapturedImage.setFitHeight(height);
    imgWebCamCapturedImage.setFitWidth(width);
    imgWebCamCapturedImage.prefHeight(height);
    imgWebCamCapturedImage.prefWidth(width);
    imgWebCamCapturedImage.setPreserveRatio(true);
  }

  private void createTopPanel() {

    // Label for camera selection
    Label lbInfoLabel = new Label("Camera App");
    lbInfoLabel.setFont(juliaMono);
    lbInfoLabel.setTextFill(Color.GREY);

    // Dropdown choices
    ObservableList<WebCamInfo> options = FXCollections.observableArrayList();

    topPane.getChildren().add(lbInfoLabel);

    int webCamCounter = 0;
    for (Webcam webcam : Webcam.getWebcams()) {
      WebCamInfo webCamInfo = new WebCamInfo();
      webCamInfo.setWebCamIndex(webCamCounter);
      webCamInfo.setWebCamName(webcam.getName());
      options.add(webCamInfo);
      webCamCounter++;
    }

    ComboBox<WebCamInfo> cameraOptions = new ComboBox<WebCamInfo>();
    cameraOptions.setItems(options);
    cameraOptions.setPromptText(cameraListPromptText);
    cameraOptions
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            new ChangeListener<WebCamInfo>() {

              @Override
              public void changed(
                  ObservableValue<? extends WebCamInfo> arg0, WebCamInfo arg1, WebCamInfo arg2) {
                if (arg2 != null) {
                  System.out.println(
                      "WebCam Index: "
                          + arg2.getWebCamIndex()
                          + ": WebCam Name:"
                          + arg2.getWebCamName());
                  initializeWebCam(arg2.getWebCamIndex());
                }
              }
            });
    topPane.getChildren().add(cameraOptions);
  }

  protected void initializeWebCam(final int webCamIndex) {

    Task<Void> webCamTask =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {

            if (webcam != null) {
              exit();
            }

            webcam = Webcam.getWebcams().get(webCamIndex);
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();

            startWebCamStream();

            return null;
          }
        };

    Thread webCamThread = new Thread(webCamTask);
    webCamThread.setDaemon(true);
    webCamThread.start();

    bottomCameraControlPane.setDisable(false);
    btnCameraResume.setDisable(true);
  }

  protected void startWebCamStream() {

    stopCamera = false;

    Task<Void> task =
        new Task<Void>() {

          @Override
          protected Void call() throws Exception {

            final AtomicReference<WritableImage> ref = new AtomicReference<>();
            BufferedImage img = null;

            while (!stopCamera) {
              try {
                if ((img = webcam.getImage()) != null) {

                  ref.set(SwingFXUtils.toFXImage(img, ref.get()));
                  img.flush();

                  Platform.runLater(
                      new Runnable() {

                        @Override
                        public void run() {
                          imageProperty.set(ref.get());
                        }
                      });
                }
              } catch (Exception e) {
                e.printStackTrace();
              }
            }

            return null;
          }
        };

    Thread th = new Thread(task);
    th.setDaemon(true);
    th.start();
    imgWebCamCapturedImage.imageProperty().bind(imageProperty);
  }

  private void createCameraControls() {

    juliaMono = Font.loadFont(getClass().getResourceAsStream("/fonts/JuliaMono-Regular.ttf"), 12);

    // Button to take picture
    btnCameraPicture = new Button();
    btnCameraPicture.setFont(juliaMono);
    btnCameraPicture.setOnAction(
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
    btnCameraPicture.setText("Snap");

    // Button to start the camera
    btnCameraResume = new Button();
    btnCameraResume.setOnAction(
        new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent arg0) {
            resumeThread();
          }
        });
    btnCameraResume.setFont(juliaMono);
    btnCameraResume.setText("Resume thread");

    // Button to close the app
    btnCameraExit = new Button();
    btnCameraExit.setFont(juliaMono);
    btnCameraExit.setText("Quit");
    btnCameraExit.setOnAction(
        new EventHandler<ActionEvent>() {

          @Override
          public void handle(ActionEvent arg0) {
            exit();
          }
        });
    bottomCameraControlPane.getChildren().add(btnCameraResume);
    bottomCameraControlPane.getChildren().add(btnCameraPicture);
    bottomCameraControlPane.getChildren().add(btnCameraExit);
  }

  protected void clickPicture() throws IOException {
    LocalDateTime date = java.time.LocalDateTime.now();
    String dateStr = date.format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
    ImageIO.write(
        webcam.getImage(),
        "JPG",
        new File(imageName.append("Camera-").append(dateStr).append(".jpg").toString()));
    stopCamera = true;
    btnCameraResume.setDisable(false);
    btnCameraPicture.setDisable(true);
  }

  protected void resumeThread() {
    startWebCamStream();
    btnCameraPicture.setDisable(false);
    btnCameraResume.setDisable(true);
  }

  protected void exit() {
    stopCamera = true;
    webcam.close();
    btnCameraResume.setDisable(true);
    btnCameraPicture.setDisable(true);
    System.exit(0);
  }
}
