package com.minkiapps.scanner.id.parser.types;

import com.minkiapps.scanner.id.parser.MrzRange;
import com.minkiapps.scanner.id.parser.MrzParseException;

/**
 * Lists all supported MRZ record types (a.k.a. document codes).
 * @author Martin Vysny
 */
public enum MrzDocumentCode {

    /**
     * A passport, P or IP.
     * ... maybe Travel Document that is very similar to the passport.
     */
    Passport,
    /**
     * General I type (besides IP).
     */
    TypeI,
    /**
     * General A type (besides AC).
     */
    TypeA,
    /**
     * Crew member (AC).
     */
    CrewMember,
    /**
     * General type C.
     */
    TypeC, 
    /**
     * Type V (Visa).
     */
    TypeV,
    /**
     *
     */
    Migrant;

    /**
     * @author Zsombor
     * turning to switch statement due to lots of types
     *
     * @param mrz
     * @return
     */
    public static MrzDocumentCode parse(String mrz) {
        final String code = mrz.substring(0, 2);

        // 2-letter checks
        switch(code){
            case "IV":
                throw new MrzParseException("IV document code is not allowed", mrz, new MrzRange(0, 2, 0), null); // TODO why?
            case "AC": return CrewMember;
            case "ME": return Migrant;
            case "TD": return Migrant; // travel document
            case "IP": return Passport;
        }

        // 1-letter checks
        switch(code.charAt(0)){
            case 'T':   // usually Travel Document
            case 'P': return Passport;
            case 'A': return TypeA;
            case 'C': return TypeC;
            case 'V': return TypeV;
            case 'I': return TypeI; // identity card or residence permit
            case 'R': return Migrant;  // swedish '51 Convention Travel Document
        }


        throw new MrzParseException("Unsupported document code: " + code, mrz, new MrzRange(0, 2, 0), null);
    }
}
