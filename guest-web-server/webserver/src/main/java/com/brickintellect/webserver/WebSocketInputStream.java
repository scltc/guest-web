package com.brickintellect.webserver;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.IOException;
import java.io.UTFDataFormatException;

public class WebSocketInputStream extends ByteArrayInputStream {

    private final static String utfExceptionMessage = "Improper UTF-8 endpoint encoding.";
    private final static String endpointExceptionMessage = "Improper endpoint prefix (expected \"digit(s):\").";

    /***
     * Read a single UTF-8 character from an InputStream.
     * 
     * @param stream
     * @return The character that was read.
     * @throws IOException
     */

    private static char readOneCharacter(final InputStream stream) throws IOException {

        char result;

        int theByte = stream.read();

        if ((theByte & 0x80) == 0) {
            result = (char) theByte;
        } else {

            if (theByte == -1) {
                throw new EOFException(utfExceptionMessage);
            }

            int remain;

            if ((theByte & 0xE0) == 0xC0) {
                remain = 1;
                result = (char) (theByte & 0x1F);
            } else if ((theByte & 0xF0) == 0xE0) {
                remain = 2;
                result = (char) (theByte & 0x0F);
            } else if ((theByte & 0xF8) == 0xF0) {
                remain = 3;
                result = (char) (theByte & 0x07);
            } else {
                throw new UTFDataFormatException(utfExceptionMessage);
            }

            while (remain-- > 0) {
                if ((theByte = stream.read()) == -1) {
                    throw new UTFDataFormatException(utfExceptionMessage);
                }

                if ((theByte & 0xC0) != 0x80) {
                    throw new UTFDataFormatException(utfExceptionMessage);
                }

                result = (char) ((result << 6) | (theByte & 0x3F));
            }
        }

        return result;
    }

    private static int getEndpoint(InputStream stream) throws IOException {
        int result = 0;

        for (char character; (character = readOneCharacter(stream)) != ':';) {

            if (!Character.isDigit(character)) {
                throw new IOException(endpointExceptionMessage);
            }
            result = result * 10 + character - '0';
        }

        return result;
    }

    private final int endpoint;

    public WebSocketInputStream(byte[] buffer) throws IOException {

        super(buffer);

        endpoint = getEndpoint(this);

        System.out.println("endpoint: " + endpoint);
    }

    public int getEndpointNumber() {
        return endpoint;
    }
}