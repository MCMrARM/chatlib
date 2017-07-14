package io.mrarm.chatlib.util;

import java.util.Base64;

public class Base64Util {

    public static Base64Util sImplementation;

    public static String encode(byte[] data) {
        return sImplementation.encodeData(data);
    }

    public String encodeData(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    static {
        try {
            Class c = Class.forName("io.mrarm.chatlib.util.Base64UtilImpl");
            sImplementation = (Base64Util) c.newInstance();
        } catch (Throwable e) {
            sImplementation = new Base64Util();
        }
    }

}
