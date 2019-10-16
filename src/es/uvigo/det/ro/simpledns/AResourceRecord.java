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

import static es.uvigo.det.ro.simpledns.RRType.A;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel Rodriguez Perez
 */
public class AResourceRecord extends ResourceRecord {
    private final Inet4Address addr;

    public AResourceRecord(DomainName domain, int ttl, Inet4Address addr) {
        super(domain, A, ttl, addr.getAddress());

        this.addr = addr;
    }

    protected AResourceRecord(ResourceRecord decoded) throws Exception {
        super(decoded);        
       
        if (getRDLength() != 4) {
            throw new Exception("Incorrect rdlength for A Resource Records");
        }

        InetAddress ad = InetAddress.getByAddress(getRRData());
        if (!(ad instanceof Inet4Address)) {
            throw new Exception("Address is not a valid IPv4 Address");
        }

        addr = (Inet4Address) ad;
    }

    public final Inet4Address getAddress() {
        return addr;
    }
    
    @Override
    public byte[] toByteArray() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        
        try {                        
            os.write(super.toByteArray());
            os.write(addr.getAddress());                        
        } catch (IOException ex) {
            Logger.getLogger(AResourceRecord.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(-1);
        }
        
        return os.toByteArray();
    }    
}
