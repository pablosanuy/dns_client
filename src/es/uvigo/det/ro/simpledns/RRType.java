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

public enum RRType {
    A(1), // a host address
    NS(2), // an authoritative name server
    CNAME(5), // the canonical name for an alias
    SOA(6), // marks the start of a zone of authority
    PTR(12), // a domain name pointer
    HINFO(13), // host information
    MX(15), // mail exchange
    TXT(16), // text strings
    AAAA(28); // an IPv6 host address*/

    static RRType fromByteArray(final byte[] bytes) throws Exception {
        final int val = Utils.int16fromByteArray(bytes);
        
        for (RRType id : values()) {
            if (val == id.id) {
                return id;
            }
        }

        throw (new Exception("Unsupported RRType: " + val));
    }


    private final int id;
    // an IPv6 host address*/
    
    private RRType(int id) {
        this.id = id;
    }
    public byte[] toByteArray() {
        return Utils.int16toByteArray(id);
    }
}
