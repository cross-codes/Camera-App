package com.github.cross;

public class WebcamInfo {
  private String webcamName;
  private int webcamIndex;

  public WebcamInfo(String webcamName, int webcamIdx) {
    this.webcamName = webcamName;
    this.webcamIndex = webcamIdx;
  }

  public String getWebcamName() {
    return this.webcamName;
  }

  public void setWebcamName(String webCamName) {
    this.webcamName = webCamName;
  }

  public int getWebcamIndex() {
    return this.webcamIndex;
  }

  public void setWebcamIndex(int webCamIndex) {
    this.webcamIndex = webCamIndex;
  }

  @Override
  public String toString() {
    return this.webcamName;
  }
}
