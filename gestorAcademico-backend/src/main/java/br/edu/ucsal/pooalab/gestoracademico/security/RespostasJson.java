package br.edu.ucsal.pooalab.gestoracademico.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Escreve as respostas do Spring Security no mesmo formato (Problem Details) que o resto da API. */
@Component
public class RespostasJson {

    private final ObjectMapper objectMapper;

    public RespostasJson(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void erro(HttpServletResponse response, HttpStatus status, String detalhe) throws IOException {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("type", "about:blank");
        corpo.put("title", status.getReasonPhrase());
        corpo.put("status", status.value());
        corpo.put("detail", detalhe);
        escrever(response, status.value(), MediaType.APPLICATION_PROBLEM_JSON_VALUE, corpo);
    }

    public void ok(HttpServletResponse response, Object corpo) throws IOException {
        escrever(response, HttpStatus.OK.value(), MediaType.APPLICATION_JSON_VALUE, corpo);
    }

    private void escrever(HttpServletResponse response, int status, String tipo, Object corpo) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(tipo);
        objectMapper.writeValue(response.getOutputStream(), corpo);
    }
}
