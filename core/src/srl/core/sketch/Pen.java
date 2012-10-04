/**
 * Pen.java
 * 
 * Revision History:<br>
 * (5/23/08) bpaulson - class created<br>
 * (5/27/08) awolin - added a setID
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
package srl.core.sketch;

import java.util.UUID;

/**
 * Class containing information about a particular pen used to create a stroke
 * 
 * @author bpaulson
 * 
 */
public class Pen implements Cloneable, Comparable<Pen> {

	/**
	 * Unique ID number for this pen
	 */
	private UUID m_id = UUID.randomUUID();

	/**
	 * Manufacturer's pen ID number
	 */
	private String m_penID = null;

	/**
	 * Brand of the pen
	 */
	private String m_brand = null;

	/**
	 * Description of the pen
	 */
	private String m_description = null;

	/**
	 * Default constructor
	 */
	public Pen() {
		// Nothing to do
	}

	/**
	 * Constructor for pen
	 * 
	 * @param penID
	 *            manufacturer's pen ID
	 * @param brand
	 *            brand of the pen
	 * @param description
	 *            description of the pen
	 */
	public Pen(String penID, String brand, String description) {
		setPenID(penID);
		setBrand(brand);
		setDescription(description);
	}

	/**
	 * Copy constructor for Pens
	 * 
	 * @param pen
	 *            Pen to copy
	 */
	public Pen(Pen pen) {
		if (pen.getID() != null)
			setID(UUID.fromString(pen.getID().toString()));
		if (pen.getPenID() != null)
			setPenID(new String(pen.getPenID()));
		if (pen.getBrand() != null)
			setBrand(new String(pen.getBrand()));
		if (pen.getDescription() != null)
			setDescription(new String(pen.getDescription()));
	}

	/**
	 * Get the unique ID number of the pen
	 * 
	 * @return unique ID number of the pen
	 */
	public UUID getID() {
		return m_id;
	}

	/**
	 * Get the manufacturer's pen ID number (this is not the same as the pen's
	 * UUID)
	 * 
	 * @return manufacturer's pen ID number
	 */
	public String getPenID() {
		return m_penID;
	}

	/**
	 * Get the brand name of the pen
	 * 
	 * @return brand name of the pen
	 */
	public String getBrand() {
		return m_brand;
	}

	/**
	 * Get the description of the pen
	 * 
	 * @return description of the pen
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * Set the ID of the pen
	 * 
	 * @param id
	 *            ID of the pen
	 */
	public void setID(UUID id) {
		m_id = id;
	}

	/**
	 * Set the manufacturer's pen ID
	 * 
	 * @param penID
	 *            manufacturer's pen ID
	 */
	public void setPenID(String penID) {
		m_penID = penID;
	}

	/**
	 * Set the brand of the pen
	 * 
	 * @param brand
	 *            brand of the pen
	 */
	public void setBrand(String brand) {
		m_brand = brand;
	}

	/**
	 * Set the description of the pen
	 * 
	 * @param description
	 *            description of the pen
	 */
	public void setDescription(String description) {
		m_description = description;
	}

	/**
	 * Returns whether two pens are equal by comparing their UUIDs.
	 * 
	 * @param obj
	 *            The object to compare to
	 * @return True if the two pens have the same UUID, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;

		if (obj instanceof Pen) {
			if (this == obj) {
				return true;
			} else {
				Pen p = (Pen) obj;
				ret = m_id.equals(p.getID());
			}
		}

		return ret;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Object clone() {
		return new Pen(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Pen o) {
		return getDescription().compareTo(o.getDescription());
	}
}
