package br.edu.ucsal.pooalab.gestoracademico.exception;

/** Vira HTTP 409: o dado já existe (CPF, RA, e-mail ou matrícula repetida). */
public class ConflitoException extends RuntimeException {
    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
