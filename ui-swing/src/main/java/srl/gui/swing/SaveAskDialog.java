/*******************************************************************************
 *  Revision History:<br>
 *  SRL Member - File created
 *
 *  <p>
 *  <pre>
 *  This work is released under the BSD License:
 *  (C) 2012 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
 *        nor the names of its contributors may be used to endorse or promote 
 *        products derived from this software without specific prior written 
 *        permission.
 *  
 *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  </pre>
 *  
 *******************************************************************************/
package srl.gui.swing;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

public class SaveAskDialog {

	/**
	 * Global variables
	 */
	private boolean m_isKeyPressed;
	private Object m_selectedValue;

	public SaveAskDialog() {
		m_isKeyPressed = false;
		m_selectedValue = null;
	}

	/**
	 * Show Option Dialog replaces JOptionPane.showConfirmDialog so that arrow
	 * keys can be used to select buttons in the dialog
	 */
	public int showOptionDialog(Component parentComponent, Object message,
			String title, int optionType, int messageType, Icon icon,
			Object[] options, Object initialValue) {

		// the JOptionPane to modify
		JOptionPane optionPane = new JOptionPane(message, messageType,
				optionType, icon, options, initialValue);

		optionPane.setInitialValue(initialValue);

		HashSet<AWTKeyStroke> forwardSet = new HashSet<AWTKeyStroke>();
		forwardSet
				.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_RIGHT, 0, true));
		forwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_UP, 0, true));
		forwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0, true));

		optionPane.setFocusTraversalKeys(
				KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forwardSet);

		HashSet<AWTKeyStroke> backwardSet = new HashSet<AWTKeyStroke>();
		backwardSet.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB,
				InputEvent.SHIFT_DOWN_MASK, true));
		backwardSet
				.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_LEFT, 0, true));
		backwardSet
				.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_DOWN, 0, true));

		optionPane.setFocusTraversalKeys(
				KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backwardSet);

		final JDialog dialog = optionPane.createDialog(parentComponent, title);

		ActionListener yesActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_isKeyPressed = true;
				dialog.dispose();
				m_selectedValue = new Integer(0);
			}
		};

		KeyStroke yKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y, 0, true);
		optionPane.registerKeyboardAction(yesActionListener, yKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		ActionListener noActionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_isKeyPressed = true;
				dialog.dispose();
				m_selectedValue = new Integer(1);
			}
		};

		KeyStroke nKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_N, 0, true);
		optionPane.registerKeyboardAction(noActionListener, nKeyStroke,
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		optionPane.selectInitialValue();

		dialog.setVisible(true);

		if (!m_isKeyPressed) {
			dialog.dispose();
			m_selectedValue = optionPane.getValue();

			m_isKeyPressed = false;
		} else {
			m_isKeyPressed = false;
			return ((Integer) m_selectedValue).intValue();
		}

		if (m_selectedValue == null) {
			return JOptionPane.CLOSED_OPTION;
		}

		if (options == null) {
			if (m_selectedValue instanceof Integer) {
				return ((Integer) m_selectedValue).intValue();
			}
			return JOptionPane.CLOSED_OPTION;
		}

		for (int counter = 0, maxCounter = options.length; counter < maxCounter; counter++) {
			if (options[counter].equals(m_selectedValue)) {
				return counter;
			}
		}
		return JOptionPane.CLOSED_OPTION;
	}
}