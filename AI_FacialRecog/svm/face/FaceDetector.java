package face;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import model.SVMModel;
import model.TrainingData;
import alg.SMO;
import io.Vector;

public class FaceDetector {

    private HOG hog;
    private SlidingWindow sw;
    private SVMModel model;
    private static final int IMG_SIZE = 128;
    public static final String DEFAULT_MODEL_PATH = "svm/face_detector.model";
    public static final String DEFAULT_HOG_PATH   = "svm/face_detector_hog.dat";

    public FaceDetector() {
        this.hog   = new HOG(8, 2, 9);           
        this.sw    = new SlidingWindow(128, 128, 48, 2.0, 0.3);
        this.model = null; 
    }

    public void train(String positivesDir, String negativesDir,
                      int negativesPerImage, String modelPath, String hogPath) {

        System.out.println("FaceDetector: incepe antrenarea...");
        TrainingData td = new TrainingData(2000); 

        System.out.println("FaceDetector: procesez imagini pozitive din " + positivesDir);
        File posDir = new File(positivesDir);
        File[] posFiles = posDir.listFiles(); 
        int posCount = 0;
        if (posFiles != null) {
            for (File f : posFiles) {
                if (!ImageUtils.isImageFile(f.getName())) continue;
                float[] hogVec = ImageUtils.loadAndExtractHOG(
                    f.getAbsolutePath(), IMG_SIZE, IMG_SIZE, hog);
                if (hogVec == null) continue;
                td.add(hogVec, +1);
                posCount++;
                if (posCount % 100 == 0)
                    System.out.println("  pozitive procesate: " + posCount);
            }
        }
        System.out.println("FaceDetector: " + posCount + " imagini pozitive procesate.");

        
        System.out.println("FaceDetector: procesez imagini negative din " + negativesDir);
        File negDir = new File(negativesDir);
        File[] negFiles = negDir.listFiles();
        int negCount = 0;
        Random rnd = new Random(42);
        if (negFiles != null) {
            for (File f : negFiles) {
                if (!ImageUtils.isImageFile(f.getName())) continue;
                BufferedImage negImg = ImageUtils.load(f.getAbsolutePath());
                if (negImg == null) continue;
                int imgW = negImg.getWidth();
                int imgH = negImg.getHeight();
                if (imgW < IMG_SIZE || imgH < IMG_SIZE) continue; 

                for (int k = 0; k < negativesPerImage; k++) {
                    int px = rnd.nextInt(imgW - IMG_SIZE);
                    int py = rnd.nextInt(imgH - IMG_SIZE);
                    float[] hogVec = ImageUtils.cropResizeHOG(
                        negImg, px, py, IMG_SIZE, IMG_SIZE, IMG_SIZE, IMG_SIZE, hog);
                    td.add(hogVec, -1);
                    negCount++;
                }
                if (negCount % 500 == 0)
                    System.out.println("  negative procesate: " + negCount);
            }
        }
        System.out.println("FaceDetector: " + negCount + " patch-uri negative procesate.");
        System.out.println("FaceDetector: total exemple: " + td.getN()
                         + " (+" + td.countPositive() + " / -" + td.countNegative() + ")");

       
        if (td.getN() == 0 || td.countPositive() == 0 || td.countNegative() == 0) {
            System.out.println("FaceDetector: EROARE - nu sunt suficiente imagini!");
            System.out.println("  Asigurati-va ca:");
            System.out.println("  - Directorul 'positives' contine imagini .jpg cu fete");
            System.out.println("  - Directorul 'negatives' contine imagini .jpg fara fete");
            return;
        }

        System.out.println("FaceDetector: antrenez SMO...");
        Vector[] vectors = td.toVectors();

        SMO smo = SMO.createStandalone(
            1.0,  
            0.001,  
            -1.0,   
            0.001,  
            10000   
        );
        smo.train(vectors);
        System.out.println("FaceDetector: antrenare SMO finalizata.");

    
        this.model = new SVMModel(smo, "");
        this.model.pruneSuportVectors(0.01);
        try {
            model.save(modelPath);
            System.out.println("FaceDetector: model salvat in " + modelPath);
        } catch (IOException e) {
            System.out.println("FaceDetector: eroare la salvare model: " + e.getMessage());
        }

       
        try {
            td.save(hogPath);
            System.out.println("FaceDetector: vectori HOG salvati in " + hogPath);
        } catch (IOException e) {
            System.out.println("FaceDetector: eroare la salvare HOG: " + e.getMessage());
        }
    }

    public boolean loadModel(String modelPath) {
        try {
            model = SVMModel.load(modelPath);
            System.out.println("FaceDetector: model incarcat din " + modelPath);
            return true;
        } catch (IOException e) {
            System.out.println("FaceDetector: nu pot incarca modelul: " + e.getMessage());
            return false;
        }
    }


    public BufferedImage detectLargestHead(BufferedImage img) {
        if (model == null) {
            System.out.println("FaceDetector: modelul nu e incarcat!");
            return null;
        }

        
        List<SlidingWindow.Detection> detections = detectAll(img);
        if (detections.isEmpty()) return null;

        SlidingWindow.Detection largest = SlidingWindow.getLargest(detections);

        
        BufferedImage head = ImageUtils.crop(img,
            largest.x, largest.y, largest.w, largest.h);
        return ImageUtils.resize(head, 128, 128);
    }


    public List<SlidingWindow.Detection> detectAll(BufferedImage img) {
        if (model == null) return new ArrayList<SlidingWindow.Detection>();

        
        SlidingWindow.Classifier classifier = new SlidingWindow.Classifier() {
            public double score(float[] hogVec) {
                return model.score(hogVec); 
            }
        };

        return sw.detect(img, classifier, hog);
    }


    public boolean isReady() {
        return model != null;
    }

   
    public HOG          getHog()   { return hog; }
    public SVMModel     getModel() { return model; }
    public SlidingWindow getSW()   { return sw; }
}
