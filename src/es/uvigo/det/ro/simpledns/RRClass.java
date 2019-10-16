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

/**
 *
 * @author Miguel Rodriguez Perez
 */
public enum RRClass {
    IN(1), // the Internet
    CH(3), // the CHAOS class
    HS(4); // Hesiod      

    static RRClass fromByteArray(final byte[] bytes) throws Exception {
        final int val = Utils.int16fromByteArray(bytes);

        for (RRClass id : values()) {
            if (val == id.id) {
                return id;
            }
        }

        throw (new Exception("Unsupported RRClass: " + val));
    }


    private final int id;
    // Hesiod
    
    private RRClass(int id) {
        this.id = id;
    }
    public byte[] toByteArray() {
        return Utils.int16toByteArray(id);
    }
}
