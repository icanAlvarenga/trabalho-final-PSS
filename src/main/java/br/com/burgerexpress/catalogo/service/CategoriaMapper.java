package br.com.burgerexpress.catalogo.service;

import br.com.burgerexpress.catalogo.dto.CategoriaRequest;
import br.com.burgerexpress.catalogo.dto.CategoriaResponse;
import br.com.burgerexpress.catalogo.entity.Categoria;

public interface CategoriaMapper {

    Categoria paraEntidade(CategoriaRequest request);

    CategoriaResponse paraResponse(Categoria categoria);
}