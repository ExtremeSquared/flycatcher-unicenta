//    uniCenta oPOS  - Touch Friendly Point Of Sale
//    Copyright (c) 2009-2015 uniCenta & previous Openbravo POS works
//    http://www.unicenta.com
//
//    This file is part of uniCenta oPOS
//
//    uniCenta oPOS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//   uniCenta oPOS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with uniCenta oPOS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.sales.shared;

import com.openbravo.basic.BasicException;
import com.openbravo.data.gui.MessageInf;
import com.openbravo.pos.forms.AppLocal;
import com.openbravo.pos.forms.AppView;
import com.openbravo.pos.forms.JRootApp;
import com.openbravo.pos.sales.DataLogicReceipts;
import com.openbravo.pos.sales.JTicketsBag;
import com.openbravo.pos.sales.SharedTicketInfo;
import com.openbravo.pos.sales.TicketsEditor;
import com.openbravo.pos.ticket.TicketInfo;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.lang.Runtime;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author JG uniCenta
 */
public class JTicketsBagShared extends JTicketsBag {
    
    private String m_sCurrentTicket = null;
    private DataLogicReceipts dlReceipts = null;
    
    
    /** Creates new form JTicketsBagShared
     * @param app
     * @param panelticket */
    public JTicketsBagShared(AppView app, TicketsEditor panelticket) {
        
        super(app, panelticket);
        
        dlReceipts = (DataLogicReceipts) app.getBean("com.openbravo.pos.sales.DataLogicReceipts");
        
        initComponents();
        
    }
    
    /**
     *
     */
    @Override
    public void activate() {
        
        m_sCurrentTicket = null;
        selectValidTicket();     
        
        m_jDelTicket.setEnabled(m_App.getAppUserView().getUser().hasPermission("com.openbravo.pos.sales.JPanelTicketEdits"));
       
    }
    
    /**
     *
     * @return
     */
    @Override
    public boolean deactivate() {
        
        saveCurrentTicket();
        
        m_sCurrentTicket = null;
        m_panelticket.setActiveTicket(null, null);       
        
        return true;
    }
        
    /**
     *
     */
    @Override
    public void deleteTicket() {
        m_sCurrentTicket = null;
        selectValidTicket();

    }
    
    /**
     *
     * @return
     */
    @Override
    protected JComponent getBagComponent() {
        return this;
    }
    
    /**
     *
     * @return
     */
    @Override
    protected JComponent getNullComponent() {
        return new JPanel();
    }
   
    private void saveCurrentTicket() {
        String magtext;
        CharSequence magtextcs;
        int nstart, nend;
        magtext = m_jTabName.getText();
        magtext = magtext.replaceAll("[0-9]","x");
        nstart = magtext.indexOf("^", 18);
        nend = magtext.indexOf("^", nstart+1);
        if (nstart > -1 && nstart != nend) {
            magtextcs = magtext.subSequence(nstart+1, nend);
        } else {
            magtextcs = magtext;
        }
        

        if (m_sCurrentTicket != null) {
            try {
                dlReceipts.insertSharedTicket(m_sCurrentTicket, m_panelticket.getActiveTicket(), magtextcs.toString().toUpperCase().trim());
                m_jTabName.setText("");
                //m_jListTickets.setText("*");
                TicketInfo l = dlReceipts.getSharedTicket(m_sCurrentTicket);
                
                //Set Tab Total in SHAREDTICKETS
                dlReceipts.updateTabSubtotal(m_sCurrentTicket, l.getSubTotal());


                if(l.getLinesCount() == 0) {
//                      throw new BasicException(AppLocal.getIntString("message.nullticket"));
//                    }else{
                        dlReceipts.deleteSharedTicket(m_sCurrentTicket);
                    }             
            } catch (BasicException e) {
                new MessageInf(e).show(this);
            }  
        }    
    }
    
    private void setActiveTicket(String id) throws BasicException{
          

        TicketInfo ticket = dlReceipts.getSharedTicket(id);
        if (ticket == null)  {
            m_jListTickets.setText("");
            throw new BasicException(AppLocal.getIntString("message.noticket"));
        } else {
            dlReceipts.getPickupId(id);
            m_jTabName.setText(dlReceipts.getTabName(id)); //Try to pull tab name
            
            Integer pickUp = dlReceipts.getPickupId(id);
            dlReceipts.deleteSharedTicket(id);
            m_sCurrentTicket = id;
            m_panelticket.setActiveTicket(ticket, null);
            ticket.setPickupId(pickUp);     
            
        } 
        // END TRANSACTION                 
    }
    
    private void selectValidTicket() {
        
        newTicket();
        
        try {
            List<SharedTicketInfo> l = dlReceipts.getSharedTicketList("");
            if (l.isEmpty()) {
                m_jListTickets.setText("");                
                 newTicket();
            } else {
// JG Deliberate doClick for testing/reuse
                //m_jListTickets.doClick(); 
            }
        } catch (BasicException e) {
            new MessageInf(e).show(this);
            newTicket();
        }    
    }    
    
    private void newTicket() {      
        
        saveCurrentTicket();

        TicketInfo ticket = new TicketInfo();    
        m_sCurrentTicket = UUID.randomUUID().toString();
        m_panelticket.setActiveTicket(ticket, null);
        m_jTabName.setText("");
        m_jTabSearch.setText("Search");
        hideOSK();

 
    }
    
    private void hideOSK() {
        Process oskhideprocess;
        try {
            oskhideprocess = Runtime.getRuntime().exec("florence hide",null);
            //oskprocess = Runtime.getRuntime().exec("bash /home/finn/oskbd.sh",null);
            //m_jTabName.requestFocusInWindow(); 
        } catch (IOException ex) {
            Logger.getLogger(JTicketsBagShared.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    

    private void showOSK() {
        Process oskshowprocess;
        try {
            //oskshowprocess = Runtime.getRuntime().exec("beep",null);
            //oskshowprocess = Runtime.getRuntime().exec("bash ~/kbdshow.sh",null);
            oskshowprocess = Runtime.getRuntime().exec("florence show",null);
            //m_jTabName.requestFocusInWindow(); 
        } catch (IOException ex) {
            Logger.getLogger(JTicketsBagShared.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        m_jHold = new javax.swing.JButton();
        m_jDelTicket = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        m_jNewTicket = new javax.swing.JButton();
        m_jListTickets = new javax.swing.JButton();
        m_jTabName = new javax.swing.JTextField();
        m_jTabSearch = new javax.swing.JTextField();

        setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        setLayout(new java.awt.BorderLayout());

        m_jHold.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        m_jHold.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/logout.png"))); // NOI18N
        m_jHold.setToolTipText("Quick LogOff");
        m_jHold.setFocusPainted(false);
        m_jHold.setFocusable(false);
        m_jHold.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jHold.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jHold.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jHold.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jHold.setRequestFocusEnabled(false);
        m_jHold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jHoldActionPerformed(evt);
            }
        });
        jPanel1.add(m_jHold);

        m_jDelTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_delete.png"))); // NOI18N
        m_jDelTicket.setToolTipText("Cancel Sale");
        m_jDelTicket.setFocusPainted(false);
        m_jDelTicket.setFocusable(false);
        m_jDelTicket.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jDelTicket.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jDelTicket.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jDelTicket.setPreferredSize(new java.awt.Dimension(40, 40));
        m_jDelTicket.setRequestFocusEnabled(false);
        m_jDelTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jDelTicketActionPerformed(evt);
            }
        });
        jPanel1.add(m_jDelTicket);

        jSeparator1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jSeparator1.setPreferredSize(new java.awt.Dimension(10, 40));
        jSeparator1.setRequestFocusEnabled(false);
        jPanel1.add(jSeparator1);

        m_jNewTicket.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/sale_new.png"))); // NOI18N
        m_jNewTicket.setToolTipText("New Sale");
        m_jNewTicket.setFocusPainted(false);
        m_jNewTicket.setFocusable(false);
        m_jNewTicket.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jNewTicket.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jNewTicket.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jNewTicket.setPreferredSize(new java.awt.Dimension(65, 40));
        m_jNewTicket.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jNewTicketActionPerformed(evt);
            }
        });
        jPanel1.add(m_jNewTicket);

        m_jListTickets.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        m_jListTickets.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/openbravo/images/payments.png"))); // NOI18N
        m_jListTickets.setToolTipText("Layaways");
        m_jListTickets.setFocusPainted(false);
        m_jListTickets.setFocusable(false);
        m_jListTickets.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        m_jListTickets.setMargin(new java.awt.Insets(0, 4, 0, 4));
        m_jListTickets.setMaximumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setMinimumSize(new java.awt.Dimension(50, 40));
        m_jListTickets.setPreferredSize(new java.awt.Dimension(65, 40));
        m_jListTickets.setRequestFocusEnabled(false);
        m_jListTickets.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jListTicketsActionPerformed(evt);
            }
        });
        jPanel1.add(m_jListTickets);

        m_jTabName.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        m_jTabName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        m_jTabName.setAutoscrolls(false);
        m_jTabName.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        m_jTabName.setPreferredSize(new java.awt.Dimension(175, 40));
        m_jTabName.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                m_jTabNameFocusGained(evt);
            }
        });
        m_jTabName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jTabNameActionPerformed(evt);
            }
        });
        jPanel1.add(m_jTabName);

        m_jTabSearch.setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        m_jTabSearch.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        m_jTabSearch.setText("Search");
        m_jTabSearch.setToolTipText("");
        m_jTabSearch.setAutoscrolls(false);
        m_jTabSearch.setPreferredSize(new java.awt.Dimension(115, 40));
        m_jTabSearch.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                m_jTabSearchFocusGained(evt);
            }
        });
        m_jTabSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                m_jTabSearchActionPerformed(evt);
            }
        });
        jPanel1.add(m_jTabSearch);

        add(jPanel1, java.awt.BorderLayout.WEST);
    }// </editor-fold>//GEN-END:initComponents

    private void m_jListTicketsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jListTicketsActionPerformed

        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                try {
                    List<SharedTicketInfo> l = dlReceipts.getSharedTicketList("");
//                    String itemCount = Integer.toString(l.size());
//                    m_jListTickets.setText(itemCount);
//                    m_jListTickets.setIcon(null);
                    JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(JTicketsBagShared.this);
                    String id = listDialog.showTicketsList(l); 

                    if (id != null) {
                        saveCurrentTicket();
                        setActiveTicket(id); 
                    }
                } catch (BasicException e) {
                    new MessageInf(e).show(JTicketsBagShared.this);
                    newTicket();
                }                    
            }
        });
        
    }//GEN-LAST:event_m_jListTicketsActionPerformed

    private void m_jDelTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jDelTicketActionPerformed
        
        int res = JOptionPane.showConfirmDialog(this, AppLocal.getIntString("message.wannadelete"), AppLocal.getIntString("title.editor"), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (res == JOptionPane.YES_OPTION) {
            deleteTicket();

        }
        
    }//GEN-LAST:event_m_jDelTicketActionPerformed

    private void m_jNewTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jNewTicketActionPerformed

        newTicket();
        
    }//GEN-LAST:event_m_jNewTicketActionPerformed

    private void m_jHoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jHoldActionPerformed
        deactivate();
        ((JRootApp)m_App).closeAppView();
    }//GEN-LAST:event_m_jHoldActionPerformed

    private void m_jTabNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jTabNameActionPerformed

        String magtext;
        CharSequence magtextcs;
        int nstart, nend;
        magtext = m_jTabName.getText();
        magtext = magtext.replaceAll("[0-9]","x");
        nstart = magtext.indexOf("^", 18);
        nend = magtext.indexOf("^", nstart+1);
        if (nstart > -1 && nstart != nend) {
            magtextcs = magtext.subSequence(nstart+1, nend);
        } else {
            magtextcs = magtext;
        }
        m_jTabName.setText(magtextcs.toString().toUpperCase());
//        m_jNewTicket.requestFocusInWindow();

    }//GEN-LAST:event_m_jTabNameActionPerformed

    private void m_jTabNameFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jTabNameFocusGained
//        m_jTabName.setText("");
        showOSK();
    }//GEN-LAST:event_m_jTabNameFocusGained

    private void m_jTabSearchFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_m_jTabSearchFocusGained
        m_jTabSearch.setText("");
        showOSK();
    }//GEN-LAST:event_m_jTabSearchFocusGained

    private void m_jTabSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_m_jTabSearchActionPerformed
            hideOSK();
            m_jNewTicket.grabFocus();

            SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                try {
                    List<SharedTicketInfo> l = dlReceipts.getSharedTicketList(m_jTabSearch.getText());
//                    String itemCount = Integer.toString(l.size());
//                    m_jListTickets.setText(itemCount);
//                    m_jListTickets.setIcon(null);
                    JTicketsBagSharedList listDialog = JTicketsBagSharedList.newJDialog(JTicketsBagShared.this);
                    String id = listDialog.showTicketsList(l); 

                    if (id != null) {
                        saveCurrentTicket();

//                        m_jTabSearch.setText("");
//                        m_jNewTicket.requestFocusInWindow();

                        setActiveTicket(id);
//                      m_jTabSearch.requestFocusInWindow();
                        
                       
                        
                    }
                } catch (BasicException e) {
                    new MessageInf(e).show(JTicketsBagShared.this);
                    newTicket();
                }                    
            }
        });
        //m_jTabSearch.setText("");
        // TODO add your handling code here:
    }//GEN-LAST:event_m_jTabSearchActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton m_jDelTicket;
    private javax.swing.JButton m_jHold;
    private javax.swing.JButton m_jListTickets;
    private javax.swing.JButton m_jNewTicket;
    private javax.swing.JTextField m_jTabName;
    private javax.swing.JTextField m_jTabSearch;
    // End of variables declaration//GEN-END:variables
    
}
