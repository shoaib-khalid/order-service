/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.kalsym.order.service.utility;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import com.kalsym.order.service.enums.StoreAssetType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.Store;
import com.kalsym.order.service.model.StoreAssets;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.StoreWithDetails;

public class GeneratePdfReport {

    private static final Logger logger = LoggerFactory.getLogger(GeneratePdfReport.class);

    public static ByteArrayInputStream orderInvoice(Order order, 
            List<OrderItem> orderItemList, 
            StoreWithDetails storeWithDetails, 
            OrderShipmentDetail deliveryDetails) {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            
            //get store logo
            String storeLogo=null;             
            if ( storeWithDetails.getStoreLogoUrl()!=null) {
                storeLogo = storeWithDetails.getStoreLogoUrl();                            
            } else {
                storeLogo = storeWithDetails.getRegionVertical().getDefaultLogoUrl();                            
            }
            //seller details
            Store store = order.getStore();
            String storeName = store.getName();
            String storeAddress = store.getAddress();
            String storeCity = store.getCity();
            String storePostcode = store.getPostcode();
            String storeState = store.getState();
            
            //buyer details
            Customer customer = order.getCustomer();
            String customerName = customer.getName();
            String deliveryAddress="";
            String deliveryPostcode="";
            String deliveryCity="";
            String deliveryState="";
            if (deliveryDetails!=null) {
                deliveryAddress = deliveryDetails.getAddress();
                deliveryPostcode = deliveryDetails.getZipcode();
                deliveryCity = deliveryDetails.getCity();
                deliveryState = deliveryDetails.getState();
            }
            
            //invoice header            
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{2, 1});
            
            Font fontXSmall = FontFactory.getFont(FontFactory.HELVETICA);
            fontXSmall.setSize(8);
            
            Font fontSmall = FontFactory.getFont(FontFactory.HELVETICA);
            fontSmall.setSize(9);
            
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA);
            fontNormal.setSize(10);            
            
            Font fontBig = FontFactory.getFont(FontFactory.HELVETICA);
            fontBig.setSize(12);            
            
            PdfPCell col1;
            col1 = new PdfPCell();
            col1.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            PdfPTable sellerTable = new PdfPTable(2);
            sellerTable.setWidths(new int[]{1,2});
            sellerTable.setWidthPercentage(100);
            PdfPCell sellerImgCol = new PdfPCell();
            try {
                Image image = Image.getInstance(storeLogo);
                sellerImgCol.addElement(image);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            sellerImgCol.setPaddingBottom(50);
            sellerTable.addCell(sellerImgCol);
            sellerTable.addCell(new Paragraph(storeName+"\n"+storeAddress+"\n"+storePostcode+" "+storeCity+"\n"+storeState, fontNormal)); 
            col1.addElement(sellerTable);
            
            PdfPTable buyerTable = new PdfPTable(2);
            buyerTable.setWidths(new int[]{1,2});
            buyerTable.setWidthPercentage(100); 
            PdfPCell buyerImgCol = new PdfPCell();
            buyerImgCol.addElement(new Paragraph("Bill To"));
            buyerImgCol.setPaddingBottom(50);
            buyerTable.addCell(buyerImgCol);
            buyerTable.addCell(new Paragraph(customerName+"\n"+deliveryAddress+"\n"+deliveryPostcode+"\n"+deliveryCity+"\n"+deliveryState, fontNormal));
            col1.addElement(buyerTable);
            col1.setBorder(1);
            col1.setPaddingBottom(100);
            col1.setPaddingTop(50);                        
            table.addCell(col1);
            
            PdfPCell col2;
            col2 = new PdfPCell();
            col2.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            PdfPTable invInfoTable = new PdfPTable(1);
            invInfoTable.setWidthPercentage(100);
            invInfoTable.addCell(new Paragraph("INVOICE "+ order.getInvoiceId(), fontBig));
            invInfoTable.addCell(new Paragraph("CREATED DATE "+order.getCreated(), fontSmall));
            invInfoTable.addCell(new Paragraph("UPDATED DATE "+order.getUpdated(), fontSmall));
            col2.addElement(invInfoTable);
            col2.setBorder(1);
            col2.setPaddingTop(50);
            table.addCell(col2);
            
            //invoice item
            PdfPTable itemTable = new PdfPTable(1);
            itemTable.setWidthPercentage(100);
            PdfPCell itemCol = new PdfPCell();
            
            PdfPTable itemSubTable = new PdfPTable(5);
            itemSubTable.setWidthPercentage(100);
            itemSubTable.setWidths(new int[]{3, 1, 1, 1, 1});
            itemSubTable.addCell(new Paragraph("ITEMS", fontNormal));
            itemSubTable.addCell(new Paragraph("PRICE", fontNormal));
            itemSubTable.addCell(new Paragraph("ORIGINAL QUANTITY", fontNormal));
            itemSubTable.addCell(new Paragraph("QUANTITY", fontNormal));
            itemSubTable.addCell(new Paragraph("TOTAL", fontNormal));
            itemCol.addElement(itemSubTable);
            
            PdfPTable itemSubTable2 = new PdfPTable(5);
            itemSubTable2.setWidthPercentage(100);
            itemSubTable2.setWidths(new int[]{3, 1, 1, 1, 1});
            
            for (int x=0;x<orderItemList.size();x++) {
                OrderItem item = orderItemList.get(x);
                itemSubTable2.addCell(new Paragraph(item.getProductName(), fontNormal));
                itemSubTable2.addCell(new Paragraph(String.valueOf(item.getProductPrice()), fontNormal));
                itemSubTable2.addCell(new Paragraph(String.valueOf(item.getOriginalQuantity()), fontNormal));
                itemSubTable2.addCell(new Paragraph(String.valueOf(item.getQuantity()), fontNormal));
                itemSubTable2.addCell(new Paragraph(String.valueOf(item.getPrice()), fontNormal));
            }
            
            itemCol.addElement(itemSubTable2);
            
            itemCol.setPaddingBottom(50);
            itemTable.addCell(itemCol);
            
            //customer notes            
            PdfPTable notesSubTable = new PdfPTable(1);
            notesSubTable.setWidthPercentage(100);
            PdfPCell notesCol = new PdfPCell();
            notesSubTable.addCell(new Paragraph("CUSTOMER NOTES : " +order.getCustomerNotes(), fontNormal));
            notesCol.addElement(notesSubTable);
            notesCol.setPaddingBottom(50);
            PdfPTable notesTable = new PdfPTable(1);
            notesTable.setWidthPercentage(100);
            notesTable.addCell(notesCol);
            
            //subtotal
            PdfPTable subtotalTable = new PdfPTable(1);
            subtotalTable.setWidthPercentage(100);
            PdfPCell subTotalCol = new PdfPCell();
            PdfPTable subtotalSubTable = new PdfPTable(2);
            subtotalSubTable.setWidthPercentage(100);
            subtotalSubTable.setWidths(new int[]{5, 1});
            subtotalSubTable.addCell(new Paragraph("SUBTOTAL", fontNormal));
            subtotalSubTable.addCell(new Paragraph(String.valueOf(order.getSubTotal()), fontNormal));
            subtotalSubTable.addCell(new Paragraph("ORDER DISCOUNT", fontNormal));
            subtotalSubTable.addCell(new Paragraph(String.valueOf(order.getAppliedDiscount()), fontNormal));
            subtotalSubTable.addCell(new Paragraph("DELIVERY CHARGE", fontNormal));
            subtotalSubTable.addCell(new Paragraph(String.valueOf(order.getDeliveryCharges()), fontNormal));
            subtotalSubTable.addCell(new Paragraph("DELIVERY DISCOUNT", fontNormal));
            subtotalSubTable.addCell(new Paragraph(String.valueOf(order.getDeliveryDiscount()), fontNormal));
            subtotalSubTable.addCell(new Paragraph("PLATFORM VOUCHER", fontNormal));
            subtotalSubTable.addCell(new Paragraph(String.valueOf(order.getVoucherDiscount()), fontNormal));
            subtotalSubTable.addCell(new Paragraph("TOTAL", fontNormal));
            subtotalSubTable.addCell(new Paragraph(String.valueOf(order.getTotal()), fontNormal));
            subTotalCol.addElement(subtotalSubTable);
            subTotalCol.setPaddingBottom(50);
            subtotalTable.addCell(subTotalCol);
             
            //footer
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);
            PdfPCell footerCol = new PdfPCell();
            PdfPTable footerSubTable = new PdfPTable(1);
            footerSubTable.setWidthPercentage(100);
            footerSubTable.addCell(new Paragraph("This invoice is computer generated. Thank you for your business.", fontXSmall));
            footerCol.addElement(footerSubTable);
            footerCol.setPaddingBottom(100);
            footerTable.addCell(footerCol);
            
            PdfWriter.getInstance(document, out);
            document.open();
            document.add(table);
            document.add(itemTable);
            document.add(notesTable);
            document.add(subtotalTable);
            document.add(footerTable);
            
            document.close();

        } catch (DocumentException ex) {

            logger.error("Error occurred: {0}", ex);
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}
