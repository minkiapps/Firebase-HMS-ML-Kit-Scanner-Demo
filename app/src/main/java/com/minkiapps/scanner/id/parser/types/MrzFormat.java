package com.minkiapps.scanner.id.parser.types;

import com.minkiapps.scanner.id.parser.MrzRange;
import com.minkiapps.scanner.id.parser.MrzRecord;
import com.minkiapps.scanner.id.parser.MrzParseException;
import com.minkiapps.scanner.id.parser.records.FrenchIdCard;
import com.minkiapps.scanner.id.parser.records.MRP;
import com.minkiapps.scanner.id.parser.records.MrtdTd1;
import com.minkiapps.scanner.id.parser.records.MrtdTd2;
import com.minkiapps.scanner.id.parser.records.MrvA;
import com.minkiapps.scanner.id.parser.records.MrvB;
import com.minkiapps.scanner.id.parser.records.SlovakId2_34;

/**
 * Lists all supported MRZ formats. Note that the order of the enum constants are important, see for example {@link  #FRENCH_ID}.
 * @author Martin Vysny, Pierrick Martin
 */
public enum MrzFormat {

    /**
     * MRTD td1 format: A three line long, 30 characters per line format.
     */
    MRTD_TD1(3, 30, MrtdTd1.class),
    /**
     * French 2 line/36 characters per line format, used with French ID cards.
     * Need to occur before the {@link #MRTD_TD2} enum constant because of the same values for row/column.
     * See below for the "if" test.
     */
    FRENCH_ID(2, 36, FrenchIdCard.class) {

        public boolean isFormatOf(String[] mrzRows) {
            if (!super.isFormatOf(mrzRows)) {
                return false;
            }
            return mrzRows[0].substring(0, 5).equals("IDFRA");
        }
    },
    /**
     * MRV type-B format: A two lines long, 36 characters per line format.
     * Need to occur before the {@link #MRTD_TD2} enum constant because of the same values for row/column.
     * See below for the "if" test.
     */
    MRV_VISA_B(2, 36, MrvB.class) {

        public boolean isFormatOf(String[] mrzRows) {
            if (!super.isFormatOf(mrzRows)) {
                return false;
            }
            return mrzRows[0].substring(0, 1).equals("V");
        } 
    },    
    /**
     * MRTD td2 format: A two line long, 36 characters per line format.
     */
    MRTD_TD2(2, 36, MrtdTd2.class),
    /**
     * MRV type-A format: A two lines long, 44 characters per line format
     * Need to occur before {@link #PASSPORT} constant because of the same values for row/column.
     * See below for the "if" test.
     */
    MRV_VISA_A(2, 44, MrvA.class) {

        public boolean isFormatOf(String[] mrzRows) {
            if (!super.isFormatOf(mrzRows)) {
                return false;
            }
            return mrzRows[0].substring(0, 1).equals("V");
        }
    }, 
    /**
     * MRP Passport format: A two line long, 44 characters per line format.
     */
    PASSPORT(2, 44, MRP.class),
    /**
     * Unknown 2 line/34 characters per line format, used with old Slovak ID cards.
     */
    SLOVAK_ID_234(2, 34, SlovakId2_34.class);
    public final int rows;
    public final int columns;
    private final Class<? extends MrzRecord> recordClass;

    private MrzFormat(int rows, int columns, Class<? extends MrzRecord> recordClass) {
        this.rows = rows;
        this.columns = columns;
        this.recordClass = recordClass;
    }

    /**
     * Checks if this format is able to parse given serialized MRZ record.
     * @param mrzRows MRZ record, separated into rows.
     * @return true if given MRZ record is of this type, false otherwise.
     */
    public boolean isFormatOf(String[] mrzRows) {
        return rows == mrzRows.length && columns == mrzRows[0].length();
    }

    /**
     * Detects given MRZ format.
     * @param mrz the MRZ string.
     * @return the format, never null.
     */
    public static final MrzFormat get(String mrz) {
        final String[] rows = mrz.split("\n");
        final int cols = rows[0].length();
        for (int i = 1; i < rows.length; i++) {
            if (rows[i].length() != cols) {
                throw new MrzParseException("Different row lengths: 0: " + cols + " and " + i + ": " + rows[i].length(), mrz, new MrzRange(0, 0, 0), null);
            }
        }
        for (final MrzFormat f : values()) {
            if (f.isFormatOf(rows)) {
                return f;
            }
        }
        throw new MrzParseException("Unknown format / unsupported number of cols/rows: " + cols + "/" + rows.length, mrz, new MrzRange(0, 0, 0), null);
    }

    /**
     * Creates new record instance with this type.
     * @return never null record instance.
     */
    public final MrzRecord newRecord() {
        try {
            return recordClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
