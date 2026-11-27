package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class RegraQuantidadePositiva implements RegraReservaEstoque {

    @Override
    public void validar(Produto produto, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade para reserva de estoque deve ser maior que zero.");
        }
    }
}