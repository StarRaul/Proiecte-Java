package webcam;

import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import face.FaceDetector;
import face.ImageUtils;

public class DataCollector {

    private WebcamCapture webcam;
    private FaceDetector detector;
    private String rootDir;
    private int imagesPerPerson;

    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");

    private volatile boolean stopRequested;
    private ProgressListener progressListener;

    public interface ProgressListener {
        void onProgress(String personName, int saved, int total,
                        BufferedImage lastFrame);
        void onDone(String personName, int saved);
        void onNoFaceDetected(String personName);
    }

    public DataCollector(FaceDetector detector, String rootDir, int imagesPerPerson) {
        this.detector       = detector;
        this.rootDir        = rootDir;
        this.imagesPerPerson = imagesPerPerson;
        this.webcam         = new WebcamCapture(0, 320, 240, 10);
        this.stopRequested  = false;
    }

    public DataCollector(FaceDetector detector, WebcamCapture webcam,
                          String rootDir, int imagesPerPerson) {
        this.detector        = detector;
        this.webcam          = webcam;
        this.rootDir         = rootDir;
        this.imagesPerPerson = imagesPerPerson;
        this.stopRequested   = false;
    }

    public void collect(String personName) {
        stopRequested = false;

        String personDir = rootDir + File.separator + personName;
        File dir = new File(personDir);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("DataCollector: creat director " + personDir);
        }

        boolean cameraOpenedHere = false;
        if (!webcam.isOpened()) {
            if (!webcam.open()) {
                System.out.println("DataCollector: nu pot deschide camera!");
                return;
            }
            cameraOpenedHere = true;
        }

        System.out.println("DataCollector: incep colectarea pentru " + personName
                         + " (" + imagesPerPerson + " imagini)");

        int saved = 0;

        while (saved < imagesPerPerson && !stopRequested) {
            BufferedImage frame = webcam.captureFrameWithDelay();
            if (frame == null) {
                System.out.println("DataCollector: frame null, continui...");
                continue;
            }

            BufferedImage imagineFata = detector.detectLargestHead(frame);

            if (imagineFata == null) {
                if (progressListener != null)
                    progressListener.onNoFaceDetected(personName);
                continue;
            }

            String timestamp = DATE_FORMAT.format(new Date());
            String fileName  = personName + "_" + timestamp + ".jpg";
            String filePath  = personDir + File.separator + fileName;

            boolean ok = ImageUtils.save(imagineFata, filePath);
            if (ok) {
                saved++;
                System.out.println("DataCollector: salvat " + fileName
                                 + " (" + saved + "/" + imagesPerPerson + ")");
                if (progressListener != null)
                    progressListener.onProgress(personName, saved,
                                                imagesPerPerson, frame);
            } else {
                System.out.println("DataCollector: eroare la salvare " + fileName);
            }
        }

        if (cameraOpenedHere) webcam.close();

        System.out.println("DataCollector: colectare finalizata pentru "
                         + personName + ": " + saved + " imagini salvate.");

        if (progressListener != null)
            progressListener.onDone(personName, saved);
    }

    public void collectMultiple(String[] personNames, long delayBetween) {
        if (!webcam.isOpened()) {
            if (!webcam.open()) {
                System.out.println("DataCollector: nu pot deschide camera!");
                return;
            }
        }

        for (String name : personNames) {
            if (stopRequested) break;

            System.out.println("DataCollector: pregatire pentru persoana: " + name);
            System.out.println("DataCollector: astept " + delayBetween/1000
                             + " secunde pentru pozitionare...");

            try {
                Thread.sleep(delayBetween);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            collect(name);
        }

        webcam.close();
        System.out.println("DataCollector: colectare completa pentru toate persoanele.");
    }

    public void stop() {
        stopRequested = true;
        System.out.println("DataCollector: oprire solicitata.");
    }

    public int countImages(String personName) {
        File dir = new File(rootDir + File.separator + personName);
        if (!dir.exists()) return 0;
        File[] files = dir.listFiles();
        if (files == null) return 0;
        int count = 0;
        for (File f : files)
            if (ImageUtils.isImageFile(f.getName())) count++;
        return count;
    }

    public String[] getPersonNames() {
        File root = new File(rootDir);
        if (!root.exists()) return new String[0];
        File[] dirs = root.listFiles(new FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory();
            }
        });
        if (dirs == null) return new String[0];
        String[] names = new String[dirs.length];
        for (int i = 0; i < dirs.length; i++)
            names[i] = dirs[i].getName();
        return names;
    }

    public String getPersonDir(String personName) {
        return rootDir + File.separator + personName;
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    public int getImagesPerPerson() { return imagesPerPerson; }
    public void setImagesPerPerson(int n) { this.imagesPerPerson = n; }
    public String getRootDir() { return rootDir; }
    public boolean isStopRequested() { return stopRequested; }
}
