package srl.core.sketch;

import java.util.List;

public interface IClassifiable {
	public void addInterpretation(Interpretation i);

	public Interpretation getInterpretation();

	public void setInterpretation(Interpretation i);

	public void setInterpretation(String label, double confidence);

	public void setLabel(String label);

	public void setNBestList(List<Interpretation> list);

	public List<Interpretation> getNBestList();
}
