package gui;

import java.awt.*;
import java.awt.event.*;
import svm.SVM;

public class SimulationControl extends Dialog{
	private int totalEpochs = 0;
	private int totalMisclassified = 0;
	private int totalAccuracy = 0;
	private long totalTime = 0;
	SVM svm;
	public TextArea ta, ta1;
	public Button start, options, reserved;
	public Button view, test, reserved1;
	public Label ilabel1, ilabel2, ilabel2_, ilabel3, ilabel3_, ilabel4, ilabel4_, ilabel5, ilabel5_, ilabel6, ilabel6_;
	public Label olabel1, olabel2, olabel2_, olabel3, olabel3_, olabel4;
	public boolean init = true;
	
	private StringBuilder outputBuffer = new StringBuilder();
	
	public SimulationControl(SVM svm, int width, int height){
		super(svm, "Simulation Control", false);
		this.svm = svm;
		setBackground(svm.settings.background_color_default);
		setResizable(false);
		setLayout(null);
		setSize(width, height);
		
		int a = 30;
		ilabel1 = new Label("Input Data");
		ilabel1.setBounds(20,a,110,20);
		ilabel1.setForeground(new Color(255,200,0));
		add(ilabel1);
		
		a+=20;
		ilabel2 = new Label("Input Data File:");
		ilabel2.setBounds(20,a,110,20);
		ilabel2.setForeground(Color.white);
		add(ilabel2);
		ilabel2_ = new Label("");
		ilabel2_.setBounds(130,a,250,20);
		ilabel2_.setForeground(Color.white);
		add(ilabel2_);	
		
		a+=20;
		ilabel3 = new Label("Vectors Count:");
		ilabel3.setBounds(20,a,110,20);
		ilabel3.setForeground(Color.white);
		add(ilabel3);
		ilabel3_ = new Label("");
		ilabel3_.setBounds(130,a,110,20);
		ilabel3_.setForeground(Color.white);
		add(ilabel3_);	
		
		a+=20;
		ilabel4 = new Label("Attributes Count:");
		ilabel4.setBounds(20,a,110,20);
		ilabel4.setForeground(Color.white);
		add(ilabel4);
		ilabel4_ = new Label("");
		ilabel4_.setBounds(130,a,110,20);
		ilabel4_.setForeground(Color.white);
		add(ilabel4_);	
		
		a+=20;
		ilabel5 = new Label("Algorithm:");
		ilabel5.setBounds(20,a,110,20);
		ilabel5.setForeground(Color.white);
		add(ilabel5);
		ilabel5_ = new Label("");
		ilabel5_.setBounds(130,a,200,20);
		ilabel5_.setForeground(Color.white);
		add(ilabel5_);	
		
		a+=20;
		ilabel6 = new Label("Max Stages:");
		ilabel6.setBounds(20,a,110,20);
		ilabel6.setForeground(Color.white);
		add(ilabel6);
		ilabel6_ = new Label("");
		ilabel6_.setBounds(130,a,110,20);
		ilabel6_.setForeground(Color.white);
		add(ilabel6_);			
		
		a+=28;	
		start = new Button("Start");
		start.setBounds((width-360)/2,a,110,30);
		start.setBackground(svm.settings.button_color_default);
		start.setForeground(svm.settings.button_label_default);		
		add(start);
		start.enable(false);

		options = new Button("Options");
		options.setBounds((width-360)/2+120,a,110,30);
		options.setBackground(svm.settings.button_color_default);
		options.setForeground(svm.settings.button_label_default);		
		add(options);
		
		reserved = new Button("Clear");
		reserved.setBounds((width-360)/2+240,a,110,30);
		reserved.setBackground(svm.settings.button_color_default);
		reserved.setForeground(svm.settings.button_label_default);		
		add(reserved);		

		ta = new TextArea("", 12, 50, TextArea.SCROLLBARS_VERTICAL_ONLY);
		ta.setBounds(10,200,width-20,height-450);
		ta.setBackground(svm.settings.button_color_default);
		ta.setForeground(svm.settings.string_color_default);
		ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
		add(ta);
		
		int b = height-195;
		olabel1 = new Label("Output");
		olabel1.setBounds(20,b,110,20);
		olabel1.setForeground(new Color(255,200,0));
		add(olabel1);
		
		b+=20;
		olabel2 = new Label("Stages:");
		olabel2.setBounds(20,b,110,20);
		olabel2.setForeground(Color.white);
		add(olabel2);	
		olabel2_ = new Label("");
		olabel2_.setBounds(130,b,110,20);
		olabel2_.setForeground(Color.white);
		add(olabel2_);	

		b+=20;
		olabel3 = new Label("Time:");
		olabel3.setBounds(20,b,110,20);
		olabel3.setForeground(Color.white);
		add(olabel3);	
		olabel3_ = new Label("");
		olabel3_.setBounds(130,b,110,20);
		olabel3_.setForeground(Color.white);
		add(olabel3_);	

		b+=20;
		olabel4 = new Label("Classifier:");
		olabel4.setBounds(20,b,110,20);
		olabel4.setForeground(new Color(255,200,0));
		add(olabel4);
		
		b+=28;
		ta1 = new TextArea("");
		ta1.setBounds(10,b,width-20,50);
		ta1.setBackground(svm.settings.button_color_default);
		ta1.setForeground(svm.settings.string_color_default);
		ta1.setEditable(false);
		add(ta1);		

		view = new Button("View Output");
		view.setBounds((width-360)/2,height-50,110,30);
		view.setBackground(svm.settings.button_color_default);
		view.setForeground(svm.settings.button_label_default);
		add(view);	
		
		test = new Button("Test");
		test.setBounds((width-360)/2+120,height-50,110,30);
		test.setBackground(svm.settings.button_color_default);
		test.setForeground(svm.settings.button_label_default);
		add(test);			

		reserved1 = new Button("...");
		reserved1.setBounds((width-360)/2+240,height-50,110,30);
		reserved1.setBackground(svm.settings.button_color_default);
		reserved1.setForeground(svm.settings.button_label_default);
		add(reserved1);	
	}
	
	public void showInputData(String inputFile, String vectors_count, String vectors_dim, String algorithm, String max_epochs){
		ilabel2_.setText(inputFile);
		ilabel3_.setText(vectors_count);
		ilabel4_.setText(vectors_dim);
		ilabel5_.setText(algorithm);
		ilabel6_.setText(max_epochs);
	}
	
	public void showOutputData(String epocs_count, String computing_time, float[] w){
		olabel2_.setText(epocs_count);
		olabel3_.setText(computing_time);
		if(w!=null) {
			String s = w[0] + " * x0 ";
			for(int j = 1; j < w.length-1; j++) {
				if(w[j] != 0) s += (w[j]<0 ? " - " : " + ") + Math.abs(w[j]) + " * x" + j;
			}
			if(w[w.length-1] != 0) s += (w[w.length-1]<0 ? " - " : " + ") + Math.abs(w[w.length-1]);
			s += " < 0 ? 0 : 1";
			ta1.setText(s);
		}
	}
	
	public void clearOutput() {
		outputBuffer = new StringBuilder();
		ta.setText("");
		totalEpochs = 0;
		totalMisclassified = 0;
		totalAccuracy = 0;
		totalTime = 0;
	}
	
	public void appendOutput(String text) {
		outputBuffer.append(text);
		ta.setText(outputBuffer.toString());
		ta.setCaretPosition(ta.getText().length());
	}
	
	public void logPerceptronEpoch(int epoch, int misclassified, int totalVectors, int accuracy, int epochTimeMs) {
		
		totalEpochs++;
		totalMisclassified += misclassified;
		totalAccuracy += accuracy;
		totalTime += epochTimeMs;
		
		String line = String.format("%d | %d/%d (%.1f%%) | %d%% | %dms\n",
			epoch, misclassified, totalVectors, (misclassified * 100.0f / totalVectors), accuracy, epochTimeMs);
		appendOutput(line);
	}
	
	public void logMedianResult(int k0, int k1, int accuracy) {
		String line = String.format("Median | %d/%d (%.1f%%) | %d%%\n\n",
			k0, k1, (k0 * 100.0f / (k0 + k1)), accuracy);
		appendOutput(line);
	}
	
	public void logFinalResult(int epochs, int accuracy, int improvement, long totalTimeMs, boolean converged, int updates) {
		
		float avgMisclassified = (totalEpochs > 0) ? (float)totalMisclassified / totalEpochs : 0;
		float avgAccuracy = (totalEpochs > 0) ? (float)totalAccuracy / totalEpochs : 0;
		float avgTime = (totalEpochs > 0) ? (float)totalTime / totalEpochs : 0;
		
		String line = String.format(
			"\n=== FINAL STATS ===\n" +
			"Final Epoch: %d\n" +
			"Avg Misclassified: %.1f/800 (%.1f%%)\n" +
			"Avg Accuracy: %.1f%%\n" +
			"Avg Time per Epoch: %.1fms\n" +
			"Total Time: %dms\n" +
			"Total Updates: %d\n" +
			"Final Accuracy: %d%% (%+d%%)\n" +
			"Status: %s\n",
			epochs, avgMisclassified, (avgMisclassified * 100.0f / 800), avgAccuracy, avgTime,
			totalTimeMs, updates, accuracy, improvement, converged ? "CONVERGED" : "MAX EPOCHS"
		);
		appendOutput(line);
	}
	
	public void logStart(String algorithmName) {
		appendOutput(algorithmName + "\n\n");
	}
	
	public void start_simulation(){
		if(svm.ind.input_file == null){
			ilabel2_.setText("Please load an input data file.");
		}else{
			if(svm.algorithm == null){
				ilabel5_.setText("Please select an algorithm.");
			}else{	
				if(start.getLabel().startsWith("Start")){
					if(init){
						clearOutput();
						svm.algorithm.start_simulation();
						init = false;
					}else svm.algorithm.resume_();
					start.setLabel("Stop");
				}else{
					svm.algorithm.suspend_();
					start.setLabel("Start");
				}			
			}
		}		
	}
	
	public boolean handleEvent(Event e){
		if(e.id==Event.WINDOW_DESTROY){
			svm.mb.getMenu(2).getItem(0).setLabel("Show Simulation Control");
			dispose();
		}else if(e.id==Event.ACTION_EVENT && e.target == reserved){
			clearOutput();
           	return true;				
		}else if(e.id==Event.ACTION_EVENT && e.target == start){
			start_simulation();
           	return true;	
		}else if(e.id==Event.ACTION_EVENT && e.target == options){
			svm.options.setValue();
			svm.options.eta_tf.setText(""+svm.settings.learning_rate);
			svm.options.show();
           	return true;			
		}else if(e.id==Event.ACTION_EVENT && e.target == view){
			svm.outd.show();
           	return true;				
		}	
		return super.handleEvent(e);
	}		
}