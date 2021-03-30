package sfu.mu_scanner;

import android.content.Context;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class NFCTagManager {

    private static final int MU_BYTE_LIMIT = 64;
    private static final int MU_BYTES_PER_PAGE = 4;
    private static final int MU_BYTES_PER_READ = 16;
    private static final int MU_NUM_PAGES = 16;
    private static final int MU_PAGES_PER_READ = 4;
    private static final int MU_FIRST_WRITABLE_PAGE = 4;

    private List<MifareUltralightTag> tags;

    public NFCTagManager() {
        this.tags = new ArrayList<>();
    }

    public List<String> getTagHexValue(String tagUID, boolean format) {
        List<String> pages = new ArrayList<>(MU_NUM_PAGES);

        for (int i = 0; i < MU_NUM_PAGES; i++) {
            pages.add(getTagPageHexValue(tagUID, i, format));
        }

        return pages;
    }

    public String getTagPageHexValue(String tagUID, int pageIndex, boolean format) {
        List<MifareUltralightTag> match = tags.stream().filter(tag -> tag.getUID().equals(tagUID)).collect(Collectors.toList());
        return match.get(0).getPageHexValue(pageIndex, format);
    }

    public List<String> streamTagHexValue(Tag tag, boolean format) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        List<String> pages = new ArrayList<>(MU_NUM_PAGES);

        try {
            mifare.connect();
            byte[] data = new byte[MU_BYTE_LIMIT];

            // reading tag;
            for (int i = 0; i < MU_NUM_PAGES; i += MU_PAGES_PER_READ) {
                System.arraycopy(mifare.readPages(i), 0, data, (i * MU_BYTES_PER_PAGE), MU_BYTES_PER_READ);
            }

            MifareUltralightTag muTag = new MifareUltralightTag(data);

            for (int i = 0; i < MU_NUM_PAGES; i++) {
                pages.add(muTag.getPageHexValue(i, format));
            }
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return pages;
    }

    // returns tag UID;
    public String readTag(Tag tag, Context context) {
        MifareUltralight mifare = MifareUltralight.get(tag);

        try {
            mifare.connect();
            byte[] data = new byte[MU_BYTE_LIMIT];

            // reading tag;

           /* private static final int MU_BYTE_LIMIT = 64;
            private static final int MU_BYTES_PER_PAGE = 4;
            private static final int MU_BYTES_PER_READ = 16;
            private static final int MU_NUM_PAGES = 16;
            private static final int MU_PAGES_PER_READ = 4;
            private static final int MU_FIRST_WRITABLE_PAGE = 4;*/

            for (int i = 0; i < MU_NUM_PAGES; i += MU_PAGES_PER_READ) {
                System.arraycopy(mifare.readPages(i), 0, data, (i * MU_BYTES_PER_PAGE), MU_BYTES_PER_READ);
            }

            MifareUltralightTag muTag = new MifareUltralightTag(data);
            String muTagUID = muTag.getUID();

            if (isDuplicateTag(muTagUID)) {
                Toast.makeText(context, R.string.message_read_old_tag, Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(context, R.string.message_read_new_tag, Toast.LENGTH_LONG).show();
                tags.add(muTag);
            }

            return muTagUID;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }





    public byte[] NewreadMifareUltralight(Tag Tag) throws IOException {
        byte[] payload = new byte[MU_BYTE_LIMIT];
        MifareUltralight tag = MifareUltralight.get(Tag);
        try {


            tag.connect();

            for (int i = 4; i < 16; i++) {
                System.arraycopy(
                        tag.readPages(i),
                        0,
                        payload,
                        (i - 4) * 4,
                        4
                );
            }
        } finally {
            tag.close();
        }

        return payload;
    }

    public void DiffwriteOnMifareUltralightC( Tag tag,
                                               String pageData, int pageNumber) {
        MifareUltralight mifare = null;

        try {
            mifare = MifareUltralight.get(tag);
            mifare.connect();
            mifare.writePage(pageNumber, pageData.getBytes("US-ASCII"));

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                mifare.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }



    public void RemovePassword(Tag Tag) throws IOException {

        String Password = "11";
        MifareUltralight ultralight = MifareUltralight.get(Tag);
        try {


            ultralight.connect();

            Password = "1111";
            byte[] t = Password.getBytes(Charset.forName("US-ASCII"));

            byte[] presponse = ultralight.transceive(new byte[] {
                    (byte) 0x1B, // PWD_AUTH
                    t[0],t[1],t[2],t[3]
            });
            byte[] pack = null;
            if ((presponse != null) && (presponse.length >= 2)) {
                pack = Arrays.copyOf(presponse, 2);
                // TODO: verify PACK to confirm that tag is authentic (not really,
                // but that whole PWD_AUTH/PACK authentication mechanism was not
                // really meant to bring much security, I hope; same with the
                // NTAG signature btw.)
            }



            byte[] responseAuth0 = ultralight.transceive(new byte[] {
                    (byte) 0x30, // READ
                    (byte) 41    // page address
            });
            if ((responseAuth0 != null) && (responseAuth0.length >= 16)) {  // read always returns 4 pages
                boolean prot = false;  // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                int auth0 = 0; // first page to be protected, set to a value between 0 and 37 for NTAG212
                responseAuth0 = ultralight.transceive(new byte[] {
                        (byte) 0xA2, // WRITE
                        (byte) 41,   // page address
                        responseAuth0[0], // keep old value for byte 0
                        responseAuth0[1], // keep old value for byte 1
                        responseAuth0[2], // keep old value for byte 2
                        (byte) (0xff)
                });
            }

            Password = "00";
            //  ultralight.writePage(44, Password.getBytes(Charset.forName("US-ASCII")));
            // SET LOCK TYPE
            /*Password = "00";
            ultralight.writePage(44, Password.getBytes(Charset.forName("US-ASCII")));*/

           /* Password = "0";
            ultralight.writePage(37, (byte) 00);*/


        } finally {
            ultralight.close();
        }


    }


    public void PasswordProtect(Tag Tag) throws IOException {

        String Password = "11";
        MifareUltralight ultralight = MifareUltralight.get(Tag);
        try {


            ultralight.connect();

           /* Password = "1111";
            byte[] t = Password.getBytes(Charset.forName("US-ASCII"));

            byte[] presponse = ultralight.transceive(new byte[] {
                    (byte) 0x1B, // PWD_AUTH
                    t[0],t[1],t[2],t[3]
            });
            byte[] pack = null;
            if ((presponse != null) && (presponse.length >= 2)) {
                pack = Arrays.copyOf(presponse, 2);
                // TODO: verify PACK to confirm that tag is authentic (not really,
                // but that whole PWD_AUTH/PACK authentication mechanism was not
                // really meant to bring much security, I hope; same with the
                // NTAG signature btw.)
            }*/



            Password = "1111";
            byte[] t = Password.getBytes(Charset.forName("US-ASCII"));

            //SET PASSWORD
            //ultralight.writePage( 43, Password.getBytes(Charset.forName("US-ASCII")));
            byte[] response = ultralight.transceive(new byte[] {
                    (byte) 0xA2, // WRITE
                    (byte) 43,   // page address
                    t[0],t[1],t[2],t[3]
            });

            //  SET ACKNOLOWLEDGEMENT
            Password = "00";

            byte[] tt = Password.getBytes(Charset.forName("US-ASCII"));

           // ultralight.connect();
            byte[] responseAut = ultralight.transceive(new byte[] {
                    (byte) 0xA2, // WRITE
                    (byte) 44,   // page address
                    tt[0],tt[1],(byte) 0, (byte) 0
            });

            byte[] responseAuth0 = ultralight.transceive(new byte[] {
                    (byte) 0x30, // READ
                    (byte) 41    // page address
            });
            if ((responseAuth0 != null) && (responseAuth0.length >= 16)) {  // read always returns 4 pages
                boolean prot = false;  // false = PWD_AUTH for write only, true = PWD_AUTH for read and write
                int auth0 = 0; // first page to be protected, set to a value between 0 and 37 for NTAG212
                responseAuth0 = ultralight.transceive(new byte[] {
                        (byte) 0xA2, // WRITE
                        (byte) 41,   // page address
                        responseAuth0[0], // keep old value for byte 0
                        responseAuth0[1], // keep old value for byte 1
                        responseAuth0[2], // keep old value for byte 2
                        (byte) (auth0 & 0x0ff)
                });
            }

            Password = "00";
          //  ultralight.writePage(44, Password.getBytes(Charset.forName("US-ASCII")));
            // SET LOCK TYPE
            /*Password = "00";
            ultralight.writePage(44, Password.getBytes(Charset.forName("US-ASCII")));*/

           /* Password = "0";
            ultralight.writePage(37, (byte) 00);*/


        } finally {
            ultralight.close();
        }


    }



    public void NewwriteTag(Tag tag, String tagText) {
        tagText = "AAAABBBB";
        String Password = "1111";
        MifareUltralight ultralight = MifareUltralight.get(tag);

        try {
            ultralight.connect();
            /*byte[] t = Password.getBytes(Charset.forName("US-ASCII"));

            byte[] presponse = ultralight.transceive(new byte[] {
                    (byte) 0x1B, // PWD_AUTH
                    t[0],t[1],t[2],t[3]
            });
            byte[] pack = null;
            if ((presponse != null) && (presponse.length >= 2)) {
                pack = Arrays.copyOf(presponse, 2);
                // TODO: verify PACK to confirm that tag is authentic (not really,
                // but that whole PWD_AUTH/PACK authentication mechanism was not
                // really meant to bring much security, I hope; same with the
                // NTAG signature btw.)
            }*/

            // Smallest tag only has 64 bytes
            if (tagText.length() / 4 < 12) {

                for (int i = 0; i < ((tagText.length() / 4)); i++) {
                    int end = (i + 1) * 4 > tagText.length() ? tagText.length() : (i + 1) * 4;
                    //Clear out the existing data
                    ultralight.writePage(i + 4, "    ".getBytes(Charset.forName("US-ASCII")));
                    //Write new data
                    ultralight.writePage(i + 4, tagText.substring(i * 4, end).getBytes(Charset.forName("US-ASCII")));
                    // Log.d(TAG, "NFC Writte:" + tagText.substring(i * 4, end));
                }
            }
        } catch (IOException e) {
            // Log.d(TAG, "IOException while closing MifareUltralight...", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                //Log.d(TAG, "IOException while closing MifareUltralight...", e);
            }
        }
    }


    // returns tag UID;
    public String readWriteTag(Tag tag, Context context) {
        MifareUltralight mifare = MifareUltralight.get(tag);

        try {
            mifare.connect();
            byte[] data = new byte[MU_BYTE_LIMIT];

            // reading tag;
            for (int i = 0; i < MU_NUM_PAGES; i += MU_PAGES_PER_READ) {
                System.arraycopy(mifare.readPages(i), 0, data, (i * MU_BYTES_PER_PAGE), MU_BYTES_PER_READ);
            }

            MifareUltralightTag muTag = new MifareUltralightTag(data);
            String muTagUID = muTag.getUID();

            if (isDuplicateTag(muTagUID)) {
                Toast.makeText(context, R.string.message_overwrite_tag, Toast.LENGTH_LONG).show();

                MifareUltralightTag muExistentTag = tags.stream().filter(t -> t.getUID().equals(muTagUID)).collect(Collectors.toList()).get(0);

                // writing tag;
                for (int i = MU_FIRST_WRITABLE_PAGE; i < MU_NUM_PAGES; i++) {
                    mifare.writePage(i, muExistentTag.getPage(i));
                }
            }
            else {
                Toast.makeText(context, R.string.message_read_new_tag, Toast.LENGTH_LONG).show();
                tags.add(muTag);
            }

            return muTagUID;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        finally {
            if (mifare != null) {
                try {
                    mifare.close();
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return null;
    }

    private boolean isDuplicateTag(String tagUID) {
        boolean duplicateTag = false;

        for (MifareUltralightTag tag : tags) {
            if (tag.getUID().equals(tagUID)) {
                duplicateTag = true;
                break;
            }
        }

        return duplicateTag;
    }
}
