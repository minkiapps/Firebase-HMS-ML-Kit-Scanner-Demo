package com.minkiapps.scanner.id.parser;

import com.minkiapps.scanner.id.parser.types.MrzFormat;

/**
 * Thrown when a MRZ parse fails.
 * @author Martin Vysny
 */
public class MrzParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    /**
     * The MRZ string being parsed.
     */
    public final String mrz;
    /**
     * Range containing problematic characters.
     */
    public final MrzRange range;
    /**
     * Expected MRZ format.
     */
    public final MrzFormat format;

    public MrzParseException(String message, String mrz, MrzRange range, MrzFormat format) {
        super("Failed to parse MRZ " + format + " " + mrz + " at " + range + ": " + message);
        this.mrz = mrz;
        this.format = format;
        this.range = range;
    }
}
