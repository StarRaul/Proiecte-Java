package face;


public class HOG {

    private int cellSize;
    private int blockSize;
    private int nbins;

    private static final double NORM_EPS = 1e-5;

    public HOG() {
        this.cellSize  = 8;
        this.blockSize = 2;
        this.nbins     = 9;
    }

    public HOG(int cellSize, int blockSize, int nbins) {
        this.cellSize  = cellSize;
        this.blockSize = blockSize;
        this.nbins     = nbins;
    }

    
    public float[] extract(int[][] pixels, int width, int height) {
        
        float[][] gray = toGrayscale(pixels, width, height);

        float[][] magnitude   = new float[height][width];
        float[][] orientation = new float[height][width];
        computeGradients(gray, width, height, magnitude, orientation);

        int nCellsX = width  / cellSize; 
        int nCellsY = height / cellSize; 
        float[][][] histogrameCell = computeCellHistograms(magnitude, orientation,
                                                     nCellsX, nCellsY);

        return normalizeAndConcatenate(histogrameCell, nCellsX, nCellsY);
    }

   
    public float[] extract(int[] pixels, int width, int height) {
        int[][] pixels2D = new int[height][width];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                pixels2D[y][x] = pixels[y * width + x]; 
        return extract(pixels2D, width, height);
    }

 
    private float[][] toGrayscale(int[][] pixels, int width, int height) {
        float[][] gray = new float[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = pixels[y][x];
                
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >>  8) & 0xFF; 
                int b =  rgb        & 0xFF; 
                
                gray[y][x] = 0.299f * r + 0.587f * g + 0.114f * b;
            }
        }
        return gray;
    }

    
    private void computeGradients(float[][] gray, int width, int height,
                                   float[][] magnitude, float[][] orient) {
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                float gx = gray[y][x + 1] - gray[y][x - 1];
                float gy = gray[y + 1][x] - gray[y - 1][x];

                magnitude[y][x] = (float) Math.sqrt(gx * gx + gy * gy);

                
                double angle = Math.toDegrees(Math.atan2(Math.abs(gy), Math.abs(gx)));
        
                if (angle >= 180.0) angle -= 180.0;
                orient[y][x] = (float) angle;
            }
        }
        
    }

    
    private float[][][] computeCellHistograms(float[][] magnitude, float[][] orient,
                                               int nCellsX, int nCellsY) {
        float[][][] histogrameCell = new float[nCellsY][nCellsX][nbins];
        double binWidth = 180.0 / nbins; 

        for (int cy = 0; cy < nCellsY; cy++) {
            for (int cx = 0; cx < nCellsX; cx++) {
                
                int yStart = cy * cellSize;
                int xStart = cx * cellSize;

                for (int py = yStart; py < yStart + cellSize; py++) {
                    for (int px = xStart; px < xStart + cellSize; px++) {
                        float mag = magnitude[py][px]; 
                        float ang = orient[py][px];    

                       
                        double binFloat = ang / binWidth - 0.5; 
                        int    bin0     = (int) Math.floor(binFloat);
                        double frac     = binFloat - bin0;            

                        
                        int bin1 = (bin0 + 1) % nbins;
                     
                        bin0 = ((bin0 % nbins) + nbins) % nbins;

                       
                        histogrameCell[cy][cx][bin0] += mag * (float)(1.0 - frac);
                        histogrameCell[cy][cx][bin1] += mag * (float) frac;
                    }
                }
            }
        }
        return histogrameCell;
    }

   
    private float[] normalizeAndConcatenate(float[][][] histogrameCell,
                                             int nCellsX, int nCellsY) {
       
        int nBlocksX = nCellsX - blockSize + 1; 
        int nBlocksY = nCellsY - blockSize + 1; 

        
        int vecSize = nBlocksY * nBlocksX * blockSize * blockSize * nbins;
        float[] vectorDescriptor = new float[vecSize]; 
        int idx = 0; 

        for (int by = 0; by < nBlocksY; by++) {
            for (int bx = 0; bx < nBlocksX; bx++) {

                
                float[] blockVec = new float[blockSize * blockSize * nbins];
                int k = 0;
                for (int dy = 0; dy < blockSize; dy++) {
                    for (int dx = 0; dx < blockSize; dx++) {
                        int cy = by + dy; 
                        int cx = bx + dx; 
                        for (int bin = 0; bin < nbins; bin++) {
                            blockVec[k++] = histogrameCell[cy][cx][bin];
                        }
                    }
                }

               
                double norm = 0.0;
                for (int i = 0; i < blockVec.length; i++)
                    norm += (double)blockVec[i] * blockVec[i];
                norm = Math.sqrt(norm + NORM_EPS * NORM_EPS); 
                for (int i = 0; i < blockVec.length; i++)
                    blockVec[i] = (float)(blockVec[i] / norm);

               
                for (int i = 0; i < blockVec.length; i++)
                    if (blockVec[i] > 0.2f) blockVec[i] = 0.2f;

                
                norm = 0.0;
                for (int i = 0; i < blockVec.length; i++)
                    norm += (double)blockVec[i] * blockVec[i];
                norm = Math.sqrt(norm + NORM_EPS * NORM_EPS);
                for (int i = 0; i < blockVec.length; i++)
                    blockVec[i] = (float)(blockVec[i] / norm);

                
                for (int i = 0; i < blockVec.length; i++)
                    vectorDescriptor[idx++] = blockVec[i];
            }
        }

        return vectorDescriptor; 
    }

 
    public int getVectorSize(int width, int height) {
        int nCellsX  = width  / cellSize;
        int nCellsY  = height / cellSize;
        int nBlocksX = nCellsX - blockSize + 1;
        int nBlocksY = nCellsY - blockSize + 1;
        return nBlocksY * nBlocksX * blockSize * blockSize * nbins;
    }

   

    
    public int getCellSize()  { return cellSize; }

    
    public int getBlockSize() { return blockSize; }

   
    public int getNbins()     { return nbins; }
}
