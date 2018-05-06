package es.uvigo.det.ro.simpledns;

import static es.uvigo.det.ro.simpledns.RRType.MX;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sergio Simons
 */

public class MXResourceRecord extends ResourceRecord {
    private final int prioridad;
    private final DomainName servidor;

    public MXResourceRecord(DomainName domain, int ttl, int prioridad, DomainName servidor) {
        super(domain, MX, ttl, RRDataBytes(prioridad, servidor));
        this.prioridad = prioridad;
        this.servidor = servidor;
    }

    protected MXResourceRecord(ResourceRecord decoded, final byte[] message) {
        super(decoded);
        prioridad = Utils.int16fromByteArray(getRRData());
        servidor = new DomainName(Arrays.copyOfRange(getRRData(), 2, getRRData().length), message);
    }

    public final DomainName getMXservidor() {
        return servidor;
    }

    public final int getMXprioridad() {
        return prioridad;
    }

    private static byte[] RRDataBytes(int prioridad, DomainName servidor) {
        ByteArrayOutputStream RRDATA = new ByteArrayOutputStream();
        try {
            RRDATA.write(Utils.int16toByteArray(prioridad));
            RRDATA.write(servidor.toByteArray());
        } catch (IOException e) {
            Logger.getLogger(MXResourceRecord.class.getName()).log(Level.SEVERE, null, e);
            System.exit(-1);
        }
        return RRDATA.toByteArray();
    }

    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(super.toByteArray());
            os.write(Utils.int16toByteArray(prioridad));
            os.write(servidor.toByteArray());
        } catch (IOException ex) {
            Logger.getLogger(MXResourceRecord.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        return os.toByteArray();
    }

}