package br.com.burgerexpress.catalogo.service;

import br.com.burgerexpress.catalogo.dto.ProdutoRequest;
import br.com.burgerexpress.catalogo.dto.ProdutoResponse;
import br.com.burgerexpress.catalogo.dto.ProdutoResumo;
import br.com.burgerexpress.catalogo.entity.Categoria;
import br.com.burgerexpress.catalogo.entity.Produto;

public interface ProdutoMapper {

    Produto paraEntidade(ProdutoRequest request, Categoria categoria);

    ProdutoResponse paraResponse(Produto produto);

    ProdutoResumo paraResumo(Produto produto);
}