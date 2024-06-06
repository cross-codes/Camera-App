package com.github.cross;

import com.github.sarxos.webcam.Webcam;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class App {
  public static void main(String[] args) throws IOException {
    Webcam webcam = Webcam.getDefault();
    webcam.open();

    ImageIO.write(webcam.getImage(), "JPG", new File("firstCapture.jpg"));
    webcam.close();
  }
}
