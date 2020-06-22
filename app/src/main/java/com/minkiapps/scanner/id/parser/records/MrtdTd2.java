package com.minkiapps.scanner.id.parser.records;

import com.minkiapps.scanner.id.parser.MrzParser;
import com.minkiapps.scanner.id.parser.MrzRange;
import com.minkiapps.scanner.id.parser.MrzRecord;
import com.minkiapps.scanner.id.parser.types.MrzFormat;

/**
 * MRTD td2 format: A two line long, 36 characters per line format.
 * @author Martin Vysny
 */
public class MrtdTd2 extends MrzRecord {
    private static final long serialVersionUID = 1L;

    public MrtdTd2() {
        super(MrzFormat.MRTD_TD2);
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
        setName(p.parseName(new MrzRange(5, 36, 0)));
        documentNumber = p.parseString(new MrzRange(0, 9, 1));
        validDocumentNumber = p.checkDigit(9, 1, new MrzRange(0, 9, 1), "document number");
        nationality = p.parseString(new MrzRange(10, 13, 1));
        dateOfBirth = p.parseDate(new MrzRange(13, 19, 1));
        validDateOfBirth = p.checkDigit(19, 1, new MrzRange(13, 19, 1), "date of birth") && dateOfBirth.isDateValid();
        sex = p.parseSex(20, 1);
        expirationDate = p.parseDate(new MrzRange(21, 27, 1));
        validExpirationDate = p.checkDigit(27, 1, new MrzRange(21, 27, 1), "expiration date") && expirationDate.isDateValid();
        optional = p.parseString(new MrzRange(28, 35, 1));
        validComposite = p.checkDigit(35, 1, p.rawValue(new MrzRange(0, 10, 1), new MrzRange(13, 20, 1), new MrzRange(21, 35, 1)), "mrz");
    }

    @Override
    public String toString() {
        return "MRTD-TD2{" + super.toString() + ", optional=" + optional + '}';
    }

    @Override
    public String toMrz() {
        // first line
        final StringBuilder sb = new StringBuilder();
        sb.append(code1);
        sb.append(code2);
        sb.append(MrzParser.toMrz(issuingCountry, 3));
        sb.append(MrzParser.nameToMrz(surname, givenNames, 31));
        sb.append('\n');
        // second line
        final String dn = MrzParser.toMrz(documentNumber, 9) + MrzParser.computeCheckDigitChar(MrzParser.toMrz(documentNumber, 9));
        sb.append(dn);
        sb.append(MrzParser.toMrz(nationality, 3));
        final String dob = dateOfBirth.toMrz() + MrzParser.computeCheckDigitChar(dateOfBirth.toMrz());
        sb.append(dob);
        sb.append(sex.mrz);
        final String ed = expirationDate.toMrz() + MrzParser.computeCheckDigitChar(expirationDate.toMrz());
        sb.append(ed);
        sb.append(MrzParser.toMrz(optional, 7));
        sb.append(MrzParser.computeCheckDigitChar(dn + dob + ed + MrzParser.toMrz(optional, 7)));
        sb.append('\n');
        return sb.toString();
    }
}
