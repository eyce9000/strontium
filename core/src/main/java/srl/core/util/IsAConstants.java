/**
 * IsAConstants.java
 * 
 * Revision History:<br>
 * Feb 24, 2009 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sketch Recognition Lab, Texas A&amp;M University 
 *       nor the names of its contributors may be used to endorse or promote 
 *       products derived from this software without specific prior written 
 *       permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
package srl.core.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class defines several constants that are used to denote abstract shape
 * types (isA relationships).
 * 
 * @author jbjohns
 */
public class IsAConstants {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory
			.getLogger(IsAConstants.class);

	/**
	 * Made of dashed lines
	 */
	public static final String DASHED = "DashedLines";

	/**
	 * A primitive shape
	 */
	public static final String PRIMITIVE = "Primitive";

	/**
	 * A closed shape
	 */
	public static final String CLOSED = "Closed";

	/**
	 * Mid-level shapes
	 */
	public static final String MID_LEVEL_SHAPE = "MidLevelShape";

	/**
	 * Brigade modifier
	 */
	public static final String BRIGADE_MODIFIER = "BDE";

	/**
	 * Company modifier
	 */
	public static final String COMPANY_MODIFIER = "Company";

	/**
	 * Battalion modifier
	 */
	public static final String BATTALION_MODIFIER = "Battalion";

	/**
	 * Platoon modifier
	 */
	public static final String PLATOON_MODIFIER = "Platoon";

	/**
	 * Reinforced modifier
	 */
	public static final String REINFORCED_MODIFIER = "Reinfored";

	/**
	 * Reduced modifier
	 */
	public static final String REDUCED_MODIFIER = "Reduced";

	/**
	 * Reflectively put together a list of all the constants declared in this
	 * class.
	 */
	private static final SortedSet<String> IS_A_CONSTANTS = new TreeSet<String>();
	static {
		// The Class object for this class
		Class<IsAConstants> isAConstantsClass = IsAConstants.class;
		// Loop over all the fields declared in this class
		Field[] isAConstantsFields = isAConstantsClass.getDeclaredFields();
		for (Field isAConstantsField : isAConstantsFields) {
			// The modifiers declared for this field.
			int fieldModifiers = isAConstantsField.getModifiers();

			// is a public static final String?
			boolean isPublic = Modifier.isPublic(fieldModifiers);
			boolean isStatic = Modifier.isStatic(fieldModifiers);
			boolean isFinal = Modifier.isFinal(fieldModifiers);
			boolean isString = isAConstantsField.getType().equals(String.class);

			// if public static final string, put into our set
			if (isPublic && isStatic && isFinal && isString) {
				try {
					IS_A_CONSTANTS.add((String) isAConstantsField.get(null));
				} catch (IllegalArgumentException e) {
					log.error("Error while instantiating set of IsAConstants:",
							e);
				} catch (IllegalAccessException e) {
					log.error("Error while instantiating set of IsAConstants:",
							e);
				}
			}
		}
	}

	/**
	 * Get an unmodifiable set that is the set of all constants declared in this
	 * class.
	 * 
	 * @return The set of all the constants declared in this class. The set is
	 *         unmodifiable.
	 */
	public static SortedSet<String> getConstantsSet() {
		return Collections.unmodifiableSortedSet(IS_A_CONSTANTS);
	}
}
