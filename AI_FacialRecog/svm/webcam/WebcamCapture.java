package webcam;

import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.awt.image.BufferedImage;

public class WebcamCapture {

    private VideoCapture capture;
    private int cameraIndex;
    private int width;
    private int height;
    private int fps;
    private boolean opened;

    private static boolean opencvLoaded = false;

    public static void loadOpenCV() {
        if (!opencvLoaded) {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            opencvLoaded = true;
            System.out.println("WebcamCapture: biblioteca OpenCV incarcata.");
        }
    }

    public WebcamCapture() {
        this(0, 320, 240, 10);
    }

    public WebcamCapture(int cameraIndex, int width, int height, int fps) {
        this.cameraIndex = cameraIndex;
        this.width       = width;
        this.height      = height;
        this.fps         = fps;
        this.opened      = false;
    }

    public boolean open() {
        loadOpenCV();

        capture = new VideoCapture(cameraIndex);

        if (!capture.isOpened()) {
            System.out.println("WebcamCapture: nu pot deschide camera " + cameraIndex);
            opened = false;
            return false;
        }

        capture.set(Videoio.CAP_PROP_FRAME_WIDTH,  width);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT, height);
        capture.set(Videoio.CAP_PROP_FPS, fps);

        opened = true;
        System.out.println("WebcamCapture: camera " + cameraIndex + " deschisa ("
                         + width + "x" + height + " @ " + fps + "fps)");
        return true;
    }

    public void close() {
        if (capture != null && capture.isOpened()) {
            capture.release();
        }
        opened = false;
        System.out.println("WebcamCapture: camera inchisa.");
    }

    public BufferedImage captureFrame() {
        if (!opened || capture == null || !capture.isOpened()) {
            System.out.println("WebcamCapture: camera nu este deschisa!");
            return null;
        }

        Mat frame = new Mat();
        boolean success = capture.read(frame);

        if (!success || frame.empty()) {
            System.out.println("WebcamCapture: nu am putut citi frame-ul.");
            frame.release();
            return null;
        }

        BufferedImage img = matToBufferedImageBGR(frame);

        frame.release();
        return img;
    }

    private BufferedImage matToBufferedImageBGR(Mat mat) {
        int w = mat.cols();
        int h = mat.rows();

        byte[] bgrData = new byte[w * h * 3];
        mat.get(0, 0, bgrData);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < w * h; i++) {
            int b = bgrData[i * 3]     & 0xFF;
            int g = bgrData[i * 3 + 1] & 0xFF;
            int r = bgrData[i * 3 + 2] & 0xFF;

            int rgb = (r << 16) | (g << 8) | b;

            img.setRGB(i % w, i / w, rgb);
        }

        return img;
    }

    public BufferedImage captureFrameWithDelay() {
        long startTime = System.currentTimeMillis();
        BufferedImage frame = captureFrame();

        long elapsed        = System.currentTimeMillis() - startTime;
        long targetInterval = 1000L / fps;
        long waitTime       = targetInterval - elapsed;

        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return frame;
    }

    public boolean isOpened() { return opened; }
    public int getWidth()     { return width; }
    public int getHeight()    { return height; }
    public int getFps()       { return fps; }

    public void setFps(int fps) {
        this.fps = fps;
        if (opened && capture != null)
            capture.set(Videoio.CAP_PROP_FPS, fps);
    }
}
