package no.nav.obo_unleash.utils;

public class MsGraphUtils {
    private static final String AD_GRUPPE_ENHET_PREFIKS = "0000-GA-ENHET_";
    public static String tilEnhetId(String displayName) {

        if (displayName == null || !displayName.toUpperCase().startsWith(AD_GRUPPE_ENHET_PREFIKS)) {
            throw new IllegalArgumentException("Ugyldig AD-gruppenavn: " + displayName);
        }
        return displayName.toUpperCase().substring(AD_GRUPPE_ENHET_PREFIKS.length());
    }
}


