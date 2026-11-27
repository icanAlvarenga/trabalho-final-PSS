package br.com.burgerexpress.pedido.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ItemPedidoRequest(
        @NotNull @Positive @JsonDeserialize(using = NumeroInteiroDeserializer.ParaLong.class)
        Long produtoId,
        @NotNull @Positive @JsonDeserialize(using = NumeroInteiroDeserializer.ParaInteger.class)
        Integer quantidade) {

    @JsonAnySetter
    public void rejeitarCampoDesconhecido(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido no item do pedido: " + campo);
    }
}
