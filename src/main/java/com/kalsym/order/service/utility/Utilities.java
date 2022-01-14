package com.kalsym.order.service.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/**
 *
 * @author FaisalHayatJadoon
 */
public class Utilities {

    public static String[] convertArrayListToStringArray(ArrayList<String> arrayList) {
        String[] stringArray = new String[arrayList.size()];
        for (int i = 0; i < arrayList.size(); i++) {
            stringArray[i] = arrayList.get(i);
        }
        
        return stringArray;
    }
    
    public static BigDecimal roundDouble(Double value, int places) {
        if (value==null) return null;
        
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd;
    }
}
