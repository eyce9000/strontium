/**
 * Author.java
 * 
 * Revision History:<br>
 * (5/23/08) bpaulson - class created<br>
 * (5/23/08) jbjohns - comments, change super() to this() in constructors and
 * force all setting of members into setters<br>
 * (5/27/08) awolin - added a setID function<br>
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
 * Class containing information about an author of a stroke (e.g. by whom it was
 * drawn, and their computer's information)
 * 
 * @author bpaulson
 * 
 */
public class Author implements Cloneable, Comparable<Author> {

	/**
	 * Unique ID of the author
	 */
	private UUID m_id = UUID.randomUUID();

	/**
	 * Dots per inch (DPI) in the X axis
	 */
	private Double m_dpiX;

	/**
	 * Dots per inch (DPI) in the Y axis
	 */
	private Double m_dpiY;

	/**
	 * Description/name of the author
	 */
	private String m_description;

	/**
	 * Default constructor
	 */
	public Author() {
		// Nothing to do
	}

	/**
	 * Constructor for author
	 * 
	 * @param description
	 *            description/name of the author
	 */
	public Author(String description) {
		setDescription(description);
	}

	/**
	 * Constructor for author
	 * 
	 * @param description
	 *            description/name of the author
	 * @param dpiX
	 *            dots per inch in the X axis
	 * @param dpiY
	 *            dots per inch in the Y axis
	 */
	public Author(String description, Double dpiX, Double dpiY) {
		setDescription(description);
		setDpi(dpiX, dpiY);
	}

	/**
	 * Copy constructor for Authors
	 * 
	 * @param author
	 *            Author to copy
	 */
	public Author(Author author) {
		if (author.getID() != null)
			setID(UUID.fromString(author.getID().toString()));
		if (author.getDpiX() != null)
			setDpi(author.getDpiX(), author.getDpiY());
		if (author.getDescription() != null)
			setDescription(new String(author.getDescription()));
	}

	/**
	 * Get the unique ID of the author
	 * 
	 * @return unique ID of the author
	 */
	public UUID getID() {
		return m_id;
	}

	/**
	 * Get the dots per inch in the X axis
	 * 
	 * @return dots per inch in the X axis
	 */
	public Double getDpiX() {
		return m_dpiX;
	}

	/**
	 * Get the dots per inch in the Y axis
	 * 
	 * @return dots per inch in the Y axis
	 */
	public Double getDpiY() {
		return m_dpiY;
	}

	/**
	 * Get the description/name of the author
	 * 
	 * @return description/name of the author
	 */
	public String getDescription() {
		return m_description;
	}

	/**
	 * Set the ID for the author
	 * 
	 * @param id
	 *            ID for the author
	 */
	public void setID(UUID id) {
		m_id = id;
	}

	/**
	 * Set the dots per inch
	 * 
	 * @param dpiX
	 *            dots per inch in the X axis
	 * @param dpiY
	 *            dots per inch in the Y axis
	 */
	public void setDpi(Double dpiX, Double dpiY) {
		m_dpiX = dpiX;
		m_dpiY = dpiY;
	}

	/**
	 * Set the dots per inch in the X axis
	 * 
	 * @param dpiX
	 *            dots per inch in the X axis
	 */
	public void setDpiX(Double dpiX) {
		m_dpiX = dpiX;
	}

	/**
	 * Set the dots per inch in the Y axis
	 * 
	 * @param dpiY
	 *            dots per inch in the Y axis
	 */
	public void setDpiY(Double dpiY) {
		m_dpiY = dpiY;
	}

	/**
	 * Set the description/name of the author
	 * 
	 * @param description
	 *            description/name of the author
	 */
	public void setDescription(String description) {
		m_description = description;
	}

	/**
	 * Returns whether two authors are equal by comparing their UUIDs.
	 * 
	 * @param obj
	 *            The object to compare to
	 * @return True if the two authors have the same UUID, false otherwise
	 */
	@Override
	public boolean equals(Object obj) {
		boolean ret = false;

		if (obj instanceof Author) {
			if (this == obj) {
				return true;
			} else {
				Author a = (Author) obj;
				ret = m_id.equals(a.getID());
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
		return new Author(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Author o) {
		return getID().compareTo(o.getID());
	}
}
