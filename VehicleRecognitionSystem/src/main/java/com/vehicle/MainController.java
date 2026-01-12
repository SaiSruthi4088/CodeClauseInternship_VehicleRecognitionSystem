package com.vehicle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;

public class MainController {

    @FXML
    private ImageView cameraView;

    @FXML
    private ImageView imageView;

    @FXML
    private TextArea resultArea;

    private VideoCapture capture;
    private CascadeClassifier carCascade;
    private Mat uploadedImage;

    private boolean reportSaved = false;
    private volatile boolean cameraRunning = false;

    // ===== STEP 5 ADDITIONS =====
    private int frameCount = 0;          // B. frame skipping
    private int previousCount = 0;       // C. stable count

    // Load OpenCV native library
    static {
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java412.dll");
    }

    @FXML
    public void initialize() {
        try {
            carCascade = new CascadeClassifier(
                    new File(getClass().getResource("/com/vehicle/cars.xml").toURI()).getAbsolutePath()
            );

            if (carCascade.empty()) {
                resultArea.setText("❌ Failed to load cars.xml");
            } else {
                System.out.println("Car cascade loaded successfully!");
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultArea.setText("Error loading cascade");
        }
    }

    // ================= CAMERA =================
    @FXML
    private void startCamera() {
        if (cameraRunning) return;

        cameraRunning = true;
        capture = new VideoCapture(0);

        if (!capture.isOpened()) {
            resultArea.setText("❌ Cannot open camera");
            cameraRunning = false;
            return;
        }

        new Thread(() -> {
            Mat frame = new Mat();

            while (cameraRunning && capture.isOpened()) {
                if (capture.read(frame) && !frame.empty()) {

                    // ===== B. FRAME SKIPPING =====
                    frameCount++;
                    if (frameCount % 5 != 0) {
                        continue;
                    }

                    int currentCount = detectCarsInFrame(frame);

                    // ===== C. STABLE COUNT =====
                    int stableCount = (previousCount + currentCount) / 2;
                    previousCount = stableCount;

                    Platform.runLater(() -> {
                        cameraView.setImage(matToImage(frame));
                        resultArea.setText("Live Cars detected: " + stableCount);
                    });
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {}
            }

            if (capture.isOpened()) capture.release();
        }).start();
    }

    @FXML
    private void stopCamera() {
        cameraRunning = false;
    }

    // ================= IMAGE UPLOAD =================
    @FXML
    private void uploadImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Select Image");
        File file = chooser.showOpenDialog(new Stage());

        if (file != null) {
            uploadedImage = Imgcodecs.imread(file.getAbsolutePath());

            if (uploadedImage.empty()) {
                resultArea.setText("❌ Image load failed");
                return;
            }

            imageView.setImage(matToImage(uploadedImage));
            resultArea.setText("✅ Image loaded");
        }
    }

    // ================= DETECT BUTTON =================
    @FXML
    private void detectVehicle() {
        if (uploadedImage == null || uploadedImage.empty()) {
            resultArea.setText("No image loaded to detect.");
            return;
        }

        Mat mat = uploadedImage.clone();
        int count = detectCarsInFrame(mat);

        imageView.setImage(matToImage(mat));
        resultArea.setText("Cars detected (Image): " + count);

        if (!reportSaved) {
            saveReport(count);
            reportSaved = true;
        }
    }

    // ================= DETECTION LOGIC =================
    private synchronized int detectCarsInFrame(Mat frame) {
        if (frame == null || frame.empty()) return 0;

        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(gray, gray);

        MatOfRect cars = new MatOfRect();
        carCascade.detectMultiScale(
                gray,
                cars,
                1.05,
                3,
                0,
                new Size(50, 50),
                new Size()
        );

        int count = 0;
        Rect[] detectedCars = cars.toArray();

        for (Rect rect : detectedCars) {

            // ===== A. MINIMUM SIZE FILTER =====
            if (rect.width < 60 || rect.height < 60) {
                continue;
            }

            count++;

            Imgproc.rectangle(frame,
                    new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 2);

            Imgproc.putText(frame, "Car",
                    new Point(rect.x, rect.y - 5),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    0.7,
                    new Scalar(0, 255, 0),
                    2);
        }

        return count;
    }

    // ================= REPORT =================
    private void saveReport(int count) {
        try {
            File file = new File("C:\\VehicleReports\\vehicle_detection_report.txt");
            file.getParentFile().mkdirs();

            FileWriter writer = new FileWriter(file, true);
            writer.write("Cars detected: " + count + "\n");
            writer.write("Time: " + LocalDateTime.now() + "\n");
            writer.write("---------------------------\n");
            writer.close();

            Platform.runLater(() ->
                    resultArea.appendText("\nReport saved successfully!")
            );

        } catch (Exception e) {
            Platform.runLater(() ->
                    resultArea.appendText("\nFailed to save report")
            );
        }
    }

    // ================= MAT → IMAGE =================
    private Image matToImage(Mat mat) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".png", mat, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }
}
