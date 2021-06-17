package com.kalsym.order.service.utility;

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
}
