package srl.core.sketch;

import java.util.Set;

import org.openawt.geom.AffineTransform;

public interface Transformable {
	public void applyTransform(AffineTransform xform, Set<Transformable> xformed);
}
