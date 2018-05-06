
package es.uvigo.det.ro.simpledns;

import static es.uvigo.det.ro.simpledns.RRType.CNAME;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sergio Simons
 */
public class CNAMEResourceRecord extends ResourceRecord {
    private final DomainName cname;

    public CNAMEResourceRecord(DomainName domain, int ttl, DomainName cname) {
        super(domain, CNAME, ttl, cname.toByteArray());

        this.cname = cname;
    }

    protected CNAMEResourceRecord(ResourceRecord decoded, final byte[] message) {
        super(decoded);

        cname = new DomainName(getRRData(), message);
    }

    public final DomainName getCNAME() {
        return cname;
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try {
            os.write(super.toByteArray());
            os.write(cname.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(CNAMEResourceRecord.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

        return os.toByteArray();
    }

}