package es.uvigo.det.ro.simpledns;

import static es.uvigo.det.ro.simpledns.RRType.TXT;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TXTResourceRecord extends ResourceRecord {
    private final String txt;

    public TXTResourceRecord(DomainName domain, int ttl, String txt) {
        super(domain, TXT, ttl, RRDataBytes(txt));
        this.txt = txt;
    }

    protected TXTResourceRecord(ResourceRecord decoded, final byte[] message) {
        super(decoded);
        txt = new String(getRRData()).substring(1);
    }

    public final String getTXT() {
        return txt;
    }

    private static byte[] RRDataBytes(String txt) {
        ByteArrayOutputStream RRDATA = new ByteArrayOutputStream();
        try {
            RRDATA.write(txt.getBytes());
        } catch (IOException e) {
            Logger.getLogger(TXTResourceRecord.class.getName()).log(Level.SEVERE, null, e);
            System.exit(-1);
        }
        return RRDATA.toByteArray();
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(super.toByteArray());
            os.write(txt.getBytes());
        } catch (IOException ex) {
            Logger.getLogger(NSResourceRecord.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        return os.toByteArray();
    }


}
