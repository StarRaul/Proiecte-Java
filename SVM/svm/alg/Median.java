package alg;

import svm.SVM;

public class Median extends Algorithm{

	public Median(SVM svm){
		super(svm);
		if(svm.ind.V != null){
			name = "Median";
			svm.outd.algorithm = name;
			svm.outd.max_stages_count = 1;
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
	
	public void run(){
		t = System.currentTimeMillis();
		
		svm.control.clearOutput();
		svm.control.logStart("Median");
		
		float[] M0 = new float[dim];
		float[] M1 = new float[dim];
		float[] w = new float[dim+1];
		int k0 = 0, k1 = 0;
		
		for(int i = 0; i < N; i++)
			if(svm.ind.V[i].cl.Y == 0) k0++; else k1++;
		
		for(int j = 0; j < dim; j++)
			for(int i = 0; i < N; i++)
				if(svm.ind.V[i].cl.Y == 0) 
					M0[j] += svm.ind.V[i].X[j];
				else 
					M1[j] += svm.ind.V[i].X[j];
		
		for(int j = 0; j < dim; j++){
			M0[j] /= k0;
			M1[j] /= k1;
		}
		
		float[] X0 = new float[dim];
		for(int j = 0; j < dim; j++){
			X0[j] = (M0[j] + M1[j])/2;
			w[j] =  M1[j] - M0[j];
			w[dim] -= w[j] * X0[j];
		}
		
		int accuracy = getAccuracy(w);
		
		svm.control.logMedianResult(k0, k1, accuracy);
		
		if(dim==2) svm.design.setPointsOfLine(w);
		
		long computingTime = System.currentTimeMillis() - t;
		
		svm.outd.stages_count = 1;
		svm.outd.max_stages_count = 1;
		svm.outd.computing_time = computingTime;
		svm.outd.w = w;
		svm.outd.accuracy = accuracy;
		svm.outd.showInputData();
		svm.outd.showOutputData();
		
		svm.control.showOutputData("1", computingTime + " ms", w);
		
		svm.control.appendOutput(String.format("\n=== %d | %d%% | %dms | DONE\n", 1, accuracy, computingTime));
		
		svm.design.calculates = false;
		svm.design.repaint();
		svm.control.start.enable(false);
		svm.control.start.setLabel("Start");
		svm.control.init = true;
	}
}