package com.minkiapps.scanner.id.parser.records;

import com.minkiapps.scanner.id.parser.MrzParser;
import com.minkiapps.scanner.id.parser.MrzRange;
import com.minkiapps.scanner.id.parser.MrzRecord;
import com.minkiapps.scanner.id.parser.types.MrzFormat;
import com.minkiapps.scanner.id.parser.types.MrzDocumentCode;

/**
 * MRV type-A format: A two lines long, 44 characters per line format
 * @author Jeremy Le Berre
 */
public class MrvA extends MrzRecord {

    private static final long serialVersionUID = 1L;

    public MrvA() {
        super(MrzFormat.MRV_VISA_A);
        code1 = 'V';
        code2 = '<';
        code = MrzDocumentCode.TypeV;
    }
    /**
     * Optional data at the discretion of the issuing State
     */
    public String optional;

    @Override
    public void fromMrz(String mrz) {
        super.fromMrz(mrz);
        final MrzParser parser = new MrzParser(mrz);
        setName(parser.parseName(new MrzRange(5, 44, 0)));
        documentNumber = parser.parseString(new MrzRange(0, 9, 1));
        validDocumentNumber = parser.checkDigit(9, 1, new MrzRange(0, 9, 1), "passport number");
        nationality = parser.parseString(new MrzRange(10, 13, 1));
        dateOfBirth = parser.parseDate(new MrzRange(13, 19, 1));
        validDateOfBirth = parser.checkDigit(19, 1, new MrzRange(13, 19, 1), "date of birth") && dateOfBirth.isDateValid();
        sex = parser.parseSex(20, 1);
        expirationDate = parser.parseDate(new MrzRange(21, 27, 1));
        validExpirationDate = parser.checkDigit(27, 1, new MrzRange(21, 27, 1), "expiration date") && expirationDate.isDateValid();
        optional = parser.parseString(new MrzRange(28, 44, 1));
        // TODO validComposite missing? (final MRZ check digit)
    }

    @Override
    public String toString() {
        return "MRV-A{" + super.toString() + ", optional=" + optional + '}';
    }

    @Override
    public String toMrz() {
        // first line
        final StringBuilder sb = new StringBuilder("V<");
        sb.append(MrzParser.toMrz(issuingCountry, 3));
        sb.append(MrzParser.nameToMrz(surname, givenNames, 39));
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
        sb.append(MrzParser.toMrz(optional, 16));
        sb.append('\n');
        return sb.toString();
    }
}
