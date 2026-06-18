package br.gov.ust.calculator.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiErrorResponse {

    private int status;
    private String titulo;
    private String detalhe;
    private String path;
    private LocalDateTime timestamp;
    private List<CampoErro> erros;

    @Getter
    @Builder
    public static class CampoErro {
        private String campo;
        private String mensagem;
    }

    public static ApiErrorResponse of(int status, String titulo, String detalhe, String path) {
        return ApiErrorResponse.builder()
                .status(status)
                .titulo(titulo)
                .detalhe(detalhe)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
