package br.edu.ucsal.pooalab.gestoracademico.domain;

import java.util.Locale;

/**
 * Normaliza RA, CPF e e-mail sempre do mesmo jeito, no cadastro e no login.
 * Assim "123.456.789-09" e "12345678909" são o mesmo CPF.
 */
public final class Identificadores {

    private Identificadores() {
    }

    public static String cpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    public static String ra(String ra) {
        return ra == null ? null : ra.trim().toUpperCase(Locale.ROOT);
    }

    public static String email(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Um identificador de login com 11 dígitos (com ou sem pontuação) pode ser um CPF. */
    public static boolean pareceCpf(String identificador) {
        String digitos = cpf(identificador);
        return digitos != null && digitos.length() == 11
                && identificador.trim().matches("[\\d.\\-\\s]+");
    }
}
