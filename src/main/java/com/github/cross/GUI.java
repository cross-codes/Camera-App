package com.github.cross;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class GUI extends javax.swing.JFrame {
  private javax.swing.JLabel imageHolder;
  private javax.swing.JButton captureButton;

  Webcam webcam;
  Boolean isRunning = false;

  class VideoFeedTaker extends Thread {
    @Override
    public void run() {
      while (isRunning) {
        try {
          Image image = webcam.getImage();
          imageHolder.setIcon(new ImageIcon(image));
          Thread.sleep(50);
        } catch (InterruptedException ex) {
          Logger.getLogger(GUI.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  private void buttonActionPerformed(java.awt.event.ActionEvent evt) {
    if (!isRunning) {
      new VideoFeedTaker().start();
      isRunning = true;
    } else {
      isRunning = false;
    }
  }

  public GUI() {
    super("Camera");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Image panel
    JPanel imagePanel = new JPanel();
    imagePanel.setPreferredSize(new Dimension(640, 480));
    imageHolder = new JLabel();
    imagePanel.add(imageHolder);

    // Button
    captureButton = new JButton("Capture");
    captureButton.addActionListener(
        new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            buttonActionPerformed(e);
          }
        });

    // Main panel;
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BorderLayout());

    mainPanel.add(imagePanel, BorderLayout.CENTER);
    mainPanel.add(captureButton, BorderLayout.SOUTH);

    add(mainPanel);
    pack();
    setLocationRelativeTo(null);

    // Webcam settings
    webcam = Webcam.getDefault();
    webcam.setViewSize(WebcamResolution.VGA.getSize());
    webcam.open();
  }

  public static void main(String[] args) {
    try {
      for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
        if ("Nimbus".equals(info.getName())) {
          UIManager.setLookAndFeel(info.getClassName());
          break;
        }
      }
    } catch (ClassNotFoundException exceptionType1) {
      java.util.logging.Logger.getLogger(GUI.class.getName())
          .log(java.util.logging.Level.SEVERE, null, exceptionType1);
    } catch (InstantiationException exceptionType2) {
      java.util.logging.Logger.getLogger(GUI.class.getName())
          .log(java.util.logging.Level.SEVERE, null, exceptionType2);
    } catch (IllegalAccessException exceptionType3) {
      java.util.logging.Logger.getLogger(GUI.class.getName())
          .log(java.util.logging.Level.SEVERE, null, exceptionType3);
    } catch (javax.swing.UnsupportedLookAndFeelException exceptionType4) {
      java.util.logging.Logger.getLogger(GUI.class.getName())
          .log(java.util.logging.Level.SEVERE, null, exceptionType4);
    }

    java.awt.EventQueue.invokeLater(
        new Runnable() {
          public void run() {
            new GUI().setVisible(true);
          }
        });
  }
}
