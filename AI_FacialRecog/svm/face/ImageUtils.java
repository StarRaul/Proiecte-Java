package face;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;


public class ImageUtils {

    
    public static BufferedImage load(String path) {
        try {
            
            return ImageIO.read(new File(path));
        } catch (IOException e) {
            System.out.println("ImageUtils: nu pot incarca imaginea: " + path);
            return null;
        }
    }

    
    public static boolean save(BufferedImage img, String path) {
        try {
           
            String ext = "jpg";
            int dot = path.lastIndexOf('.');
            if (dot >= 0) ext = path.substring(dot + 1).toLowerCase();
            return ImageIO.write(img, ext, new File(path));
        } catch (IOException e) {
            System.out.println("ImageUtils: nu pot salva imaginea: " + path);
            return false;
        }
    }

   
    public static BufferedImage resize(BufferedImage src, int newW, int newH) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();

       
        BufferedImage dst = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);

        
        double scaleX = (double) srcW / newW; 
        double scaleY = (double) srcH / newH; 

        for (int dy = 0; dy < newH; dy++) {
            for (int dx = 0; dx < newW; dx++) {
                
                double sx = dx * scaleX; 
                double sy = dy * scaleY;

               
                int x0 = (int) sx;                          
                int y0 = (int) sy;                          
                int x1 = Math.min(x0 + 1, srcW - 1);      
                int y1 = Math.min(y0 + 1, srcH - 1);       

               
                double fx = sx - x0; 
                double fy = sy - y0; 

                
                int c00 = src.getRGB(x0, y0); 
                int c10 = src.getRGB(x1, y0); 
                int c01 = src.getRGB(x0, y1); 
                int c11 = src.getRGB(x1, y1); 

               
                int r = bilinear(r(c00), r(c10), r(c01), r(c11), fx, fy);
                int g = bilinear(g(c00), g(c10), g(c01), g(c11), fx, fy);
                int b = bilinear(b(c00), b(c10), b(c01), b(c11), fx, fy);

                
                dst.setRGB(dx, dy, rgb(r, g, b));
            }
        }
        return dst;
    }

   
    private static int bilinear(int v00, int v10, int v01, int v11,
                                 double fx, double fy) {
        
        double top    = v00 * (1.0 - fx) + v10 * fx; 
        double bottom = v01 * (1.0 - fx) + v11 * fx; 
       
        double result = top * (1.0 - fy) + bottom * fy;
        
        return (int) Math.max(0, Math.min(255, Math.round(result)));
    }

    
    public static BufferedImage crop(BufferedImage src, int x, int y, int w, int h) {
        int srcW = src.getWidth();
        int srcH = src.getHeight();

       
        int x0 = Math.max(0, x);
        int y0 = Math.max(0, y);
        int x1 = Math.min(srcW, x + w); 
        int y1 = Math.min(srcH, y + h); 

        int cropW = x1 - x0; 
        int cropH = y1 - y0; 

        if (cropW <= 0 || cropH <= 0) {
            return new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        }

        BufferedImage dst = new BufferedImage(cropW, cropH, BufferedImage.TYPE_INT_RGB);
        for (int dy = 0; dy < cropH; dy++)
            for (int dx = 0; dx < cropW; dx++)
                dst.setRGB(dx, dy, src.getRGB(x0 + dx, y0 + dy));

        return dst;
    }

   
    public static BufferedImage toGrayscale(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
       
        BufferedImage gray = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
               
                int lum = (int)(0.299 * r(rgb) + 0.587 * g(rgb) + 0.114 * b(rgb));
                
                gray.setRGB(x, y, rgb(lum, lum, lum));
            }
        }
        return gray;
    }

    public static int[] getPixels(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int[] pixels = new int[w * h];
        img.getRGB(0, 0, w, h, pixels, 0, w);
        return pixels;
    }

    
    public static float[] loadAndExtractHOG(String path, int targetW, int targetH, HOG hog) {
        BufferedImage img = load(path);
        if (img == null) return null;                       
        BufferedImage resized = resize(img, targetW, targetH); 
        int[] pixels = getPixels(resized);                 
        return hog.extract(pixels, targetW, targetH);       
    }

    public static float[] cropResizeHOG(BufferedImage src,
                                         int x, int y, int w, int h,
                                         int targetW, int targetH, HOG hog) {
        BufferedImage cropped = crop(src, x, y, w, h);         
        BufferedImage resized = resize(cropped, targetW, targetH); 
        int[] pixels = getPixels(resized);                      
        return hog.extract(pixels, targetW, targetH);          
    }

    
    public static int r(int rgb) { return (rgb >> 16) & 0xFF; }

    
    public static int g(int rgb) { return (rgb >> 8) & 0xFF; }

   
    public static int b(int rgb) { return rgb & 0xFF; }

    public static int rgb(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

   
    public static boolean isImageFile(String filename) {
        if (filename == null) return false;
        String lower = filename.toLowerCase();
        return lower.endsWith(".jpg")  || lower.endsWith(".jpeg") ||
               lower.endsWith(".png")  || lower.endsWith(".bmp")  ||
               lower.endsWith(".gif");
    }
}
