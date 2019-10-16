/*
 * Copyright (C) 2016 Miguel Rodriguez Perez <miguel@det.uvigo.gal>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.uvigo.det.ro.simpledns;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel Rodriguez Perez
 */
public class ResourceRecord {

    /**
     * Creates a new ResourceRecord based on a byte array
     *
     * @param rrecord A byte array with the serialized RR
     * @param message A byte array containing the complete message
     * @return A ResourceRecord of the appropriate type
     * @throws Exception in case the bytes cannot be fully parsed
     */
    static public ResourceRecord createResourceRecord(final byte[] rrecord, final byte[] message) throws Exception {
        ResourceRecord temp = new ResourceRecord(rrecord, message);

        switch (temp.getRRType()) {
            case A:
                return new AResourceRecord(temp);
            case AAAA:
                return new AAAAResourceRecord(temp);
            case NS:
                return new NSResourceRecord(temp, message);
            default:
                return temp;
        }
    }

    private final DomainName domain;
    private final RRType rrtype;
    private final RRClass rrclass;
    private final int ttl;
    private final int rdlength;
    private final byte[] rrdata;

    protected ResourceRecord(DomainName domain, RRType type, int ttl, final byte[] rrdata) {
        this.domain = domain;
        this.rrtype = type;
        this.rrclass = RRClass.IN;
        this.ttl = ttl;
        this.rdlength = rrdata.length;
        this.rrdata = rrdata;
    }

    protected ResourceRecord(final byte[] record, final byte[] message) throws Exception {
        byte[] buffer = record;

        domain = new DomainName(record, message);
        buffer = Arrays.copyOfRange(buffer, domain.getEncodedLength(), record.length);

        rrtype = RRType.fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, record.length);

        rrclass = RRClass.fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, record.length);

        ttl = Utils.int32fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 4, record.length);

        rdlength = Utils.int16fromByteArray(buffer);

        rrdata = Arrays.copyOfRange(buffer, 2, 2 + rdlength);
    }

    protected ResourceRecord(ResourceRecord copy) {
        this.domain = copy.domain;
        this.rrtype = copy.rrtype;
        this.rrclass = copy.rrclass;
        this.ttl = copy.ttl;
        this.rdlength = copy.rdlength;
        this.rrdata = copy.rrdata;
    }

    /**
     * Required size to serialize the Resource Record
     *
     * @return the number of bytes needed
     */
    public int getEncodedLength() {
        return commonSize() + getRDLength();
    }

    protected byte[] toByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream(commonSize());

        try {
            os.write(domain.toByteArray());
            os.write(rrtype.toByteArray());
            os.write(rrclass.toByteArray());
            os.write(Utils.int32toByteArray(ttl));
            os.write(Utils.int16toByteArray(rdlength));
        } catch (IOException ex) {
            // This Exception should not happen ever
            Logger.getLogger(ResourceRecord.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }

        return os.toByteArray();
    }

    protected final int commonSize() {
        return domain.getEncodedLength() + 2 + 2 + 4 + 2; // type + class + ttl + rdlength
    }

    /**
     * The DomainName of the ResourceRecord
     *
     * @return
     */
    public final DomainName getDomain() {
        return domain;
    }

    /**
     * The Type of the ResourceRecourd
     *
     * @return
     */
    public final RRType getRRType() {
        return rrtype;
    }

    /**
     * The class of the Resource Record
     *
     * @return
     */
    public final RRClass getRRClass() {
        return rrclass;
    }

    /**
     * The TTL (in seconds) of the ResourceRecord. Note that this field is not
     * updated by this class. Care must be taken to update it if it is to be
     * stored in a cache object.
     *
     * @return
     */
    public final int getTTL() {
        return ttl;
    }

    /**
     * Number of bytes required to hold the RRData
     *
     * @return
     */
    public final int getRDLength() {
        return rdlength;
    }

    /**
     * Undecoded data in this ResourceRecord. Needed if the type is not
     * supported by this library.
     *
     * @return
     */
    public final byte[] getRRData() {
        return rrdata;
    }
}
