/*
 * Copyright 2015 Idrees Hassan, All Rights Reserved
 */
package com.idreesinc.gemini;

/**
 *
 * @author Idrees Hassan
 */
public class Console extends javax.swing.JPanel {

    /**
     * Creates new form Console
     */
    public Console() {
        initComponents();
    }

    /**
     * Sets the text of the console's text field.
     *
     * @param text The text to set
     */
    public void setConsoleText(String text) {
        txtConsole.setText(text);
    }

    /**
     * Appends the given text to the console's text.
     *
     * @param text The text to append
     */
    public void addConsoleText(String text) {
        setConsoleText(txtConsole.getText() + "\n" + text);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        txtConsole = new javax.swing.JTextPane();
        cbCommand = new javax.swing.JComboBox();
        btnCommand = new javax.swing.JButton();

        setPreferredSize(new java.awt.Dimension(478, 365));

        txtConsole.setEditable(false);
        txtConsole.setBackground(new java.awt.Color(0, 153, 204));
        txtConsole.setForeground(new java.awt.Color(255, 255, 255));
        txtConsole.setText("Twin Activated");
        txtConsole.setToolTipText("");
        jScrollPane1.setViewportView(txtConsole);

        cbCommand.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Waiting", "Terminate", "Destroy" }));
        cbCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbCommandActionPerformed(evt);
            }
        });

        btnCommand.setText("//");
        btnCommand.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                btnCommandMouseReleased(evt);
            }
        });
        btnCommand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCommandActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbCommand, 0, 423, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 298, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCommand, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(cbCommand))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbCommandActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cbCommandActionPerformed

    private void btnCommandActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCommandActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnCommandActionPerformed

    private void btnCommandMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_btnCommandMouseReleased
        String command = (String) cbCommand.getSelectedItem();
        Gemini.setClipboardText("//" + command);
        addConsoleText("Command \"" + command + "\" sent");
    }//GEN-LAST:event_btnCommandMouseReleased

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnCommand;
    private javax.swing.JComboBox cbCommand;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane txtConsole;
    // End of variables declaration//GEN-END:variables
}
