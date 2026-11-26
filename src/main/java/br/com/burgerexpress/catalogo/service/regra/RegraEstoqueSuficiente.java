package br.com.burgerexpress.catalogo.service.regra;

import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import org.springframework.stereotype.Component;

@Component
public class RegraEstoqueSuficiente implements RegraBaixaEstoque {

    @Override
    public void validar(Produto produto, int quantidade) {
        if (produto.getEstoque() < quantidade) {
            throw new DadosInvalidosException(
                    "Estoque insuficiente para o produto '" + produto.getNome()
                            + "'. Disponível: " + produto.getEstoque()
                            + ", solicitado: " + quantidade + ".");
        }
    }
}