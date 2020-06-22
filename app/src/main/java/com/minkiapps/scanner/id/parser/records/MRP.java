package com.minkiapps.scanner.id.parser.records;

import com.minkiapps.scanner.id.parser.MrzParser;
import com.minkiapps.scanner.id.parser.MrzRange;
import com.minkiapps.scanner.id.parser.MrzRecord;
import com.minkiapps.scanner.id.parser.types.MrzFormat;

/**
 * MRP Passport format: A two line long, 44 characters per line format.
 * @author Martin Vysny
 */
public class MRP extends MrzRecord {
    private static final long serialVersionUID = 1L;

    public MRP() {
        super(MrzFormat.PASSPORT);
    }
    /**
     * personal number (may be used by the issuing country as it desires), 14 characters long.
     */
    public String personalNumber;

    public boolean validPersonalNumber;

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
        personalNumber = parser.parseString(new MrzRange(28, 42, 1));
        validPersonalNumber = parser.checkDigit(42, 1, new MrzRange(28, 42, 1), "personal number");
        validComposite = parser.checkDigit(43, 1, parser.rawValue(new MrzRange(0, 10, 1), new MrzRange(13, 20, 1), new MrzRange(21, 43, 1)), "mrz");
    }

    @Override
    public String toString() {
        return "MRP{" + super.toString() + ", personalNumber=" + personalNumber + '}';
    }

    @Override
    public String toMrz() {
        // first line
        final StringBuilder sb = new StringBuilder();
        sb.append(code1);
        sb.append(code2);
        sb.append(MrzParser.toMrz(issuingCountry, 3));
        sb.append(MrzParser.nameToMrz(surname, givenNames, 39));
        sb.append('\n');
        // second line
        final String docNum = MrzParser.toMrz(documentNumber, 9) + MrzParser.computeCheckDigitChar(MrzParser.toMrz(documentNumber, 9));
        sb.append(docNum);
        sb.append(MrzParser.toMrz(nationality, 3));
        final String dob = dateOfBirth.toMrz() + MrzParser.computeCheckDigitChar(dateOfBirth.toMrz());
        sb.append(dob);
        sb.append(sex.mrz);
        final String edpn = expirationDate.toMrz() + MrzParser.computeCheckDigitChar(expirationDate.toMrz()) + MrzParser.toMrz(personalNumber, 14) + MrzParser.computeCheckDigitChar(MrzParser.toMrz(personalNumber, 14));
        sb.append(edpn);
        sb.append(MrzParser.computeCheckDigitChar(docNum + dob + edpn));
        sb.append('\n');
        return sb.toString();
    }
}
