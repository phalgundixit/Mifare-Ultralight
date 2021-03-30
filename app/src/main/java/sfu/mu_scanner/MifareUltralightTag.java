package sfu.mu_scanner;

import java.util.Arrays;


public class MifareUltralightTag {

    private static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final int MU_BYTES_PER_PAGE = 4;

    private byte[] data;
    private String uid;

    public MifareUltralightTag(byte[] data) {
        this.data = data;
        this.uid = fetchUID();
    }

    public byte[] getPage(int pageIndex) {
        byte[] page = new byte[MU_BYTES_PER_PAGE];
        System.arraycopy(data, (pageIndex * MU_BYTES_PER_PAGE), page, 0, MU_BYTES_PER_PAGE);

        return page;
    }

    public String getPageHexValue(int pageIndex, boolean format) {
        int startIndex = pageIndex * MU_BYTES_PER_PAGE;
        int endIndex = startIndex + MU_BYTES_PER_PAGE;

        byte[] pageData = Arrays.copyOfRange(data, startIndex, endIndex);
        String hexValue = byteArrayToHexString(pageData);

        if (format) {
            hexValue = formatHexString(hexValue);
        }

        return hexValue;
    }

    public String getTagHexValue(boolean format) {
        String hexValue = byteArrayToHexString(data);

        if (format) {
            hexValue = formatHexString(hexValue);
        }

        return hexValue;
    }

    public String getUID() {
        return uid;
    }

    private String fetchUID() {
        byte[] uidData = Arrays.copyOfRange(data, 0, 9);
        return byteArrayToHexString(uidData);
    }

    private static String byteArrayToHexString(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i*2] = hexArray[v >>> 4];
            hexChars[i*2 + 1] = hexArray[v & 0x0F];
        }

        return new String(hexChars);
    }

    private static String formatHexString(String rawHex) {
        StringBuilder sb = new StringBuilder();
        char[] rawChar = rawHex.toCharArray();

        for (int i = 0; i < rawHex.length(); i++) {
            sb.append(rawChar[i]);

            if (i%2 == 1) {
                sb.append("-");
            }
        }

        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
