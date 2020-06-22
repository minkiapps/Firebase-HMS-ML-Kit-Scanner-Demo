package com.minkiapps.scanner.id.parser.records;

import com.minkiapps.scanner.id.parser.MrzParser;
import com.minkiapps.scanner.id.parser.MrzRange;
import com.minkiapps.scanner.id.parser.MrzRecord;
import com.minkiapps.scanner.id.parser.types.MrzDocumentCode;
import com.minkiapps.scanner.id.parser.types.MrzFormat;

/**
 * Format used for French ID Cards.
 * <p/>
 * The structure of the card:
 * 2 lines of 36 characters :
<pre>First line : IDFRA{name}{many < to complete line}{6 numbers unknown}
Second line : {card number on 12 numbers}{Check digit}{given names separated by "<<" and maybe troncated if too long}{date of birth YYMMDD}{Check digit}{sex M/F}{1 number checksum}</pre>
 * @author Pierrick Martin, Marin Moulinier
 */
public class FrenchIdCard extends MrzRecord {

    private static final long serialVersionUID = 1L;

    public FrenchIdCard() {
        super(MrzFormat.FRENCH_ID);
        code = MrzDocumentCode.TypeI;
        code1 = 'I';
        code2 = 'D';
    }
    /**
     * For use of the issuing State or 
    organization.
     */
    public String optional;

    @Override
    public void fromMrz(String mrz) {
        super.fromMrz(mrz);
        final MrzParser p = new MrzParser(mrz);
        //Special because surname and firstname not on the same line
        String[] name = new String[]{"", ""};
        name[0] = p.parseString(new MrzRange(5, 30, 0));
        name[1] = p.parseString(new MrzRange(13, 27, 1));
        setName(name);
        nationality = p.parseString(new MrzRange(2, 5, 0));
        optional = p.parseString(new MrzRange(30, 36, 0));
        documentNumber = p.parseString(new MrzRange(0, 12, 1));
        validDocumentNumber = p.checkDigit(12, 1, new MrzRange(0, 12, 1), "document number");
        dateOfBirth = p.parseDate(new MrzRange(27, 33, 1));
        validDateOfBirth = p.checkDigit(33, 1, new MrzRange(27, 33, 1), "date of birth") && dateOfBirth.isDateValid();
        sex = p.parseSex(34, 1);
        final String finalChecksum = mrz.toString().replace("\n","").substring(0, 36 + 35);
        validComposite = p.checkDigit(35, 1, finalChecksum, "final checksum");
        // TODO expirationDate is missing
    }

    @Override
    public String toString() {
        return "FrenchIdCard{" + super.toString() + ", optional=" + optional + '}';
    }

    @Override
    public String toMrz() {
        final StringBuilder sb = new StringBuilder("IDFRA");
        // first row
        sb.append(MrzParser.toMrz(surname, 25));
        sb.append(MrzParser.toMrz(optional, 6));
        sb.append('\n');
        // second row
        sb.append(MrzParser.toMrz(documentNumber, 12));
        sb.append(MrzParser.computeCheckDigitChar(MrzParser.toMrz(documentNumber, 12)));
        sb.append(MrzParser.toMrz(givenNames, 14));
        sb.append(dateOfBirth.toMrz());
        sb.append(MrzParser.computeCheckDigitChar(dateOfBirth.toMrz()));
        sb.append(sex.mrz);
        sb.append(MrzParser.computeCheckDigitChar(sb.toString().replace("\n","")));
        sb.append('\n');
        return sb.toString();
    }
}
