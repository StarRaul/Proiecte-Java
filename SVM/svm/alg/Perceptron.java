package alg;

import svm.SVM;

public class Perceptron extends Algorithm {

    public Perceptron(SVM svm) {
        super(svm);
        if (svm.ind.V != null) {
            name = "Perceptron";
            svm.outd.algorithm = name;
            svm.outd.showInputData();

            svm.control.showInputData(
                svm.ind.input_file != null ? svm.ind.input_file : "",
                String.valueOf(N),
                String.valueOf(dim),
                name,
                String.valueOf(P)
            );
        }
    }

    public void run() {
        
        float eta = svm.settings.learning_rate;
        System.out.println(">>> PERCEPTRON USING ETA = " + eta);
        if (eta <= 0) eta = 0.1f;
        
        System.out.println("PERCEPTRON: learning rate = " + eta);
        svm.control.appendOutput("PERCEPTRON: learning rate = " + eta + "\n\n");
        
        t = System.currentTimeMillis();
        
        float[] w = new float[dim + 1];
        for (int i = 0; i <= dim; i++) w[i] = 0;
        
        int epoch = 0;
        boolean converged = false;
        int misclassified;
        
        svm.control.logStart("Perceptron");
        
        while (epoch < P && !converged && shouldContinue()) {
            long epochStart = System.currentTimeMillis();
            misclassified = 0;
            
            for (int i = 0; i < N && shouldContinue(); i++) {
                checkPause();
                if (!shouldContinue()) break;
                
                float activation = 0;
                for (int j = 0; j < dim; j++) activation += w[j] * svm.ind.V[i].X[j];
                activation += w[dim];
                
                int predicted = (activation >= 0) ? 1 : 0;
                int actual = svm.ind.V[i].cl.Y;
                
                if (predicted != actual) {
                    misclassified++;
                    int error = actual - predicted;
                    for (int j = 0; j < dim; j++) w[j] += eta * error * svm.ind.V[i].X[j];
                    w[dim] += eta * error;
                }
            }
            
            if (!shouldContinue()) break;
            
            if (misclassified == 0) converged = true;
            epoch++;
            
            long epochTime = System.currentTimeMillis() - epochStart;
            int accuracy = getAccuracy(w);
            
            svm.control.logPerceptronEpoch(epoch, misclassified, N, accuracy, (int)epochTime);
            svm.control.showOutputData(String.valueOf(epoch), (System.currentTimeMillis() - t) + " ms", w);
            
            if (dim == 2 && epoch % 10 == 0) {
                svm.design.setPointsOfLine(w);
                svm.design.repaint();
            }
        }
        
        long finalTime = System.currentTimeMillis() - t;
        int finalAccuracy = getAccuracy(w);
        
        svm.outd.stages_count = epoch;
        svm.outd.max_stages_count = P;
        svm.outd.computing_time = finalTime;
        svm.outd.w = w;
        svm.outd.accuracy = finalAccuracy;
        svm.outd.showOutputData();
        
        svm.control.showOutputData(String.valueOf(epoch), finalTime + " ms", w);
        svm.control.logFinalResult(epoch, finalAccuracy, 0, finalTime, converged, 0);
        
        if (dim == 2) svm.design.setPointsOfLine(w);
        
        svm.design.calculates = false;
        svm.design.repaint();
        svm.control.start.enable(false);
        svm.control.start.setLabel("Start");
        svm.control.init = true;
    }
}