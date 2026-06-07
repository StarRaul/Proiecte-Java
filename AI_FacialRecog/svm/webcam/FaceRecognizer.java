package webcam;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.*;
import java.util.*;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import face.*;
import model.*;
import alg.SMO;
import io.Vector;

public class FaceRecognizer {

    private HOG hog;
    private FaceDetector faceDetector;
    private WebcamCapture webcam;
    private SVMModel[] personModels;
    private String trainingDir;

    public static final String MODELS_PATH = "svm/face_models_hog.dat";

    private static final int IMG_SIZE = 128;
    private static final Scalar CULOARE_CHENAR = new Scalar(0, 255, 0);
    private static final int GROSIME_LINIE = 2;
    private static final double SCALA_TEXT = 0.75;

    private volatile boolean running;
    private FrameListener frameListener;

    public interface FrameListener {
        void onFrame(BufferedImage frame, String[] names);
    }

    public FaceRecognizer(FaceDetector faceDetector, String trainingDir) {
        this.faceDetector = faceDetector;
        this.trainingDir  = trainingDir;
        this.hog          = new HOG(8, 2, 9);
        this.webcam       = new WebcamCapture(0, 640, 480, 10);
        this.personModels = null;
        this.running      = false;
    }

    public void trainAll(String modelsPath) {
        System.out.println("FaceRecognizer: incep antrenarea clasificatoarelor...");

        File rootDir = new File(trainingDir);
        File[] personDirs = rootDir.listFiles(new FileFilter() {
            public boolean accept(File f) { return f.isDirectory(); }
        });

        if (personDirs == null || personDirs.length == 0) {
            System.out.println("FaceRecognizer: nicio persoana in " + trainingDir);
            return;
        }
        Arrays.sort(personDirs);

        int nPersons = personDirs.length;
        System.out.println("FaceRecognizer: " + nPersons + " persoane gasite.");

        TrainingData[] allData  = new TrainingData[nPersons];
        String[]       allNames = new String[nPersons];

        for (int p = 0; p < nPersons; p++) {
            allNames[p] = personDirs[p].getName();
            allData[p]  = new TrainingData(600);
            System.out.println("FaceRecognizer: extrag HOG pentru " + allNames[p]);

            File[] imgFiles = personDirs[p].listFiles();
            if (imgFiles == null) continue;

            for (File f : imgFiles) {
                if (!ImageUtils.isImageFile(f.getName())) continue;
                float[] hogVec = ImageUtils.loadAndExtractHOG(
                    f.getAbsolutePath(), IMG_SIZE, IMG_SIZE, hog);
                if (hogVec != null)
                    allData[p].add(hogVec, +1);
            }
            System.out.println("  " + allData[p].getN() + " imagini procesate.");
        }

        personModels = new SVMModel[nPersons];
        TrainingData setDateHOG = new TrainingData(nPersons * 600);

        for (int p = 0; p < nPersons; p++) {
            System.out.println("FaceRecognizer: antrenez clasificator pentru "
                             + allNames[p] + "...");

            TrainingData td = new TrainingData(nPersons * 600);

            for (int i = 0; i < allData[p].getN(); i++)
                td.add(allData[p].getX(i), +1);

            for (int q = 0; q < nPersons; q++) {
                if (q == p) continue;
                for (int i = 0; i < allData[q].getN(); i++)
                    td.add(allData[q].getX(i), -1);
            }

            System.out.println("  pozitive: " + td.countPositive()
                             + ", negative: " + td.countNegative());

            Vector[] vectors = td.toVectors();
            SMO smo = SMO.createStandalone(1, 0.001, -1.0, 0.001, 1000000);
            smo.train(vectors);

            personModels[p] = new SVMModel(smo, allNames[p]);
            personModels[p].pruneSuportVectors(0.01);
            System.out.println("FaceRecognizer: clasificator antrenat pentru "
                             + allNames[p]);

            setDateHOG.addAll(td);
        }

        try {
            SVMModel.saveAllWithHOG(personModels, setDateHOG, modelsPath);
            System.out.println("FaceRecognizer: modele + HOG salvate in " + modelsPath);
        } catch (IOException e) {
            System.out.println("FaceRecognizer: eroare salvare: " + e.getMessage());
        }

        System.out.println("FaceRecognizer: antrenare completa pentru "
                         + nPersons + " persoane.");
    }

    public boolean loadModels(String modelsPath) {
        try {
            Object[] result = SVMModel.loadAllWithHOG(modelsPath);
            personModels = (SVMModel[]) result[0];
            System.out.println("FaceRecognizer: incarcate " + personModels.length
                             + " modele din " + modelsPath);
            return true;
        } catch (IOException e) {
            System.out.println("FaceRecognizer: nu pot incarca modelele: "
                             + e.getMessage());
            return false;
        }
    }

    public void startLive() {
        if (personModels == null || personModels.length == 0) {
            System.out.println("FaceRecognizer: nu exista modele incarcate!");
            return;
        }
        if (!faceDetector.isReady()) {
            System.out.println("FaceRecognizer: detectorul de cap nu e pregatit!");
            return;
        }

        running = true;

        if (!webcam.isOpened()) {
            if (!webcam.open()) {
                System.out.println("FaceRecognizer: nu pot deschide camera!");
                running = false;
                return;
            }
        }

        System.out.println("FaceRecognizer: recunoastere live pornita la 10 FPS...");

        while (running) {
            BufferedImage frame = webcam.captureFrameWithDelay();
            if (frame == null) continue;

            java.util.List<SlidingWindow.Detection> detections =
                faceDetector.detectAll(frame);

            SlidingWindow.Detection largest = SlidingWindow.getBest(detections);
            detections = new java.util.ArrayList<SlidingWindow.Detection>();
            if (largest != null && largest.score > -0.87) detections.add(largest);

            Mat mat = bufferedImageToMat(frame);

            java.util.List<String> recognizedNames = new ArrayList<String>();

            for (SlidingWindow.Detection det : detections) {

                Imgproc.rectangle(
                    mat,
                    new org.opencv.core.Point(det.x, det.y),
                    new org.opencv.core.Point(det.x + det.w, det.y + det.h),
                    CULOARE_CHENAR,
                    GROSIME_LINIE
                );

                BufferedImage imagineCap = ImageUtils.crop(
                    frame, det.x, det.y, det.w, det.h);
                imagineCap = ImageUtils.resize(imagineCap, IMG_SIZE, IMG_SIZE);

                int[]   pixels = ImageUtils.getPixels(imagineCap);
                float[] hogVec = hog.extract(pixels, IMG_SIZE, IMG_SIZE);

                String recognized = null;
                double bestScore  = -Double.MAX_VALUE;

                for (SVMModel m : personModels) {
                    double score = m.score(hogVec);
                    if (score > bestScore) {
                        bestScore  = score;
                        recognized = m.personName;
                    }
                }

                if (bestScore <= 0.0) recognized = null;

                if (recognized != null) {
                    Imgproc.putText(
                        mat,
                        recognized,
                        new org.opencv.core.Point(
                            det.x,
                            Math.max(det.y - 8, 15)
                        ),
                        Imgproc.FONT_HERSHEY_SIMPLEX,
                        SCALA_TEXT,
                        CULOARE_CHENAR,
                        2
                    );
                    recognizedNames.add(recognized);
                    System.out.println("FaceRecognizer: recunoscut " + recognized
                        + " (scor=" + String.format("%.3f", bestScore) + ")");
                }
            }

            BufferedImage annotated = matToBufferedImage(mat);
            mat.release();

            if (frameListener != null) {
                String[] names = recognizedNames.toArray(new String[0]);
                frameListener.onFrame(annotated, names);
            }
        }

        webcam.close();
        System.out.println("FaceRecognizer: recunoastere live oprita.");
    }

    public void startLiveAsync() {
        Thread t = new Thread(new Runnable() {
            public void run() { startLive(); }
        });
        t.setDaemon(true);
        t.start();
    }

    public void stopLive() {
        running = false;
        System.out.println("FaceRecognizer: oprire solicitata.");
    }

    private Mat bufferedImageToMat(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        BufferedImage bgr;
        if (img.getType() == BufferedImage.TYPE_3BYTE_BGR) {
            bgr = img;
        } else {
            bgr = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
            Graphics g = bgr.getGraphics();
            g.drawImage(img, 0, 0, null);
            g.dispose();
        }

        byte[] pixels = ((DataBufferByte) bgr.getRaster().getDataBuffer()).getData();
        Mat mat = new Mat(h, w, CvType.CV_8UC3);
        mat.put(0, 0, pixels);

        return mat;
    }

    private BufferedImage matToBufferedImage(Mat mat) {
        int w = mat.cols();
        int h = mat.rows();

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
        byte[] pixels = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        mat.get(0, 0, pixels);

        return img;
    }

    public void setFrameListener(FrameListener listener) {
        this.frameListener = listener;
    }

    public boolean isRunning() { return running; }

    public int getPersonCount() {
        return personModels != null ? personModels.length : 0;
    }

    public String[] getPersonNames() {
        if (personModels == null) return new String[0];
        String[] names = new String[personModels.length];
        for (int i = 0; i < personModels.length; i++)
            names[i] = personModels[i].personName;
        return names;
    }

    public SVMModel[] getPersonModels() { return personModels; }

    public WebcamCapture getWebcam() { return webcam; }
}