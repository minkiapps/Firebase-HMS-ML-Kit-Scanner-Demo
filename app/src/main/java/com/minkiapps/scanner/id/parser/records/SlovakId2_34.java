package com.minkiapps.scanner.id.parser.records;

import com.minkiapps.scanner.id.parser.MrzParser;
import com.minkiapps.scanner.id.parser.MrzRange;
import com.minkiapps.scanner.id.parser.MrzRecord;
import com.minkiapps.scanner.id.parser.types.MrzFormat;

/**
 * Unknown 2 line/34 characters per line format, used with old Slovak ID cards.
 * @author Martin Vysny
 */
public class SlovakId2_34 extends MrzRecord {
    private static final long serialVersionUID = 1L;

    public SlovakId2_34() {
        super(MrzFormat.SLOVAK_ID_234);
    }
    /**
     * For use of the issuing State or 
    organization. Unused character positions 
    shall be completed with filler characters (&lt;)
    repeated up to position 35 as required. 
     */
    public String optional;

    @Override
    public void fromMrz(String mrz) {
        super.fromMrz(mrz);
        final MrzParser p = new MrzParser(mrz);
        setName(p.parseName(new MrzRange(5, 34, 0)));
        documentNumber = p.parseString(new MrzRange(0, 9, 1));
        validDocumentNumber = p.checkDigit(9, 1, new MrzRange(0, 9, 1), "document number");
        nationality = p.parseString(new MrzRange(10, 13, 1));
        dateOfBirth = p.parseDate(new MrzRange(13, 19, 1));
        validDateOfBirth = p.checkDigit(19, 1, new MrzRange(13, 19, 1), "date of birth") && dateOfBirth.isDateValid();
        sex = p.parseSex(20, 1);
        expirationDate = p.parseDate(new MrzRange(21, 27, 1));
        validExpirationDate = p.checkDigit(27, 1, new MrzRange(21, 27, 1), "expiration date") && expirationDate.isDateValid();
        optional = p.parseString(new MrzRange(28, 34, 1));
        // TODO validComposite missing? (final MRZ check digit)
    }

    @Override
    public String toString() {
        return "SlovakId2x34{" + super.toString() + ", optional=" + optional + '}';
    }

    @Override
    public String toMrz() {
        // first line
        final StringBuilder sb = new StringBuilder();
        sb.append(code1);
        sb.append(code2);
        sb.append(MrzParser.toMrz(issuingCountry, 3));
        sb.append(MrzParser.nameToMrz(surname, givenNames, 29));
        sb.append('\n');
        // second line
        sb.append(MrzParser.toMrz(documentNumber, 9));
        sb.append(MrzParser.computeCheckDigitChar(MrzParser.toMrz(documentNumber, 9)));
        sb.append(MrzParser.toMrz(nationality, 3));
        sb.append(dateOfBirth.toMrz());
        sb.append(MrzParser.computeCheckDigitChar(dateOfBirth.toMrz()));
        sb.append(sex.mrz);
        sb.append(expirationDate.toMrz());
        sb.append(MrzParser.computeCheckDigitChar(expirationDate.toMrz()));
        sb.append(MrzParser.toMrz(optional, 6));
        sb.append('\n');
        return sb.toString();
    }
}
