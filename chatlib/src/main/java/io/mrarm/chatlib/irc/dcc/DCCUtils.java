package io.mrarm.chatlib.irc.dcc;

public class DCCUtils {

    public static String convertIPForCommand(String ip) {
        int idx = 0;
        long ret = 0;
        for (int i = 0; i < 4; i++) {
            int idx2 = ip.indexOf('.', idx);
            if (i == 3 ? (idx2 != -1) : (idx2 == -1))
                throw new IllegalArgumentException("Invalid IPv4 address");
            ret = ret * 256 + Integer.parseInt(i == 3 ? ip.substring(idx) : ip.substring(idx, idx2));
            idx = idx2 + 1;
        }
        return Long.toString(ret);
    }

    public static String convertIPFromCommand(String data) {
        long l = Long.parseLong(data);
        return ((l >> 24) & 0xff) + "." + ((l >> 16) & 0xff) + "." + ((l >> 8) & 0xff) + "." + (l & 0xff);
    }



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



    public static String buildSendMessage(String myIp, String name, int port, long size, int reverseId) {
        StringBuilder sendCmd = new StringBuilder();
        sendCmd.append('\001');
        sendCmd.append("DCC SEND ");
        sendCmd.append(name);
        sendCmd.append(' ');
        sendCmd.append(convertIPForCommand(myIp));
        sendCmd.append(' ');
        sendCmd.append(port);
        sendCmd.append(' ');
        sendCmd.append(size);
        if (reverseId != -1) {
            sendCmd.append(' ');
            sendCmd.append(reverseId);
        }
        sendCmd.append('\001');
        return sendCmd.toString();
    }

    public static String buildSendMessage(String myIp, String name, int port, long size) {
        return buildSendMessage(myIp, name, port, size, -1);
    }

}
