package br.com.burgerexpress.catalogo.service;

import br.com.burgerexpress.catalogo.dto.AtualizarEstoqueRequest;
import br.com.burgerexpress.catalogo.dto.ProdutoRequest;
import br.com.burgerexpress.catalogo.dto.ProdutoResponse;
import br.com.burgerexpress.catalogo.dto.ProdutoResumo;
import br.com.burgerexpress.catalogo.entity.Categoria;
import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.catalogo.repository.CategoriaRepository;
import br.com.burgerexpress.catalogo.repository.ProdutoRepository;
import br.com.burgerexpress.catalogo.service.regra.RegraReservaEstoque;
import br.com.burgerexpress.shared.exception.ConflitoException;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProdutoMapper produtoMapper;
    private final List<RegraReservaEstoque> regrasReservaEstoque;

    public ProdutoService(
            ProdutoRepository produtoRepository,
            CategoriaRepository categoriaRepository,
            ProdutoMapper produtoMapper,
            List<RegraReservaEstoque> regrasReservaEstoque) {
        this.produtoRepository = produtoRepository;
        this.categoriaRepository = categoriaRepository;
        this.produtoMapper = produtoMapper;
        this.regrasReservaEstoque = regrasReservaEstoque;
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

        List<ProdutoResponse> respostas = new ArrayList<>();
        for (Produto produto : produtos) {
            respostas.add(produtoMapper.paraResponse(produto));
        }
        return respostas;
    }

    @Transactional(readOnly = true)
    public ProdutoResponse buscarPorId(Long id) {
        return produtoMapper.paraResponse(buscarEntidade(id));
    }

    /** Contrato público para outros módulos (ex.: Pedidos) consultarem produto ativo e disponível. */
    @Transactional(readOnly = true)
    public ProdutoResumo buscarProdutoDisponivel(Long produtoId) {
        Produto produto = buscarEntidade(produtoId);
        if (!produto.getCategoria().isAtiva()) {
            throw new ConflitoException(
                    "A categoria do produto '" + produto.getNome() + "' está inativa.");
        }
        if (!produto.isDisponivel()) {
            throw new ConflitoException(
                    "O produto '" + produto.getNome() + "' está indisponível.");
        }
        return produtoMapper.paraResumo(produto);
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

    /** Contrato público para reserva de estoque (chamado ao criar pedido). */
    @Transactional
    public ProdutoResumo reservarEstoque(Long produtoId, Integer quantidade) {
        Produto produto = buscarEntidade(produtoId);
        for (RegraReservaEstoque regra : regrasReservaEstoque) {
            regra.validar(produto, quantidade);
        }
        produto.baixarEstoque(quantidade);
        return produtoMapper.paraResumo(produtoRepository.save(produto));
    }

    /** Contrato público para devolução de estoque (chamado ao cancelar pedido). */
    @Transactional
    public ProdutoResumo devolverEstoque(Long produtoId, Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new DadosInvalidosException("A quantidade para devolução de estoque deve ser maior que zero.");
        }
        Produto produto = buscarEntidade(produtoId);
        produto.reporEstoque(quantidade);
        return produtoMapper.paraResumo(produtoRepository.save(produto));
    }

    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);
        produtoRepository.delete(produto);
    }

    private Produto buscarEntidade(Long id) {
        Optional<Produto> encontrado = produtoRepository.findById(id);
        if (encontrado.isEmpty()) {
            throw new RecursoNaoEncontradoException("Produto não encontrado com id: " + id);
        }
        return encontrado.get();
    }

    private Categoria buscarCategoriaAtiva(Long categoriaId) {
        Optional<Categoria> encontrada = categoriaRepository.findById(categoriaId);
        if (encontrada.isEmpty()) {
            throw new RecursoNaoEncontradoException("Categoria não encontrada com id: " + categoriaId);
        }
        Categoria categoria = encontrada.get();
        if (!categoria.isAtiva()) {
            throw new ConflitoException("Não é possível vincular produto a uma categoria inativa.");
        }
        return categoria;
    }
}