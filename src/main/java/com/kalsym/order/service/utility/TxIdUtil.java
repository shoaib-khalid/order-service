package com.kalsym.order.service.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import com.kalsym.order.service.model.repository.StoreRepository;

/**
 *
 * @author saros
 */
public class TxIdUtil {

    public static String generateReferenceIdOld(String prefix) {
        String referenceId = prefix;
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmss");
        String datetime = ft.format(dNow);

        Random rnd = new Random();
        int n = 100 + rnd.nextInt(900);

        referenceId = referenceId + datetime + n;

        return referenceId;
    }
    
    public static String generateInvoiceId(String storeId, String prefix, StoreRepository storeRepository) {
        
        //get running number for current store
        int orderNo = storeRepository.getInvoiceSeqNo(storeId);
        if (orderNo>=99999) { storeRepository.ResetInvoiceSeqNo(storeId); }
        String referenceId = prefix + String.format("%05d", orderNo);
        return referenceId;
    }
    
    public static String generateQrcodeToken(String prefix) {
        String referenceId = prefix;
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyMMddhhmmss");
        String datetime = ft.format(dNow);

        Random rnd = new Random();
        int n = 100 + rnd.nextInt(900);

        referenceId = referenceId + datetime + n;

        return referenceId;
    }
}
