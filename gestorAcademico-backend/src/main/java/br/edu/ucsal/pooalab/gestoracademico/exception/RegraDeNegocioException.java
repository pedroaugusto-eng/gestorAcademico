package br.edu.ucsal.pooalab.gestoracademico.exception;

/** Vira HTTP 422: o pedido é válido, mas fere uma regra acadêmica. */
public class RegraDeNegocioException extends RuntimeException {
    public RegraDeNegocioException(String mensagem) {
        super(mensagem);
    }
}
