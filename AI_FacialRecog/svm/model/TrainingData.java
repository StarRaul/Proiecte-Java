package model;

import java.io.*;

public class TrainingData {

    private int N;
    private int dim;
    private float[][] X;
    private int[] y;

    private static final int INITIAL_CAPACITY = 1000;
    private static final int GROW_FACTOR       = 2;

    public TrainingData() {
        N   = 0;
        dim = 0;
        X   = new float[INITIAL_CAPACITY][];
        y   = new int[INITIAL_CAPACITY];
    }

    public TrainingData(int initialCapacity) {
        N   = 0;
        dim = 0;
        X   = new float[initialCapacity][];
        y   = new int[initialCapacity];
    }

    public void add(float[] hog, int label) {
        if (N == 0) dim = hog.length;
        if (N >= X.length) grow();
        X[N] = new float[dim];
        for (int j = 0; j < dim; j++)
            X[N][j] = hog[j];
        y[N] = label;
        N++;
    }

    private void grow() {
        int newCap = X.length * GROW_FACTOR;
        float[][] newX = new float[newCap][];
        int[]     newY = new int[newCap];
        for (int i = 0; i < N; i++) {
            newX[i] = X[i];
            newY[i] = y[i];
        }
        X = newX;
        y = newY;
    }

    public void addAll(TrainingData other) {
        for (int i = 0; i < other.N; i++)
            add(other.X[i], other.y[i]);
    }

    public io.Vector[] toVectors() {
        io.Clasa cls1 = new io.Clasa("+1", 1, java.awt.Color.RED);
        io.Clasa cls0 = new io.Clasa("-1", 0, java.awt.Color.BLUE);
        io.Vector[] vectors = new io.Vector[N];
        for (int i = 0; i < N; i++) {
            float[] xi = new float[dim];
            for (int j = 0; j < dim; j++) xi[j] = X[i][j];
            vectors[i] = new io.Vector(xi, y[i] == 1 ? cls1 : cls0);
        }
        return vectors;
    }

    public void save(String path) throws IOException {
        DataOutputStream dos = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(path)));
        try {
            dos.writeInt(N);
            dos.writeInt(dim);
            for (int i = 0; i < N; i++)
                dos.writeInt(y[i]);
            for (int i = 0; i < N; i++)
                for (int j = 0; j < dim; j++)
                    dos.writeFloat(X[i][j]);
        } finally {
            dos.close();
        }
        System.out.println("TrainingData: salvat " + N + " exemple in " + path);
    }

    public static TrainingData load(String path) throws IOException {
        DataInputStream dis = new DataInputStream(
            new BufferedInputStream(new FileInputStream(path)));
        try {
            int n   = dis.readInt();
            int dim = dis.readInt();
            TrainingData td = new TrainingData(n);
            td.N   = n;
            td.dim = dim;
            td.X   = new float[n][dim];
            td.y   = new int[n];
            for (int i = 0; i < n; i++)
                td.y[i] = dis.readInt();
            for (int i = 0; i < n; i++)
                for (int j = 0; j < dim; j++)
                    td.X[i][j] = dis.readFloat();
            System.out.println("TrainingData: incarcat " + n + " exemple din " + path);
            return td;
        } finally {
            dis.close();
        }
    }

    public int getN()   { return N; }
    public int getDim() { return dim; }
    public float[] getX(int i) { return X[i]; }
    public int getY(int i) { return y[i]; }

    public int countPositive() {
        int cnt = 0;
        for (int i = 0; i < N; i++)
            if (y[i] == 1) cnt++;
        return cnt;
    }

    public int countNegative() {
        int cnt = 0;
        for (int i = 0; i < N; i++)
            if (y[i] == -1) cnt++;
        return cnt;
    }

    @Override
    public String toString() {
        return "TrainingData[N=" + N + ", dim=" + dim
             + ", pozitive=" + countPositive()
             + ", negative=" + countNegative() + "]";
    }
}
