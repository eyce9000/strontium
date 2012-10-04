/**
 * ComponentSubPart.java
 * 
 * Revision History:<br>
 * Aug 20, 2008 jbjohns - File created
 * 
 * <p>
 * 
 * <pre>
 * This work is released under the BSD License:
 * (C) 2008 Sketch Recognition Lab, Texas A&amp;M University (hereafter SRL @ TAMU)
 * All rights reserved.
 * 
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
package srl.recognition.constraint.domains;

/**
 * 
 * @author jbjohns
 */
public enum ComponentSubPart {

	/**
	 * No sub-part defined
	 */
	None,

	/**
	 * Left-most/Bottom(in case of a vertical line) point of a line
	 */
	End1,

	/**
	 * Right-most/Top(in case of vertical line) point of a line
	 */
	End2,

	/**
	 * Right-most endpoint of a line
	 */
	RightMostEnd,

	/**
	 * Left-most endpoint of a line
	 */
	LeftMostEnd,

	/**
	 * Top-most endpoint of a line
	 */
	TopMostEnd,

	/**
	 * Bottom-most endpoint of a line
	 */
	BottomMostEnd,

	/**
	 * Center point, either of a line or a bounding box
	 */
	Center,

	/**
	 * Top left corner of a bounding box
	 */
	TopLeft,

	/**
	 * Center of top side of bounding box
	 */
	TopCenter,

	/**
	 * Top right corner of a bounding box
	 */
	TopRight,

	/**
	 * Center of left side of bounding box
	 */
	CenterLeft,

	/**
	 * Center of right side of bounding box
	 */
	CenterRight,

	/**
	 * Bottom left corner of bounding box
	 */
	BottomLeft,

	/**
	 * Center of bottom side of bounding box
	 */
	BottomCenter,

	/**
	 * Bottom right corner of bounding box
	 */
	BottomRight;

	/**
	 * Get the enumeration type from the given string representation. Case
	 * insensitive. If your string does not match any valid string
	 * representations, will return {@link #None}. Use this method instead of
	 * {@link #valueOf(String)} allows us to default to {@link #None} if the
	 * value provided is not valid, and also allows equals ignoring case.
	 * 
	 * @param string
	 *            The string representation to get the enum type for, case
	 *            insensitive
	 * @return The enum type for the given string representation, or
	 *         {@link #None} if your string does not match a valid
	 *         representation
	 */
	public static ComponentSubPart fromString(String string) {
		String trimmedString = string.trim();

		ComponentSubPart ret = ComponentSubPart.None;

		// which one does it equal, ignoring case? loop over all the enum values
		// and check
		for (ComponentSubPart subPart : ComponentSubPart.values()) {
			if (subPart.toString().equalsIgnoreCase(trimmedString)) {
				ret = subPart;
				break;
			}
		}

		return ret;
	}
}
