package no.nav.obo_unleash.utils;

public class NAVidentUtils {
    public static boolean erNavIdent(String verdi) {
        return verdi != null && verdi.matches("\\w\\d{6}");
    }
}
