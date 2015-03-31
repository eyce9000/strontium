package srl.recognition.recognizer;

public interface IPipelinedRecognizer<Rec,Res,State> {
	public Res appendToRecognition(Rec rec);
	public State getRecognitionState();
	public void setRecognitionState(State state);
}
