package svm.io;

public class AttributeStatistics {
    public int[] counts;
    public float min, max;
    public int numBins = 10;

    public AttributeStatistics(Vector[] vectors, int attrIdx, Clasa cl, float gMin, float gMax) {
        this.counts = new int[numBins];
        this.min = gMin;
        this.max = gMax;
        float range = max - min;
        if (range <= 0) range = 0.001f;

        if (vectors != null) {
            for (Vector v : vectors) {
                if (v != null && v.cl != null && v.cl.Y == cl.Y) {
                    float val = v.X[attrIdx];
                    int bin = (int) (((val - min) / range) * numBins);
                    if (bin >= numBins) bin = numBins - 1;
                    if (bin < 0) bin = 0;
                    counts[bin]++;
                }
            }
        }
    }
}