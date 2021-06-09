package com.kalsym.order.service.utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 *
 * @author saros
 */
public class TxIdUtil {

    public static String generateReferenceId(String prefix) {
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
