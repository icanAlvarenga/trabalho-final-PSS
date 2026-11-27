package br.com.burgerexpress.pedido.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record PedidoRequest(
        @NotNull @Positive @JsonDeserialize(using = NumeroInteiroDeserializer.ParaLong.class)
        Long clienteId,
        @NotNull @Positive @JsonDeserialize(using = NumeroInteiroDeserializer.ParaLong.class)
        Long enderecoId,
        String observacoes,
        @NotEmpty List<@NotNull @Valid ItemPedidoRequest> itens) {

    @JsonAnySetter
    public void rejeitarCampoDesconhecido(String campo, Object valor) {
        throw new IllegalArgumentException("Campo não permitido no pedido: " + campo);
    }
}
