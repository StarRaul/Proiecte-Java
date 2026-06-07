package alg;

import svm.SVM;

public class DualPerceptron extends Algorithm {
    
    public DualPerceptron(SVM svm) {
        super(svm);
        if (svm.ind.V != null) {
            name = "Dual Perceptron";
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
        if (eta <= 0) eta = 0.1f;
        
        System.out.println("DUAL PERCEPTRON: learning rate = " + eta);
        svm.control.appendOutput("DUAL PERCEPTRON: learning rate = " + eta + "\n\n");
        
        t = System.currentTimeMillis();
        
        svm.control.clearOutput();
        svm.control.appendOutput("Dual Perceptron\n\n");
        svm.control.appendOutput(String.format("Learning rate η = %.4f\n\n", eta));
        
        int[] y = new int[N];
        for (int i = 0; i < N; i++) {
            y[i] = (svm.ind.V[i].cl.Y == 1) ? 1 : -1;
        }
        
        float[] alpha = new float[N];
        
        float[] w = new float[dim + 1];
        
        int epoch = 0;
        boolean converged = false;
        int misclassified;
        int totalUpdates = 0;
        
        while (epoch < P && !converged && shouldContinue()) {
            long epochStart = System.currentTimeMillis();
            misclassified = 0;
            
            for (int i = 0; i < N && shouldContinue(); i++) {
                checkPause();
                if (!shouldContinue()) break;
                
                float sum = 0;
                for (int d = 0; d < dim; d++) {
                    sum += w[d] * svm.ind.V[i].X[d];
                }
                sum += w[dim];
                
                int predicted = (sum >= 0) ? 1 : -1;
                
                if (predicted != y[i]) {
                    misclassified++;
                    totalUpdates++;
                    
                    alpha[i] += eta;
                    
                    for (int d = 0; d < dim; d++) {
                        w[d] += eta * y[i] * svm.ind.V[i].X[d];
                    }
                    w[dim] += eta * y[i];
                }
            }
            
            if (!shouldContinue()) break;
            
            if (misclassified == 0) {
                converged = true;
            }
            
            epoch++;
            
            long epochTime = System.currentTimeMillis() - epochStart;
            int accuracy = getAccuracy(w);
            
            int supportVectors = 0;
            for (int i = 0; i < N; i++) {
                if (alpha[i] != 0) supportVectors++;
            }
            
            String line = String.format("%d | %d/%d (%.1f%%) | %d%% | SV:%d | η=%.4f | %dms\n",
                epoch, misclassified, N, (misclassified * 100.0f / N),
                accuracy, supportVectors, eta, epochTime);
            svm.control.appendOutput(line);
            
            svm.control.showOutputData(String.valueOf(epoch), (System.currentTimeMillis() - t) + " ms", w);
            
            if (dim == 2) {
                svm.design.setPointsOfLine(w);
                svm.design.repaint();
            }
        }
        
        long finalTime = System.currentTimeMillis() - t;
        int finalAccuracy = getAccuracy(w);
        
        int finalSupportVectors = 0;
        for (int i = 0; i < N; i++) {
            if (alpha[i] != 0) finalSupportVectors++;
        }
        
        svm.outd.stages_count = epoch;
        svm.outd.max_stages_count = P;
        svm.outd.computing_time = finalTime;
        svm.outd.w = w;
        svm.outd.accuracy = finalAccuracy;
        svm.outd.showOutputData();
        
        svm.control.showOutputData(String.valueOf(epoch), finalTime + " ms", w);
        
        String result = String.format(
            "\n=== DUAL PERCEPTRON ===\n" +
            "Learning Rate (η): %.4f\n" +
            "Epochs: %d\n" +
            "Accuracy: %d%%\n" +
            "Support Vectors: %d/%d (%.1f%%)\n" +
            "Total Updates: %d\n" +
            "Time: %dms\n" +
            "Status: %s\n",
            eta, epoch, finalAccuracy, finalSupportVectors, N, (finalSupportVectors * 100.0f / N),
            totalUpdates, finalTime, converged ? "CONVERGED" : "MAX EPOCHS"
        );
        
        svm.control.appendOutput(result);
        
        if (dim == 2) {
            svm.design.setPointsOfLine(w);
        }
        
        svm.design.calculates = false;
        svm.design.repaint();
        svm.control.start.enable(false);
        svm.control.start.setLabel("Start");
        svm.control.init = true;
    }
}