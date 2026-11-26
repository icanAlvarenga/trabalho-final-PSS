package br.com.burgerexpress.catalogo.service;

import br.com.burgerexpress.catalogo.dto.AtualizarEstoqueRequest;
import br.com.burgerexpress.catalogo.dto.ProdutoRequest;
import br.com.burgerexpress.catalogo.dto.ProdutoResponse;
import br.com.burgerexpress.catalogo.dto.ProdutoResumo;
import br.com.burgerexpress.catalogo.entity.Categoria;
import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.catalogo.service.ProdutoMapper;
import br.com.burgerexpress.catalogo.service.regra.RegraBaixaEstoque;
import br.com.burgerexpress.catalogo.repository.CategoriaRepository;
import br.com.burgerexpress.catalogo.repository.ProdutoRepository;
import br.com.burgerexpress.shared.exception.ConflitoException;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoMapper produtoMapper;
    private final List<RegraBaixaEstoque> regrasBaixaEstoque;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            ProdutoMapper produtoMapper,
            List<RegraBaixaEstoque> regrasBaixaEstoque) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoMapper = produtoMapper;
        this.regrasBaixaEstoque = regrasBaixaEstoque;
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponse> listar(Long categoriaId, Boolean disponivel) {
        List<Produto> produtos;

        if (categoriaId != null && disponivel != null) {
            produtos = produtoRepository.findByCategoriaIdAndDisponivel(categoriaId, disponivel);
        } else if (categoriaId != null) {
            produtos = produtoRepository.findByCategoriaId(categoriaId);
        } else if (disponivel != null) {
            produtos = produtoRepository.findByDisponivel(disponivel);
        } else {
            produtos = produtoRepository.findAll();
        }

        return produtos.stream()
                .map(produtoMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return produtoMapper.paraResponse(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public ProdutoResumo buscarResumo(Long id) {
        return produtoMapper.paraResumo(buscarEntidade(id));
    }

    @Transactional
    public ProdutoResponse criar(ProdutoRequest request) {
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());
        String nome = request.nome().trim();

        if (produtoRepository.existsByNomeIgnoreCaseAndCategoriaId(nome, categoria.getId())) {
            throw new ConflitoException("Já existe um produto '" + nome + "' nesta categoria.");
        }

        Produto salvo = produtoRepository.save(produtoMapper.paraEntidade(request, categoria));
        return produtoMapper.paraResponse(salvo);
    }

    @Transactional
    public ProdutoResponse atualizar(Long id, ProdutoRequest request) {
        Produto produto = buscarEntidade(id);
        Categoria categoria = buscarCategoriaAtiva(request.categoriaId());
        String nome = request.nome().trim();

        if (produtoRepository.existsByNomeIgnoreCaseAndCategoriaIdAndIdNot(nome, categoria.getId(), id)) {
            throw new ConflitoException("Já existe outro produto '" + nome + "' nesta categoria.");
        }

        produto.setNome(nome);
        produto.setDescricao(request.descricao() != null ? request.descricao().trim() : null);
        produto.setPreco(request.preco());
        produto.setEstoque(request.estoque());
        produto.setCategoria(categoria);
        if (request.disponivel() != null) {
            produto.setDisponivel(request.disponivel());
        }

        return produtoMapper.paraResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResponse atualizarEstoque(Long id, AtualizarEstoqueRequest request) {
        Produto produto = buscarEntidade(id);
        produto.setEstoque(request.estoque());
        return produtoMapper.paraResponse(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResumo baixarEstoque(Long id, int quantidade) {
        Produto produto = buscarEntidade(id);
        for (RegraBaixaEstoque regra : regrasBaixaEstoque) {
            regra.validar(produto, quantidade);
        }
        produto.baixarEstoque(quantidade);
        return produtoMapper.paraResumo(produtoRepository.save(produto));
    }

    @Transactional
    public ProdutoResumo reporEstoque(Long id, int quantidade) {
        if (quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade para reposição de estoque deve ser maior que zero.");
        }
        Produto produto = buscarEntidade(id);
        produto.reporEstoque(quantidade);
        return produtoMapper.paraResumo(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);
        produtoRepository.delete(produto);
    }

    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto não encontrado com id: " + id));
    }

    private Categoria buscarCategoriaAtiva(Long categoriaId) {
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException(
                        "Categoria não encontrada com id: " + categoriaId));

        if (!categoria.isAtiva()) {
            throw new DadosInvalidosException("Não é possível vincular produto a uma categoria inativa.");
        }
        return categoria;
    }
}