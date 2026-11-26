package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import org.springframework.stereotype.Component;

@Component
public class RegraQuantidadePositiva implements RegraBaixaEstoque {

    @Override
    public void validar(Produto produto, int quantidade) {
        if (quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade para baixa de estoque deve ser maior que zero.");
        }
    }
}