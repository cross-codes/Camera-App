package com.github.cross;

import com.github.sarxos.webcam.Webcam;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class StageDesigner {

  private FlowPane bottomControlPane;
  private FlowPane topPane;
  private BorderPane root;
  private String cameraListPromptText = "Available cameras";
  private BorderPane borderPane;
  private ImageView capturedImageView;
  protected WebcamController controller;

  public StageDesigner(Stage primaryStage, Font font, WebcamController handler) {
    this.controller = handler;

    this.root = new BorderPane();
    this.topPane = new FlowPane();
    this.topPane.setStyle("-fx-background-color: #0e0f12;");
    this.topPane.setAlignment(Pos.CENTER);
    this.topPane.setHgap(30);
    this.topPane.setOrientation(Orientation.HORIZONTAL);
    this.topPane.setPrefHeight(40);
    this.topPane.setOpacity(0.9);
    this.root.setTop(topPane);

    this.borderPane = new BorderPane();
    this.borderPane.setStyle("-fx-background-color: #ccc;");
    this.capturedImageView = new ImageView();
    this.borderPane.setCenter(capturedImageView);
    this.root.setCenter(borderPane);
    createTopPanel(font);

    this.bottomControlPane = new FlowPane();
    this.bottomControlPane.setStyle("-fx-background-color: #0e0f12;");
    this.bottomControlPane.setOrientation(Orientation.HORIZONTAL);
    this.bottomControlPane.setAlignment(Pos.CENTER);
    this.bottomControlPane.setHgap(30);
    this.bottomControlPane.setVgap(10);
    this.bottomControlPane.setPrefHeight(40);
    this.bottomControlPane.setOpacity(0.9);
    this.bottomControlPane.setDisable(false);
    this.root.setBottom(bottomControlPane);
    this.controller.createCameraControls(font, bottomControlPane, capturedImageView);

    primaryStage.setScene(new Scene(root));
    primaryStage.setHeight(380);
    primaryStage.setWidth(380);
    primaryStage.centerOnScreen();
    primaryStage.show();
  }

  private void createTopPanel(Font font) {

    Label lbInfoLabel = new Label("Camera App");
    lbInfoLabel.setFont(font);
    lbInfoLabel.setTextFill(Color.GREY);
    ObservableList<WebcamInfo> options = FXCollections.observableArrayList();

    this.topPane.getChildren().add(lbInfoLabel);

    for (Webcam webcam : Webcam.getWebcams()) {
      String webcamName = webcam.getName();
      WebcamInfo webCamInfo = new WebcamInfo(webcamName, options.size());
      options.add(webCamInfo);
    }

    ComboBox<WebcamInfo> cameraOptions = new ComboBox<WebcamInfo>();
    cameraOptions.setItems(options);
    cameraOptions.setPromptText(cameraListPromptText);
    cameraOptions
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            new ChangeListener<WebcamInfo>() {
              @Override
              public void changed(
                  ObservableValue<? extends WebcamInfo> arg0, WebcamInfo arg1, WebcamInfo arg2) {
                if (arg2 != null) {
                  System.out.println(
                      "WebCam Index: "
                          + arg2.getWebcamIndex()
                          + ": WebCam Name:"
                          + arg2.getWebcamName());
                  controller.initializeWebCam(arg2.getWebcamIndex(), bottomControlPane, capturedImageView);
                }
              }
            });
    this.topPane.getChildren().add(cameraOptions);
  }

  protected void setImageViewSize() {
    double height = borderPane.getHeight();
    double width = borderPane.getWidth();

    this.capturedImageView.setFitHeight(height);
    this.capturedImageView.setFitWidth(width);
    this.capturedImageView.prefHeight(height);
    this.capturedImageView.prefWidth(width);
    this.capturedImageView.setPreserveRatio(true);
  }

}
