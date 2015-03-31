package srl.recognition.constraint.confidence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import srl.recognition.constraint.IConstrainable;
import srl.recognition.constraint.constrainable.ConstrainableShape;

/**
 * Constraint to see if the given text is contained within a shape.
 * 
 * @author jbjohns
 */
public class ContainsTextConstraint extends AbstractConfidenceConstraint {
	
	private static final Logger log = LoggerFactory
	        .getLogger(ContainsTextConstraint.class);
	
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.ladder.recognition.constraint.IConstraint#newInstance()
	 */
	@Override
	public ContainsTextConstraint newInstance() {
		return new ContainsTextConstraint();
	}
	
	/**
	 * Name of this constraint
	 */
	public static final String NAME = "ContainsText";
	
	/**
	 * Description of this constraint
	 */
	public static final String DESCRIPTION = "The shape contains some text.";
	
	/**
	 * Number of parameters this constraint uses.
	 */
	public static final int NUM_PARAMETERS = 2;
	
	/**
	 * Default threshold for this constraint
	 */
	public static final double DEFAULT_THRESHOLD = 7.5;
	
	
	/**
	 * Construct the constraint with {@link #DEFAULT_THRESHOLD}
	 */
	public ContainsTextConstraint() {
		this(DEFAULT_THRESHOLD);
	}
	

	public ContainsTextConstraint(double threshold) {
		super(NAME, DESCRIPTION, NUM_PARAMETERS, threshold);
	}
	

	public double solve(ConstrainableShape textShape, String textMatch) {
		String confidenceString = textShape.getShape().getAttribute(textMatch);
		if (confidenceString == null) {
			return 0;
		}
		return Double.parseDouble(confidenceString);
	}
	

	public double solve(IConstrainable textShape, IConstrainable textMatch) {
		return solve((ConstrainableShape) textShape, textMatch.getShapeType());
	}
	

	@Override
	public double solve() {
		if (getParameters() == null
		    || getParameters().size() < getNumRequiredParameters()) {
			log.debug("ContainsText requires " + getNumRequiredParameters()
			          + " arguments");
			return 0;
		}
		
		IConstrainable shape1 = getParameters().get(0);
		IConstrainable shape2 = getParameters().get(1);
		
		return solve(shape1, shape2);
	}
	

	/**
	 * @Override public boolean isBinaryConstraint() { return true; }
	 **/
	
	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.ladder.recognition.constraint.confidence.AbstractConstraint#
	 * isClearlyFalse(double)
	 */
	@Override
	public boolean isClearlyFalse(double value) {
		// TODO Auto-generated method stub
		return value < -1.0;
	}
	
}
