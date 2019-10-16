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
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Miguel Rodriguez Perez
 */
public class Message {

    private static final SecureRandom RANDOM = new SecureRandom();
    private final int messageId;
    private DomainName question;
    private RRType questionType;
    private RRClass questionClass;
    private final List<ResourceRecord> answers;
    private final List<ResourceRecord> nameServers;
    private final List<ResourceRecord> additionalRecords;
    private final MessageOptions options;

    /**
     * Constructs a message of the given type
     *
     * @param question The Domain to ask for
     * @param type The type of the query of type
     * @param recursion Wether recursion is desired
     */
    public Message(String question, RRType type, boolean recursion) {
        this(new DomainName(question), type, recursion);
    }

    /**
     * Constructs a message of the given type
     *
     * @param question The Domain to ask for
     * @param type The type of the query of type
     * @param recursion Wether recursion is desired
     */
    public Message(DomainName question, RRType type, boolean recursion) {
        this(question, type, new ArrayList<ResourceRecord>(0), new ArrayList<ResourceRecord>(0), new ArrayList<ResourceRecord>(0), recursion);
    }

    protected Message(DomainName question, RRType type, List<ResourceRecord> answers, List<ResourceRecord> nameServers, List<ResourceRecord> addionalRecords, boolean recursion) {
        this.question = question;
        this.answers = answers;
        this.nameServers = nameServers;
        this.additionalRecords = addionalRecords;
        this.options = new MessageOptions();
        this.questionType = type;
        this.questionClass = RRClass.IN;
        options.setOPCODE(0);
        options.setQR(0);
        options.setRD(recursion ? 1 : 0);
        byte[] id_bytes = new byte[2];
        RANDOM.nextBytes(id_bytes);
        messageId = Utils.int16fromByteArray(id_bytes);
    }

    /**
     * Constructs a message from a byte array
     *
     * @param messageBytes the bytes forming the complete message
     * @throws Exception in case the Message cannot be parsed
     */
    public Message(final byte[] messageBytes) throws Exception {
        byte[] buffer = messageBytes;
        final int length = messageBytes.length;

        messageId = Utils.int16fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);

        options = new MessageOptions(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);

        if (options.getTC()) {
            throw new Exception("We do not know what to do with truncated responses");
        }

        int qcount, acount, nscount, adcount;
        qcount = Utils.int16fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);
        acount = Utils.int16fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);
        nscount = Utils.int16fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);
        adcount = Utils.int16fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);

        if (qcount != 1) {
            throw new Exception("We do not yet support unquestioned queries");
        }

        question = new DomainName(buffer, messageBytes);
        buffer = Arrays.copyOfRange(buffer, question.getEncodedLength(), length);
        questionType = RRType.fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);
        questionClass = RRClass.fromByteArray(buffer);
        buffer = Arrays.copyOfRange(buffer, 2, length);

        answers = new ArrayList<>(acount);
        for (int i = 0; i < acount; i++) {
            ResourceRecord record = ResourceRecord.createResourceRecord(buffer, messageBytes);
            buffer = Arrays.copyOfRange(buffer, record.getEncodedLength(), length);
            answers.add(record);
        }
        nameServers = new ArrayList<>(nscount);
        for (int i = 0; i < nscount; i++) {
            ResourceRecord record = ResourceRecord.createResourceRecord(buffer, messageBytes);
            buffer = Arrays.copyOfRange(buffer, record.getEncodedLength(), length);
            nameServers.add(record);
        }
        additionalRecords = new ArrayList<>(adcount);
        for (int i = 0; i < adcount; i++) {
            ResourceRecord record = ResourceRecord.createResourceRecord(buffer, messageBytes);
            buffer = Arrays.copyOfRange(buffer, record.getEncodedLength(), length);
            additionalRecords.add(record);
        }
    }

    public byte[] toByteArray() throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        os.write(Utils.int16toByteArray(messageId));
        os.write(options.toByteArray());

        os.write(Utils.int16toByteArray(1)); // qdcount;
        os.write(Utils.int16toByteArray(answers.size()));
        os.write(Utils.int16toByteArray(nameServers.size()));
        os.write(Utils.int16toByteArray(additionalRecords.size()));

        os.write(question.toByteArray());
        os.write(questionType.toByteArray());
        os.write(questionClass.toByteArray());

        for (ResourceRecord record : answers) {
            os.write(record.toByteArray());
        }
        for (ResourceRecord record : nameServers) {
            os.write(record.toByteArray());
        }
        for (ResourceRecord record : additionalRecords) {
            os.write(record.toByteArray());
        }

        return os.toByteArray();
    }

    /**
     * @return the messageId
     */
    public int getMessageId() {
        return messageId;
    }

    /**
     * @return the question
     */
    public DomainName getQuestion() {
        return question;
    }

    /**
     * @return the questionType
     */
    public RRType getQuestionType() {
        return questionType;
    }

    /**
     * @return the questionClass
     */
    public RRClass getQuestionClass() {
        return questionClass;
    }

    /**
     * @return the answers
     */
    public List<ResourceRecord> getAnswers() {
        return answers;
    }

    /**
     * @return the nameServers
     */
    public List<ResourceRecord> getNameServers() {
        return nameServers;
    }

    /**
     * @return the ad_records
     */
    public List<ResourceRecord> getAdditonalRecords() {
        return additionalRecords;
    }

    static private class MessageOptions {

        public MessageOptions() {
            QR = OPCODE = AA = TC = RD = RA = Z = RCODE = 0;
        }

        public MessageOptions(final byte[] options_bytes) {
            final int options = Utils.int16fromByteArray(options_bytes);

            RCODE = options & 0x000F;
            Z = (options & 0x0070) >> 4;
            RA = (options & 0x0080) >> 7;
            RD = (options & 0x0100) >> 8;
            TC = (options & 0x0200) >> 9;
            AA = (options & 0x0400) >> 10;
            OPCODE = (options & 0x7800) >> 11;
            QR = (options & 0x8000) >> 15;
        }

        byte[] toByteArray() {
            final int options = RCODE | Z << 4 | RA << 7 | RD << 8 | TC << 9
                    | AA << 10 | OPCODE << 11 | QR << 15;

            return Utils.int16toByteArray(options);
        }

        private int QR;
        private int OPCODE;
        private final int AA;
        private final int TC;
        private int RD;
        private final int RA;
        private final int Z;
        private final int RCODE;

        /**
         * @return the QR
         */
        public boolean getQR() {
            return QR > 0;
        }

        /**
         * @param QR the QR to set
         */
        public void setQR(int QR) {
            this.QR = QR;
        }

        /**
         * @param QR the QR to set
         */
        public void setQR(boolean QR) {
            this.QR = QR ? 1 : 0;
        }

        /**
         * @return the OPCODE
         */
        public int getOPCODE() {
            return OPCODE;
        }

        /**
         * @param OPCODE the OPCODE to set
         */
        public void setOPCODE(int OPCODE) {
            this.OPCODE = OPCODE;
        }

        /**
         * @return the AA
         */
        public boolean getAA() {
            return AA > 0;
        }

        /**
         * @return the TC
         */
        public boolean getTC() {
            return TC != 0;
        }

        /**
         * @return the RD
         */
        public boolean getRD() {
            return RD != 0;
        }

        /**
         * @param RD the RD to set
         */
        public void setRD(int RD) {
            this.RD = RD;
        }

        /**
         * @param RD the RD to set
         */
        public void setRD(boolean RD) {
            this.RD = RD ? 1 : 0;
        }

        /**
         * @return the RA
         */
        public boolean getRA() {
            return RA > 0;
        }

        /**
         * @return the Z
         */
        public int getZ() {
            return Z;
        }

        /**
         * @return the RCODE
         */
        public int getRCODE() {
            return RCODE;
        }
    }
}
