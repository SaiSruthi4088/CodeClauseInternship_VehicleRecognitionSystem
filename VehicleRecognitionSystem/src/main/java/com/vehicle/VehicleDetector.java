package com.vehicle;

import org.opencv.core.*;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;

public class VehicleDetector {

    private CascadeClassifier vehicleCascade;

    public VehicleDetector(String cascadePath) {
        System.load("C:\\opencv\\build\\java\\x64\\opencv_java412.dll");
        vehicleCascade = new CascadeClassifier(cascadePath);
        if (vehicleCascade.empty()) {
            System.out.println("Failed to load cascade!");
        } else {
            System.out.println("Cascade loaded successfully!");
        }
    }

    public Mat detectVehicles(Mat frame) {
        Mat gray = new Mat();
        Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

        MatOfRect vehicles = new MatOfRect();
        vehicleCascade.detectMultiScale(gray, vehicles);

        for (Rect rect : vehicles.toArray()) {
            Imgproc.rectangle(frame, new Point(rect.x, rect.y),
                    new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0), 2);
        }
        return frame;
    }

    public Mat detectFromImage(String imagePath) {
        Mat image = Imgcodecs.imread(imagePath);
        return detectVehicles(image);
    }

    public void detectFromCamera() {
        VideoCapture camera = new VideoCapture(0);
        Mat frame = new Mat();
        while (camera.read(frame)) {
            Mat processed = detectVehicles(frame);
            // TODO: send 'processed' to JavaFX ImageView
        }
        camera.release();
    }
}
