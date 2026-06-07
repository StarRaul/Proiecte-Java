package alg;

import svm.SVM;

public class MPerceptron extends Algorithm {
    
    public MPerceptron(SVM svm) {
        super(svm);
        if (svm.ind.V != null) {
            name = "Median->Perceptron";
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
        
        System.out.println("MPERCEPTRON: learning rate = " + eta);
        svm.control.appendOutput("MPERCEPTRON: learning rate = " + eta + "\n\n");
        
        t = System.currentTimeMillis();
        if (!shouldContinue()) return;
        
        svm.control.logStart("Median->Perceptron");
        
        float[] M0 = new float[dim];
        float[] M1 = new float[dim];
        float[] w = new float[dim + 1];
        int k0 = 0, k1 = 0;
        
        for(int i = 0; i < N && shouldContinue(); i++) {
            if(svm.ind.V[i].cl.Y == 0) k0++; 
            else k1++;
        }
        if (!shouldContinue()) return;
        
        for(int j = 0; j < dim && shouldContinue(); j++) {
            for(int i = 0; i < N && shouldContinue(); i++) {
                if(svm.ind.V[i].cl.Y == 0) M0[j] += svm.ind.V[i].X[j];
                else M1[j] += svm.ind.V[i].X[j];
            }
        }
        if (!shouldContinue()) return;
        
        for(int j = 0; j < dim; j++) {
            M0[j] /= k0;
            M1[j] /= k1;
        }
        
        float[] X0 = new float[dim];
        for(int j = 0; j < dim; j++) {
            X0[j] = (M0[j] + M1[j]) / 2;
            w[j] = M1[j] - M0[j];
            w[dim] -= w[j] * X0[j];
        }
        
        int initialAccuracy = getAccuracy(w);
        svm.control.logMedianResult(k0, k1, initialAccuracy);
        
        if(dim == 2) {
            svm.design.setPointsOfLine(w);
            svm.design.repaint();
        }
        
        svm.control.showOutputData("Median", (System.currentTimeMillis() - t) + " ms", w);
        
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
                
                float activation = 0;
                for (int j = 0; j < dim; j++) activation += w[j] * svm.ind.V[i].X[j];
                activation += w[dim];
                
                int predicted = (activation >= 0) ? 1 : 0;
                int actual = svm.ind.V[i].cl.Y;
                
                if (predicted != actual) {
                    misclassified++;
                    totalUpdates++;
                    int error = actual - predicted;
                    for (int j = 0; j < dim; j++) w[j] += eta * error * svm.ind.V[i].X[j];
                    w[dim] += eta * error;
                }
            }
            
            if (!shouldContinue()) break;
            
            if (misclassified == 0) converged = true;
            epoch++;
            
            long epochTime = System.currentTimeMillis() - epochStart;
            int currentAccuracy = getAccuracy(w);
            
            svm.control.logPerceptronEpoch(epoch, misclassified, N, currentAccuracy, (int)epochTime);
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
        svm.control.logFinalResult(epoch, finalAccuracy, finalAccuracy - initialAccuracy, finalTime, converged, totalUpdates);
        
        if (dim == 2) svm.design.setPointsOfLine(w);
        
        svm.design.calculates = false;
        svm.design.repaint();
        svm.control.start.enable(false);
        svm.control.start.setLabel("Start");
        svm.control.init = true;
    }
}