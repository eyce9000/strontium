package srl.patternrec.crossvalidation;

import java.util.ArrayList;
import java.util.List;

public class KFoldList<T> {
	private List<List<T>> testing;
	private List<List<T>> training;
	private int k = 0;
	public KFoldList(List<T> values, int k){
		this.k = k;
		testing = new ArrayList<List<T>>();
		training = new ArrayList<List<T>>();
		
		int size = values.size();
		
		
		
		
		int total = 0;
		for(int i=0; i<k; i++){
			int width = size/k;
			if(i==k-1){
				width = size - total;
			}
			List<T> selectedTesting = new ArrayList<T>(values.subList(total, total + width));
			List<T> selectedTraining = new ArrayList<T>();
			if(total>0)
				selectedTraining.addAll(values.subList(0, total));
			if(total+width<size)
				selectedTraining.addAll(values.subList(total+width, size));
			total += width;
			
			testing.add(selectedTesting);
			training.add(selectedTraining);
		}
	}
	public int getK(){
		return k;
	}
	public List<T> getTesting(int fold){
		return testing.get(fold);
	}
	public List<T> getTraining(int fold){
		return training.get(fold);
	}
}
