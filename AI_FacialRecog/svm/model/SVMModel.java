package model;

import java.io.*;
import io.Vector;
import alg.SMO;

public class SVMModel implements Serializable {

    private static final long serialVersionUID = 1L;

    public double[] alpha;
    public double b;
    public int[] labels;
    public float[][] supportVectors;

    public double gamma;
    public double coef0;

    public String personName;
    public int N;
    public int dim;

    public SVMModel() {
        this.personName = "";
    }

    public SVMModel(SMO smo, String personName) {
        this.personName = personName;
        this.b          = smo.getB();
        this.gamma      = smo.gamma;
        this.coef0      = smo.coef0;

        Vector[] tv = smo.getTrainVectors();
        this.N   = tv.length;
        this.dim = tv[0].getDimension();
        double[] srcAlpha  = smo.getAlpha();
        int[]    srcLabels = smo.getLabels();
        this.alpha  = new double[this.N];
        this.labels = new int[this.N];
        System.arraycopy(srcAlpha,  0, this.alpha,  0, this.N);
        System.arraycopy(srcLabels, 0, this.labels, 0, this.N);

        this.supportVectors = new float[N][dim];
        for (int i = 0; i < N; i++)
            for (int j = 0; j < dim; j++)
                this.supportVectors[i][j] = tv[i].X[j];
    }

    private double kernel(float[] x, float[] z) {
        double dot = 0.0;
        for (int j = 0; j < x.length; j++)
            dot += (double)x[j] * (double)z[j];
        return Math.tanh(gamma * dot + coef0);
    }

    public double score(float[] x) {
        double s = b;
        for (int i = 0; i < N; i++) {
            if (alpha[i] == 0.0) continue;
            s += alpha[i] * labels[i] * kernel(supportVectors[i], x);
        }
        return s;
    }

    public int classify(float[] x) {
        return score(x) >= 0.0 ? 1 : -1;
    }

    public void pruneSuportVectors(double threshold) {
        int kept = 0;
        for (int i = 0; i < N; i++)
            if (Math.abs(alpha[i]) >= threshold) kept++;

        double[]   newAlpha = new double[kept];
        int[]      newLabels = new int[kept];
        float[][]  newSV    = new float[kept][];

        int k = 0;
        for (int i = 0; i < N; i++) {
            if (Math.abs(alpha[i]) >= threshold) {
                newAlpha[k]  = alpha[i];
                newLabels[k] = labels[i];
                newSV[k]     = supportVectors[i];
                k++;
            }
        }
        System.out.println("SVMModel[" + personName + "]: vectori suport "
                         + N + " -> " + kept);
        alpha          = newAlpha;
        labels         = newLabels;
        supportVectors = newSV;
        N              = kept;
    }

    public void save(String path) throws IOException {
        DataOutputStream dos = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(path)));
        try {
            dos.writeUTF(personName);
            dos.writeDouble(b);
            dos.writeDouble(gamma);
            dos.writeDouble(coef0);
            dos.writeInt(N);
            dos.writeInt(dim);

            for (int i = 0; i < N; i++)
                dos.writeDouble(alpha[i]);

            for (int i = 0; i < N; i++)
                dos.writeInt(labels[i]);

            for (int i = 0; i < N; i++)
                for (int j = 0; j < dim; j++)
                    dos.writeFloat(supportVectors[i][j]);
        } finally {
            dos.close();
        }
    }

    public static SVMModel load(String path) throws IOException {
        DataInputStream dis = new DataInputStream(
            new BufferedInputStream(new FileInputStream(path)));
        try {
            SVMModel model     = new SVMModel();
            model.personName   = dis.readUTF();
            model.b            = dis.readDouble();
            model.gamma        = dis.readDouble();
            model.coef0        = dis.readDouble();
            model.N            = dis.readInt();
            model.dim          = dis.readInt();

            model.alpha = new double[model.N];
            for (int i = 0; i < model.N; i++)
                model.alpha[i] = dis.readDouble();

            model.labels = new int[model.N];
            for (int i = 0; i < model.N; i++)
                model.labels[i] = dis.readInt();

            model.supportVectors = new float[model.N][model.dim];
            for (int i = 0; i < model.N; i++)
                for (int j = 0; j < model.dim; j++)
                    model.supportVectors[i][j] = dis.readFloat();

            return model;
        } finally {
            dis.close();
        }
    }

    public static void saveAll(SVMModel[] models, String path) throws IOException {
        DataOutputStream dos = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(path)));
        try {
            dos.writeInt(models.length);
            for (SVMModel m : models) {
                dos.writeUTF(m.personName);
                dos.writeDouble(m.b);
                dos.writeDouble(m.gamma);
                dos.writeDouble(m.coef0);
                dos.writeInt(m.N);
                dos.writeInt(m.dim);
                for (int i = 0; i < m.N; i++) dos.writeDouble(m.alpha[i]);
                for (int i = 0; i < m.N; i++) dos.writeInt(m.labels[i]);
                for (int i = 0; i < m.N; i++)
                    for (int j = 0; j < m.dim; j++)
                        dos.writeFloat(m.supportVectors[i][j]);
            }
        } finally {
            dos.close();
        }
    }

    public static SVMModel[] loadAll(String path) throws IOException {
        DataInputStream dis = new DataInputStream(
            new BufferedInputStream(new FileInputStream(path)));
        try {
            int n = dis.readInt();
            SVMModel[] models = new SVMModel[n];
            for (int mi = 0; mi < n; mi++) {
                SVMModel m   = new SVMModel();
                m.personName = dis.readUTF();
                m.b          = dis.readDouble();
                m.gamma      = dis.readDouble();
                m.coef0      = dis.readDouble();
                m.N          = dis.readInt();
                m.dim        = dis.readInt();
                m.alpha      = new double[m.N];
                for (int i = 0; i < m.N; i++) m.alpha[i] = dis.readDouble();
                m.labels     = new int[m.N];
                for (int i = 0; i < m.N; i++) m.labels[i] = dis.readInt();
                m.supportVectors = new float[m.N][m.dim];
                for (int i = 0; i < m.N; i++)
                    for (int j = 0; j < m.dim; j++)
                        m.supportVectors[i][j] = dis.readFloat();
                models[mi] = m;
            }
            return models;
        } finally {
            dis.close();
        }
    }

    public static void saveAllWithHOG(SVMModel[] models,
                                       model.TrainingData hogData,
                                       String path) throws IOException {
        DataOutputStream dos = new DataOutputStream(
            new BufferedOutputStream(new FileOutputStream(path)));
        try {
            dos.writeInt(models.length);

            for (SVMModel m : models) {
                dos.writeUTF(m.personName);
                dos.writeDouble(m.b);
                dos.writeDouble(m.gamma);
                dos.writeDouble(m.coef0);
                dos.writeInt(m.N);
                dos.writeInt(m.dim);
                for (int i = 0; i < m.N; i++) dos.writeDouble(m.alpha[i]);
                for (int i = 0; i < m.N; i++) dos.writeInt(m.labels[i]);
                for (int i = 0; i < m.N; i++)
                    for (int j = 0; j < m.dim; j++)
                        dos.writeFloat(m.supportVectors[i][j]);
            }

            int n   = hogData.getN();
            int dim = hogData.getDim();
            dos.writeInt(n);
            dos.writeInt(dim);

            for (int i = 0; i < n; i++)
                dos.writeInt(hogData.getY(i));

            for (int i = 0; i < n; i++)
                for (int j = 0; j < dim; j++)
                    dos.writeFloat(hogData.getX(i)[j]);

        } finally {
            dos.close();
        }
        System.out.println("SVMModel: salvat " + models.length
                         + " modele + " + hogData.getN()
                         + " vectori HOG in " + path);
    }

    public static Object[] loadAllWithHOG(String path) throws IOException {
        DataInputStream dis = new DataInputStream(
            new BufferedInputStream(new FileInputStream(path)));
        try {
            int nModels = dis.readInt();
            SVMModel[] models = new SVMModel[nModels];
            for (int mi = 0; mi < nModels; mi++) {
                SVMModel m   = new SVMModel();
                m.personName = dis.readUTF();
                m.b          = dis.readDouble();
                m.gamma      = dis.readDouble();
                m.coef0      = dis.readDouble();
                m.N          = dis.readInt();
                m.dim        = dis.readInt();
                m.alpha      = new double[m.N];
                for (int i = 0; i < m.N; i++) m.alpha[i] = dis.readDouble();
                m.labels     = new int[m.N];
                for (int i = 0; i < m.N; i++) m.labels[i] = dis.readInt();
                m.supportVectors = new float[m.N][m.dim];
                for (int i = 0; i < m.N; i++)
                    for (int j = 0; j < m.dim; j++)
                        m.supportVectors[i][j] = dis.readFloat();
                models[mi] = m;
            }

            int n   = dis.readInt();
            int dim = dis.readInt();
            model.TrainingData td = new model.TrainingData(n);
            io.Clasa cls1 = new io.Clasa("+1", 1, java.awt.Color.RED);
            io.Clasa cls0 = new io.Clasa("-1", 0, java.awt.Color.BLUE);
            int[] labels = new int[n];
            for (int i = 0; i < n; i++) labels[i] = dis.readInt();
            for (int i = 0; i < n; i++) {
                float[] x = new float[dim];
                for (int j = 0; j < dim; j++) x[j] = dis.readFloat();
                td.add(x, labels[i]);
            }

            return new Object[]{models, td};
        } finally {
            dis.close();
        }
    }

    @Override
    public String toString() {
        return "SVMModel[person=" + personName + ", N=" + N + ", dim=" + dim
             + ", gamma=" + gamma + ", coef0=" + coef0 + ", b=" + b + "]";
    }
}
