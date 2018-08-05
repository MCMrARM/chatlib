package io.mrarm.chatlib.irc;

import java.util.ArrayList;
import java.util.List;

public class MessageSplitHelper {

    public static final int MAX_SPACE_LOOKUP_N = 10;

    private static class FormattingState {
        int fg = 99, bg = 99;
        boolean bold, italic, underline;

        public String toColorCodes() {
            if (fg == 99 && bg == 99 && !bold && !italic && !underline)
                return null;

            StringBuilder ret = new StringBuilder(10);
            if (fg != 99 || bg != 99) {
                ret.append((char) 0x03);
                ret.append(fg);
                ret.append(',');
                ret.append(bg);
            }
            if (bold)
                ret.append((char) 0x02);
            if (italic)
                ret.append((char) 0x1D);
            if (underline)
                ret.append((char) 0x1F);
            return ret.toString();
        }
    }

    public static String[] split(ServerConnectionData conn, String channel, String message, boolean notice) {
        String userUser = conn.getUserUser();
        String userHost = conn.getUserHost();
        if (userUser == null || userHost == null)
            return new String[] { message };
        int maxLength = 512;
        maxLength -= 1 + conn.getUserNick().length() + 1 + userUser.length() + 1 + userHost.length() + 1; // ":n!u@h "
        maxLength -= (notice ? 7 : 8); // "NOTICE "/"PRIVMSG "
        maxLength -= channel.length() + 2; // "chan :"
        if (message.length() < maxLength)
            return new String[] { message }; // no splitting needed

        String messagePrefix = null;
        String messageSuffix = null;

        boolean isMeMessage = message.startsWith("\01ACTION ") && message.endsWith("\01");
        if (isMeMessage) {
            messagePrefix = "\01ACTION ";
            messageSuffix = "\01";
            message = message.substring(messagePrefix.length(), message.length() - messageSuffix.length());
        }

        List<String> ret = new ArrayList<>();
        FormattingState formattingState = new FormattingState();
        for (int i = 0; i < message.length(); ) {
            String colorPrefix = formattingState.toColorCodes();

            int len = Math.min(maxLength - (messagePrefix != null ? messagePrefix.length() : 0)
                    - (messageSuffix != null ? messageSuffix.length() : 0)
                    - (colorPrefix != null ? colorPrefix.length() : 0), message.length() - i);
            if (i + len != message.length()) {
                int slen = Math.max(len - MAX_SPACE_LOOKUP_N, 0);
                for (int nlen = len; nlen >= slen; --nlen) {
                    if (message.charAt(nlen - 1) == ' ') {
                        len = nlen;
                        break;
                    }
                }
            }

            if (colorPrefix  != null)
                ret.add(colorPrefix + message.substring(i, i + len));
            else
                ret.add(message.substring(i, i + len));

            processColors(formattingState, message, i, i + len);
            i += len;
        }
        return ret.toArray(new String[ret.size()]);
    }

    private static void processColors(FormattingState f, String string, int start, int end) {
        for (int i = start; i < end; ) {
            switch (string.charAt(i)) {
                case 0x02: { // bold
                    i++;
                    f.bold = !f.bold;
                    break;
                }
                case 0x1D: { // italic
                    i++;
                    f.italic = !f.italic;
                    break;
                }
                case 0x1F: { // underline
                    i++;
                    f.underline = !f.underline;
                    break;
                }
                case 0x0F: { // reset
                    i++;
                    f.fg = f.bg = 99;
                    f.bold = f.italic = f.underline = false;
                    break;
                }
                case 0x03: { // color
                    f.fg = -1;
                    i++;
                    for (int j = 0; j < 2 && i < string.length(); i++, j++) {
                        if (string.charAt(i) < '0' || string.charAt(i) > '9')
                            break;
                        f.fg = Math.max(f.fg, 0) * 10 + string.charAt(i) - '0';
                    }
                    if (f.fg == -1) {
                        f.fg = f.bg = 99;
                        continue;
                    }

                    if (string.charAt(i) != ',')
                        break;
                    i++;
                    f.bg = 0;
                    for (int j = 0; j < 2 && i < string.length(); i++, j++) {
                        if (string.charAt(i) < '0' || string.charAt(i) > '9')
                            break;
                        f.bg = f.bg * 10 + string.charAt(i) - '0';
                    }
                    break;
                }
                case 0x16: { // swap bg and fg
                    i++;
                    int tmp = f.fg;
                    f.fg = f.bg;
                    f.bg = tmp;
                    break;
                }
                default: {
                    i++;
                }
            }
        }
    }

}
