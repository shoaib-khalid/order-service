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
import com.kalsym.order.service.OrderServiceApplication;
import com.kalsym.order.service.enums.StoreAssetType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import com.kalsym.order.service.model.Order;
import com.kalsym.order.service.model.OrderItem;
import com.kalsym.order.service.model.OrderSubItem;
import com.kalsym.order.service.model.Store;
import com.kalsym.order.service.model.StoreAssets;
import com.kalsym.order.service.model.Customer;
import com.kalsym.order.service.model.OrderPaymentDetail;
import com.kalsym.order.service.model.OrderShipmentDetail;
import com.kalsym.order.service.model.StoreWithDetails;
import com.kalsym.order.service.model.RegionCountry;
import com.kalsym.order.service.model.repository.OrderPaymentDetailRepository;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class GeneratePdfReport {

    
    public static ByteArrayInputStream orderInvoice(Order order, 
            List<OrderItem> orderItemList, 
            StoreWithDetails storeWithDetails, 
            OrderShipmentDetail deliveryDetails,
            RegionCountry regionCountry,
            String assetServiceBaseUrl,
            OrderPaymentDetailRepository orderPaymentDetailRepository) {

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String logprefix="GeneratePdfReport()";
        
        try {
            
            
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Generating pdf for orderId:"+order.getId());          
            
            //get store logo
            String storeLogo=null;             
            if ( storeWithDetails.getStoreLogoUrl()!=null) {
                storeLogo = storeWithDetails.getStoreLogoUrl();                            
            } else {
                storeLogo = storeWithDetails.getRegionVertical().getDefaultLogoUrl();                            
            }
            Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "storeLogo url:"+storeLogo);          
            
            //seller details
            Store store = order.getStore();
            String storeName = store.getName();
            String storeAddress = store.getAddress();
            String storeCity = store.getCity();
            String storePostcode = store.getPostcode();
            String storeState = store.getRegionCountryStateId();
            
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
            String deliveryChargeRemarks="";            
            if (order.getOrderPaymentDetail().getIsCombinedDelivery()) {
                List<OrderPaymentDetail> orderPaymentDetailList = orderPaymentDetailRepository.findByDeliveryQuotationReferenceId(order.getOrderPaymentDetail().getDeliveryQuotationReferenceId());
                deliveryChargeRemarks = " (combined x"+orderPaymentDetailList.size()+" shops)";
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
            
            Font fontNormalBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
            fontNormalBold.setSize(10);
            
            Font fontBig = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
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
                float preferredImageHeight = 80;
                float widthScale = image.getScaledHeight() / preferredImageHeight;
                image.scaleAbsolute(image.getScaledWidth() / widthScale, preferredImageHeight);                
                sellerImgCol.addElement(image);
            } catch (Exception ex) {
                Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception load store logo:"+ex.getMessage(), ex);          
            }
            sellerImgCol.setPaddingBottom(20);
            sellerImgCol.setBorder(0);
            sellerTable.addCell(sellerImgCol);
            PdfPCell sellerAdressCell = new PdfPCell();
            sellerAdressCell.addElement(new Paragraph(storeName+"\n"+storeAddress+"\n"+storePostcode+" "+storeCity+"\n"+storeState, fontNormal));             
            sellerAdressCell.setBorder(0);
            sellerTable.addCell(sellerAdressCell);
            col1.addElement(sellerTable);
            
            PdfPTable spacerTable = new PdfPTable(1);
            PdfPCell spacerCell = new PdfPCell();
            spacerCell.setPaddingBottom(10);
            spacerCell.setBorder(0);
            spacerCell.addElement(new Paragraph(" "));
            spacerTable.addCell(spacerCell);
            col1.addElement(spacerTable);
            
            PdfPTable buyerTable = new PdfPTable(2);
            buyerTable.setWidths(new int[]{1,2});
            buyerTable.setWidthPercentage(100); 
            PdfPCell buyerImgCol = new PdfPCell();
            buyerImgCol.addElement(new Paragraph("Bill To"));
            buyerImgCol.setPaddingBottom(50);
            buyerImgCol.setBorder(0);
            buyerTable.addCell(buyerImgCol);
            PdfPCell buyerAdressCell = new PdfPCell();
            buyerAdressCell.addElement(new Paragraph(customerName+"\n"+deliveryAddress+"\n"+deliveryPostcode+"\n"+deliveryCity+"\n"+deliveryState, fontNormal));
            buyerAdressCell.setBorder(0);
            buyerTable.addCell(buyerAdressCell);
            col1.addElement(buyerTable);
            col1.setBorder(0);
            col1.setPaddingBottom(100);
            col1.setPaddingTop(20);                        
            table.addCell(col1);
            
            PdfPCell col2;
            col2 = new PdfPCell();
            col2.setHorizontalAlignment(Element.ALIGN_CENTER);
            
            PdfPTable invInfoTable = new PdfPTable(1);
            invInfoTable.setWidthPercentage(100);
            invInfoTable.addCell(setCellNoBorder("INVOICE NO : "+ order.getInvoiceId(), fontBig, Element.ALIGN_LEFT));
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/YYYY HH:mm");
            //convert time to merchant timezone
            LocalDateTime startLocalTime = DateTimeUtil.convertToLocalDateTimeViaInstant(order.getCreated(), ZoneId.of(regionCountry.getTimezone()) );                
            DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("dd-MM-yyyy h:mm a");
            
            invInfoTable.addCell(setCellNoBorder("ORDER DATE : "+formatter1.format(startLocalTime), fontSmall, Element.ALIGN_LEFT));            
            col2.addElement(invInfoTable);
            col2.setBorder(0);
            col2.setPaddingTop(20);
            table.addCell(col2);
            
            //invoice item
            PdfPTable itemTable = new PdfPTable(1);
            itemTable.setWidthPercentage(100);
            PdfPCell itemCol = new PdfPCell();
            
            PdfPTable itemSubTable = new PdfPTable(4);
            itemSubTable.setWidthPercentage(100);
            itemSubTable.setWidths(new int[]{3, 1, 1, 1});
            itemSubTable.addCell(setCellNoBorder("ITEMS", fontNormalBold, Element.ALIGN_LEFT));
            itemSubTable.addCell(setCellNoBorder("PRICE", fontNormalBold, Element.ALIGN_RIGHT));           
            itemSubTable.addCell(setCellNoBorder("QUANTITY", fontNormalBold, Element.ALIGN_RIGHT));
            itemSubTable.addCell(setCellNoBorder("TOTAL", fontNormalBold, Element.ALIGN_RIGHT));
            itemCol.addElement(itemSubTable);
            
            PdfPTable itemSubTable2 = new PdfPTable(4);
            itemSubTable2.setWidthPercentage(100);
            itemSubTable2.setWidths(new int[]{3, 1, 1, 1});
            
            for (int x=0;x<orderItemList.size();x++) {
                OrderItem item = orderItemList.get(x);                
                
                if (item.getProductVariant()!=null && !"".equals(item.getProductVariant()) && !"null".equals(item.getProductVariant())) {
                    itemSubTable2.addCell(setStringCellValue(item.getProductName() + "|" + item.getProductVariant(), fontSmall, 1)); 
                } else if (item.getOrderSubItem()!=null) {
                    Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "", "Order subitem size:"+item.getOrderSubItem().size());
                    String subItemList = "";
                    for (OrderSubItem subItem : item.getOrderSubItem()) {
                        Logger.application.info(Logger.pattern, OrderServiceApplication.VERSION, "", "subitem product:"+subItem.getProductName());                
                        if (subItem.getProductName()!=null) {
                            if (subItemList.equals("")) {
                                subItemList = subItem.getProductName();
                            } else {
                                subItemList = subItemList +" | "+subItem.getProductName();
                            }
                        }
                    }
                    itemSubTable2.addCell(setStringCellValue(item.getProductName() + "|" + subItemList, fontSmall, 1)); 
                } else{
                    itemSubTable2.addCell(setStringCellValue(item.getProductName(), fontSmall, 1)); 
                }
                
                itemSubTable2.addCell(setNumberCellValue(String.format("%.2f",item.getProductPrice()),fontSmall, 1));                
                itemSubTable2.addCell(setIntegerCellValue(String.valueOf(item.getQuantity()), fontSmall, 1));
                itemSubTable2.addCell(setNumberCellValue(String.format("%.2f",item.getPrice()), fontSmall, 1));
            }
            
            itemCol.addElement(itemSubTable2);
            
            itemCol.setPaddingBottom(50);
            itemTable.addCell(itemCol);
            
            //customer notes            
            PdfPTable notesSubTable = new PdfPTable(1);
            notesSubTable.setWidthPercentage(100);
            PdfPCell notesCol = new PdfPCell();
            notesSubTable.addCell(setCellNoBorder("CUSTOMER NOTES : " +order.getCustomerNotes(), fontNormal, Element.ALIGN_LEFT));
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
            
            subtotalSubTable.addCell(setStringCellValue("SUBTOTAL", fontNormal, 0));
            subtotalSubTable.addCell(setNumberCellValue(String.format("%.2f",order.getSubTotal()), fontNormal, 0));
            
            subtotalSubTable.addCell(setStringCellValue("ORDER DISCOUNT", fontNormal, 0));
            subtotalSubTable.addCell(setNumberCellValue("-" +String.format("%.2f",convertNullToZero(order.getAppliedDiscount())), fontNormal, 0));
            
            subtotalSubTable.addCell(setStringCellValue("DELIVERY CHARGE "+deliveryChargeRemarks, fontNormal, 0));
            subtotalSubTable.addCell(setNumberCellValue(String.format("%.2f",convertNullToZero(order.getDeliveryCharges())), fontNormal, 0));
            
            subtotalSubTable.addCell(setStringCellValue("DELIVERY DISCOUNT", fontNormal, 0));
            subtotalSubTable.addCell(setNumberCellValue("-" +String.format("%.2f",convertNullToZero(order.getDeliveryDiscount())), fontNormal, 0));
                        
            subtotalSubTable.addCell(setStringCellValue("TOTAL", fontNormalBold, 0));            
            subtotalSubTable.addCell(setNumberCellValue(String.format("%.2f",order.getTotal()), fontNormalBold, 0));
            subTotalCol.addElement(subtotalSubTable);
            subTotalCol.setPaddingBottom(50);
            subtotalTable.addCell(subTotalCol);
             
            //footer
            PdfPTable footerTable = new PdfPTable(1);
            footerTable.setWidthPercentage(100);
            PdfPCell footerCol = new PdfPCell();
            PdfPTable footerSubTable = new PdfPTable(1);
            footerSubTable.setWidthPercentage(100);
            footerSubTable.addCell(setCellNoBorder("This invoice is computer generated. Thank you for your purchase.", fontXSmall, Element.ALIGN_LEFT));
            footerCol.addElement(footerSubTable);
            footerCol.setPaddingBottom(10);
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

            Logger.application.error(Logger.pattern, OrderServiceApplication.VERSION, logprefix, "Exception generating pdf:"+ex.getMessage(), ex);          
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
    
    private static double convertNullToZero(Double dblVal) {
        if (dblVal==null) {
            return 0.00;
        } else {
            return dblVal;
        }
    }
    
    private static PdfPCell setNumberCellValue(String value, Font font, int border) {
       PdfPCell pcell = new PdfPCell();
       if (value!=null && !value.equals("nu") && !value.equals("null") ) {
           Paragraph p = new Paragraph(value, font);
           p.setAlignment(Element.ALIGN_RIGHT);
           pcell.addElement(p);
       } else {
           Paragraph p = new Paragraph("0.00", font);
           p.setAlignment(Element.ALIGN_RIGHT);
           pcell.addElement(p);           
       }
       pcell.setBorder(border);
       return pcell;
    }
    
    private static PdfPCell setStringCellValue(String value, Font font, int border) {
       PdfPCell pcell = new PdfPCell();
       if (value!=null && !value.equals("nu") && !value.equals("null")) {
           Paragraph p = new Paragraph(value, font);
           p.setAlignment(Element.ALIGN_LEFT);
           pcell.addElement(p);
       } else {
           Paragraph p = new Paragraph("", font);
           p.setAlignment(Element.ALIGN_LEFT);
           pcell.addElement(p);           
       }
       pcell.setBorder(border);
       return pcell;
    }
    
    private static PdfPCell setIntegerCellValue(String value, Font font, int border) {
       PdfPCell pcell = new PdfPCell();
       if (value!=null && !value.equals("nu") && !value.equals("null") ) {
           Paragraph p = new Paragraph(value, font);
           p.setAlignment(Element.ALIGN_RIGHT);
           pcell.addElement(p);
       } else {
           Paragraph p = new Paragraph("0", font);
           p.setAlignment(Element.ALIGN_RIGHT);
           pcell.addElement(p);           
       }
       pcell.setBorder(border);
       return pcell;
    }
    
    private static PdfPCell setCellNoBorder(String value, Font font, int alignment) {
        PdfPCell pCell = new PdfPCell();
        Paragraph p = new Paragraph(value, font);
        p.setAlignment(alignment);
        pCell.addElement(p);
        pCell.setBorder(0);
        return pCell;
    }
   
  
}
