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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Miguel Rodriguez Perez
 */
public class DomainName {

    private final static Charset ASCII = Charset.forName("US-ASCII");
    private final List<String> labels = new ArrayList<>();
    private int encoded_length = -1;

    public DomainName(String domain) {
        labels.addAll(Arrays.asList(domain.split("\\.")));
    }

    public DomainName(byte[] domain, byte[] message) {
        fromByteArray(domain, message);
    }

    @Override
    public String toString() {
        String res = "";
        
        return labels.stream().map((label) -> label + '.').reduce(res, String::concat);        
    }

	@Override
	public boolean equals(Object obj){
		if (this.toString().equals(obj.toString()))
			return true;
		else return false;
	}

	private void fromByteArray(final byte[] domain, final byte[] message) {
        int i = 0;
        while (i < domain.length) {
            int size = (domain[i++] & 0xff);
            if (size == 0) {
                break;
            }

            if ((size & 0xc0) == 0xc0) { // Pointer
                int offset = (domain[i++] & 0xff) + ((size & 0x3f) << 8);
                setEncodedLength(i);

                fromByteArray(Arrays.copyOfRange(message, offset, message.length), message);

                return;
            }

            String label = new String(Arrays.copyOfRange(domain, i, i + size), ASCII);
            i += size;
            labels.add(label);
        }

        setEncodedLength(i);
    }

    public byte[] toByteArray() {
        ByteArrayOutputStream bo = new ByteArrayOutputStream(labels.size() * 5); // A good approximation to final size
        labels.forEach((label) -> {
            try {
                bo.write(label.length());
                bo.write(label.getBytes(ASCII));
            } catch (IOException ex) {
                Logger.getLogger(DomainName.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(-1);
            }
        });
        bo.write(0); // Final label

        return bo.toByteArray();
    }

    public int getEncodedLength() {
        if (encoded_length < 0) {
            encoded_length = labels.size(); // 1 byte for each label for its size
            labels.forEach((label) -> {
                encoded_length += label.length();
            });
        }
        return encoded_length;
    }

    private void setEncodedLength(int i) {
        if (encoded_length < 0) { // Not recorded yet
            encoded_length = i; 
        }
    }
}
