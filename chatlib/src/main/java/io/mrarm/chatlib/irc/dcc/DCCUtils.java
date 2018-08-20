package io.mrarm.chatlib.irc.dcc;

public class DCCUtils {

    public static String escapeFilename(String filename) {
        if (filename.contains(" "))
            return "\"" + filename.replace('"', '\'') + "\"";
        if (filename.startsWith("\""))
            return filename.replace('"', '\'');
        return filename;
    }

    public static int getFilenameLength(String val) {
        if (val.startsWith("\"")) {
            int iof = val.indexOf("\" ", 1) + 1;
            if (iof == -1 + 1)
                iof = val.indexOf(" ", 1);
            return iof == -1 ? val.length() : iof;
        } else {
            int iof = val.indexOf(" ");
            return iof == -1 ? val.length() : iof;
        }
    }

    public static String unescapeFilename(String val) {
        if (val.startsWith("\"") && val.endsWith("\""))
            return val.substring(1, val.length() - 1);
        else
            return val;
    }

}
