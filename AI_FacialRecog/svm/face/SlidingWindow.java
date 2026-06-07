package face;

import java.util.*;

public class SlidingWindow {

    private int winW;
    private int winH;
    private int stepSize;
    private double scaleFactor;
    private int minScale;
    private double nmsThresh;

    public static class Detection {
        public int x, y, w, h;
        public double score;

        public Detection(int x, int y, int w, int h, double score) {
            this.x = x; this.y = y;
            this.w = w; this.h = h;
            this.score = score;
        }

        public int area() { return w * h; }
    }

    public SlidingWindow() {
        this.winW        = 128;
        this.winH        = 128;
        this.stepSize    = 48;
        this.scaleFactor = 2.0;
        this.minScale    = 1;
        this.nmsThresh   = 0.3;
    }

    public SlidingWindow(int winW, int winH, int stepSize,
                          double scaleFactor, double nmsThresh) {
        this.winW        = winW;
        this.winH        = winH;
        this.stepSize    = stepSize;
        this.scaleFactor = scaleFactor;
        this.minScale    = 1;
        this.nmsThresh   = nmsThresh;
    }

    public List<Detection> detect(java.awt.image.BufferedImage img,
                                   Classifier classifier, HOG hog) {
        int origW = img.getWidth();
        int origH = img.getHeight();

        List<Detection> candidatiDetectie = new ArrayList<Detection>();

        double scale = 1.0;
        java.awt.image.BufferedImage scaled = img;

        while (true) {
            int scaledW = scaled.getWidth();
            int scaledH = scaled.getHeight();

            if (scaledW < winW || scaledH < winH) break;

            for (int y = 0; y + winH <= scaledH; y += stepSize) {
                for (int x = 0; x + winW <= scaledW; x += stepSize) {

                    float[] hogVec = ImageUtils.cropResizeHOG(
                        scaled, x, y, winW, winH, 128, 128, hog);

                    double score = classifier.score(hogVec);

                    if (score > -0.87) {
                        int origX    = (int)(x * scale);
                        int origY    = (int)(y * scale);
                        int origBoxW = (int)(winW * scale);
                        int origBoxH = (int)(winH * scale);
                        double ratio = (double) origBoxW / origBoxH;
                        if (origBoxW >= 60 && origBoxW <= origW * 6 / 10
                                && ratio >= 0.7 && ratio <= 1.4) {
                            candidatiDetectie.add(new Detection(origX, origY,
                                                          origBoxW, origBoxH, score));
                        }
                    }
                }
            }

            scale *= scaleFactor;
            int newW = (int)(origW / scale);
            int newH = (int)(origH / scale);
            if (newW < winW || newH < winH) break;
            scaled = ImageUtils.resize(img, newW, newH);
        }

        return nonMaxSuppression(candidatiDetectie);
    }

    public List<Detection> nonMaxSuppression(List<Detection> detections) {
        if (detections.isEmpty()) return detections;

        Collections.sort(detections, new Comparator<Detection>() {
            public int compare(Detection a, Detection b) {
                return Double.compare(b.score, a.score);
            }
        });

        List<Detection> detectiiFinale  = new ArrayList<Detection>();
        boolean[] esteSuprimat    = new boolean[detections.size()];

        for (int i = 0; i < detections.size(); i++) {
            if (esteSuprimat[i]) continue;

            Detection di = detections.get(i);
            detectiiFinale.add(di);

            for (int j = i + 1; j < detections.size(); j++) {
                if (esteSuprimat[j]) continue;
                Detection dj = detections.get(j);
                if (calcIoU(di, dj) > nmsThresh)
                    esteSuprimat[j] = true;
            }
        }
        return detectiiFinale;
    }

    private double calcIoU(Detection a, Detection b) {
        int interX1 = Math.max(a.x, b.x);
        int interY1 = Math.max(a.y, b.y);
        int interX2 = Math.min(a.x + a.w, b.x + b.w);
        int interY2 = Math.min(a.y + a.h, b.y + b.h);

        if (interX2 <= interX1 || interY2 <= interY1) return 0.0;

        double interArea = (double)(interX2 - interX1) * (interY2 - interY1);
        double unionArea = (double)(a.area() + b.area()) - interArea;

        return interArea / unionArea;
    }

    public static Detection getLargest(List<Detection> detections) {
        if (detections == null || detections.isEmpty()) return null;
        Detection largest = detections.get(0);
        for (int i = 1; i < detections.size(); i++) {
            if (detections.get(i).area() > largest.area())
                largest = detections.get(i);
        }
        return largest;
    }

    public static Detection getBest(List<Detection> detections) {
        if (detections == null || detections.isEmpty()) return null;
        Detection best = detections.get(0);
        for (int i = 1; i < detections.size(); i++) {
            if (detections.get(i).score > best.score)
                best = detections.get(i);
        }
        return best;
    }

    public interface Classifier {
        double score(float[] hogVec);
    }

    public int    getWinW()        { return winW; }
    public int    getWinH()        { return winH; }
    public int    getStepSize()    { return stepSize; }
    public double getScaleFactor() { return scaleFactor; }
    public double getNmsThresh()   { return nmsThresh; }
}