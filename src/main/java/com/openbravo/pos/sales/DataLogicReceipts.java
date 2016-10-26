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

package com.openbravo.pos.sales;

import com.openbravo.basic.BasicException;
import com.openbravo.data.loader.Datas;
import com.openbravo.data.loader.PreparedSentence;
import com.openbravo.data.loader.SerializerReadBasic;
import com.openbravo.data.loader.SerializerReadClass;
import com.openbravo.data.loader.SerializerWriteBasicExt;
import com.openbravo.data.loader.SerializerWriteString;
import com.openbravo.data.loader.Session;
import com.openbravo.data.loader.StaticSentence;
import com.openbravo.pos.forms.BeanFactoryDataSingle;
import com.openbravo.pos.ticket.TicketInfo;
import java.util.List;

/**
 *
 * @author adrianromero
 */
public class DataLogicReceipts extends BeanFactoryDataSingle {
    
    private Session s;
    
    /** Creates a new instance of DataLogicReceipts */
    public DataLogicReceipts() {
    }
    
    /**
     *
     * @param s
     */
    @Override
    public void init(Session s){
        this.s = s;
    }
     
    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final TicketInfo getSharedTicket(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT CONTENT FROM SHAREDTICKETS WHERE ID = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {Datas.SERIALIZABLE})).find(Id);
            return record == null ? null : (TicketInfo) record[0];
        }
    }

    /**
     *
     * @param tabsearch
     * @return
     * @throws BasicException
     */
    public final List<SharedTicketInfo> getSharedTicketList(String tabsearch) throws BasicException {
        
        return (List<SharedTicketInfo>) new StaticSentence(s
// JG 20 Aug 13 Bug Fix: invalid SQL string
//                , "SELECT ID, NAME, CONTENT PICKUPID FROM SHAREDTICKETS ORDER BY ID"                
//                , "SELECT ID, NAME, CONTENT, TABNAME FROM SHAREDTICKETS " +
                , "SELECT ID, NAME, CONTENT, TABNAME, TABTOTAL FROM SHAREDTICKETS " +
                  "WHERE TABNAME LIKE '%" + tabsearch + "%' " +
                  "ORDER BY TABNAME"
                , null
                , new SerializerReadClass(SharedTicketInfo.class)).list();
    }
    
    /**
     *
     * @param id
     * @param ticket
     * @param tabname
     * @throws BasicException
     */
    public final void updateSharedTicket(final String id, final TicketInfo ticket, String tabname) throws BasicException {
         
        Object[] values = new Object[] {
            id, 
            ticket.getName(), 
            ticket, 
            tabname
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING, 
            Datas.SERIALIZABLE, 
            Datas.STRING
        };
        new PreparedSentence(s
                , "UPDATE SHAREDTICKETS SET "
                + "NAME = ?, "
                + "CONTENT = ?, "
                //+ "PICKUPID = ? "
                + "TABNAME = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, new int[] {1, 2, 3, 0})).exec(values);
    }
    
    /**
     *
     * @param id
     * @param ticket
     * @param tabname
     * @throws BasicException
     */
    public final void insertSharedTicket(final String id, final TicketInfo ticket, String tabname) throws BasicException {
        
        Object[] values = new Object[] {
            id, 
            ticket.getName(), 
            ticket,
            tabname,
            ticket.getUser()
        };
        Datas[] datas;
        datas = new Datas[] {
            Datas.STRING, 
            Datas.STRING, 
            Datas.SERIALIZABLE, 
            Datas.STRING
        };
        new PreparedSentence(s
            , "INSERT INTO SHAREDTICKETS ("
                + "ID, "
                + "NAME, "
                + "CONTENT, "
                //+ "PICKUPID) "
                + "TABNAME) "
                + "VALUES (?, ?, ?, ?)"
            , new SerializerWriteBasicExt(datas, new int[] {0, 1, 2, 3})).exec(values);
    }
    
    /**
     *
     * @param id
     * @throws BasicException
     */
    public final void deleteSharedTicket(final String id) throws BasicException {

        new StaticSentence(s
            , "DELETE FROM SHAREDTICKETS WHERE ID = ?"
            , SerializerWriteString.INSTANCE).exec(id);      
    }

    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public final Integer getPickupId(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT PICKUPID FROM SHAREDTICKETS WHERE ID = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {Datas.INT})).find(Id);
            return record == null ? 0 : (Integer)record[0];
        }
    } 


    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public String getTabName(String Id) throws BasicException {
        
        if (Id == null) {
            return null; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT TABNAME FROM SHAREDTICKETS WHERE ID = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {Datas.STRING})).find(Id);
            return (String) (record == null ? 0 : (String)record[0]);
        }
    } 
    
    
    /**
     *
     * @param Id
     * @return
     * @throws BasicException
     */
    public double getTabSubtotal(String Id) throws BasicException {
        
        if (Id == null) {
            return 0; 
        } else {
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT TABTOTAL FROM SHAREDTICKETS WHERE ID = ?"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {Datas.STRING})).find(Id);
            return (double) (record == null ? 0 : (double)record[0]);
        }
    } 
    

    public final void updateTabSubtotal(final String id, final double total) throws BasicException {
         
        Object[] values = new Object[] {
            id, 
            total
        };
        Datas[] datas = new Datas[] {
            Datas.STRING, 
            Datas.DOUBLE
        };
        new PreparedSentence(s
                , "UPDATE SHAREDTICKETS SET "
                + "TABTOTAL = ? "
                + "WHERE ID = ?"
                , new SerializerWriteBasicExt(datas, new int[] {1, 0})).exec(values);
    }
    

    /**
     *
     * @return
     * @throws BasicException
     */
    public final Double getTabsTotal() throws BasicException {
        
            Object[]record = (Object[]) new StaticSentence(s
                    , "SELECT SUM(TABTOTAL) FROM SHAREDTICKETS"
                    , SerializerWriteString.INSTANCE
                    , new SerializerReadBasic(new Datas[] {Datas.DOUBLE})).find();
            return record == null ? 0 : (Double)record[0];
        
    } 


    
}
