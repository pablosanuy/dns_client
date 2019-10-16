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
 * @author Miguel Rodriguez Perez <miguel@det.uvigo.gal>
 */
class Utils {

    public static byte[] int16toByteArray(int i) {
        byte[] output = new byte[2];

        output[0] = (byte) ((i & 0xFF00) >> 8);
        output[1] = (byte) (i & 0x00FF);

        return output;
    }

    public static byte[] int32toByteArray(int i) {
        byte[] output = new byte[4];

        output[0] = (byte) ((i & 0xFF000000) >> 24);
        output[1] = (byte) ((i & 0x00FF0000) >> 16);
        output[2] = (byte) ((i & 0x0000FF00) >> 8);
        output[3] = (byte) (i & 0x000000FF);

        return output;
    }

    public static int int16fromByteArray(byte[] val) {
        return (val[0] << 8) + (val[1] & 0xff);
    }

    public static int int32fromByteArray(byte[] val) {
        int value = 0;

        for (int i = 0; i < 4; i++) {
            value = (value << 8) + (val[i] & 0xff);
        }

        return value;
    }
}
