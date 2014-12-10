/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package UI;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.apache.log4j.Priority;
import org.apache.log4j.Level;

/**
 * Represents the controls for filtering, pausing, exiting, etc.
 *
 * @author <a href="mailto:oliver@puppycrawl.com">Oliver Burn</a>
 */
class ControlPanel extends JPanel {
    /**
	 * 
	 */
	private static final long serialVersionUID = -9207123545408690171L;

	/**
     * Creates a new <code>ControlPanel</code> instance.
     *
     * @param aModel the model to control
     */
    ControlPanel(MainWnd mainWnd, final MyTableModel aModel) {
        setBorder(BorderFactory.createTitledBorder("Controls: "));
        final GridBagLayout gridbag = new GridBagLayout();
        final GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);

        // Pad everything
        c.ipadx = 5;
        c.ipady = 5;

        // Add the 1st column of labels
        c.gridx = 0;
        c.gridy = 0;        
        c.anchor = GridBagConstraints.EAST;
        JLabel label = new JLabel("  过滤级别:");
        gridbag.setConstraints(label, c);
        add(label);

        c.gridx = 0;        
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;        
        label = new JLabel("  过滤线程:");
        gridbag.setConstraints(label, c);
        add(label);

        c.gridx = 2;        
        c.gridy = 1;
        c.anchor = GridBagConstraints.EAST;        
        label = new JLabel("  过滤记录器:");
        gridbag.setConstraints(label, c);
        add(label);


        c.gridx = 0;
        c.gridy = 2;        
        c.anchor = GridBagConstraints.EAST;         
        label = new JLabel("  过滤 NDC:");
        gridbag.setConstraints(label, c);
        add(label);

        c.gridx = 2;
        c.gridy=2;        
        c.anchor = GridBagConstraints.EAST;         
        label = new JLabel("  过滤消息体:");
        gridbag.setConstraints(label, c);
        add(label);

        // Add the 2nd column of filters
        c.weightx = 1;
        //c.weighty = 1;
        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;

        c.gridy = 0;
        final Level[] allPriorities = new Level[] {Level.FATAL, 
               Level.ERROR, 
               Level.WARN, 
			   Level.INFO, 
			   Level.DEBUG, 
			   Level.TRACE };
        
        final JComboBox priorities = new JComboBox(allPriorities);
        final Level lowest = allPriorities[allPriorities.length - 1];
        priorities.setSelectedItem(lowest);
        aModel.setPriorityFilter(lowest);
        gridbag.setConstraints(priorities, c);
        add(priorities);
        priorities.setEditable(false);
        priorities.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent aEvent) {
                    aModel.setPriorityFilter(
                        (Priority) priorities.getSelectedItem());
                }
            });


        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 1;        
        c.gridy = 1;
        final JTextField threadField = new JTextField("");
        threadField.getDocument().addDocumentListener(new DocumentListener () {
                @Override
				public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setThreadFilter(threadField.getText());
                }
                @Override
				public void removeUpdate(DocumentEvent aEvente) {
                    aModel.setThreadFilter(threadField.getText());
                }
                @Override
				public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setThreadFilter(threadField.getText());
                }
            });
        gridbag.setConstraints(threadField, c);
        add(threadField);

        c.gridx = 3;        
        c.gridy = 1;
        final JTextField catField = new JTextField("");
        catField.getDocument().addDocumentListener(new DocumentListener () {
                @Override
				public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setCategoryFilter(catField.getText());
                }
                @Override
				public void removeUpdate(DocumentEvent aEvent) {
                    aModel.setCategoryFilter(catField.getText());
                }
                @Override
				public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setCategoryFilter(catField.getText());
                }
            });
        gridbag.setConstraints(catField, c);
        add(catField);

        c.gridx = 1;        
        c.gridy = 2;
        final JTextField ndcField = new JTextField("");
        ndcField.getDocument().addDocumentListener(new DocumentListener () {
                @Override
				public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setNDCFilter(ndcField.getText());
                }
                @Override
				public void removeUpdate(DocumentEvent aEvent) {
                    aModel.setNDCFilter(ndcField.getText());
                }
                @Override
				public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setNDCFilter(ndcField.getText());
                }
            });
        gridbag.setConstraints(ndcField, c);
        add(ndcField);

        c.gridx = 3;        
        c.gridy = 2;
        final JTextField msgField = new JTextField("");
        msgField.getDocument().addDocumentListener(new DocumentListener () {
                @Override
				public void insertUpdate(DocumentEvent aEvent) {
                    aModel.setMessageFilter(msgField.getText());
                }
                @Override
				public void removeUpdate(DocumentEvent aEvent) {
                    aModel.setMessageFilter(msgField.getText());
                }
                @Override
				public void changedUpdate(DocumentEvent aEvent) {
                    aModel.setMessageFilter(msgField.getText());
                }
            });


        gridbag.setConstraints(msgField, c);
        add(msgField);

        // Add the 3rd column of buttons
        c.weightx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;
        c.gridx = 4;

        c.gridy = 0;
        final JButton exitButton = new JButton("退出");
        exitButton.setMnemonic('x');
        exitButton.addActionListener(mainWnd.GetExistActionListener());
        gridbag.setConstraints(exitButton, c);
        add(exitButton);

        c.gridy++;
        final JButton clearButton = new JButton("清除");
        clearButton.setMnemonic('c');
        clearButton.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent aEvent) {
                    aModel.clear();
                }
            });
        gridbag.setConstraints(clearButton, c);
        add(clearButton);

        c.gridy++;
        final JButton toggleButton = new JButton("暂停");
        toggleButton.setMnemonic('p');
        toggleButton.addActionListener(new ActionListener() {
                @Override
				public void actionPerformed(ActionEvent aEvent) {
                    aModel.toggle();
                    toggleButton.setText(
                        aModel.isPaused() ? "恢复" : "暂停");
                }
            });
        gridbag.setConstraints(toggleButton, c);
        add(toggleButton);
    }
}
