package br.com.burgerexpress.catalogo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.burgerexpress.catalogo.dto.ProdutoResumo;
import br.com.burgerexpress.catalogo.entity.Categoria;
import br.com.burgerexpress.catalogo.entity.Produto;
import br.com.burgerexpress.catalogo.repository.CategoriaRepository;
import br.com.burgerexpress.catalogo.repository.ProdutoRepository;
import br.com.burgerexpress.catalogo.service.regra.RegraCategoriaAtiva;
import br.com.burgerexpress.catalogo.service.regra.RegraEstoqueSuficiente;
import br.com.burgerexpress.catalogo.service.regra.RegraProdutoDisponivel;
import br.com.burgerexpress.catalogo.service.regra.RegraQuantidadePositiva;
import br.com.burgerexpress.catalogo.service.regra.RegraReservaEstoque;
import br.com.burgerexpress.shared.exception.ConflitoException;
import br.com.burgerexpress.shared.exception.DadosInvalidosException;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    private ProdutoService produtoService;

    @BeforeEach
    void configurar() {
        ProdutoMapper mapper = new ProdutoMapperPadrao();
        List<RegraReservaEstoque> regras = Arrays.asList(
                new RegraQuantidadePositiva(),
                new RegraCategoriaAtiva(),
                new RegraProdutoDisponivel(),
                new RegraEstoqueSuficiente());

        produtoService = new ProdutoService(produtoRepository, categoriaRepository, mapper, regras);
    }

    @Test
    void deveReservarEstoqueComSucesso() {
        Categoria categoria = new Categoria("Hamburgueres", "Artesanais");
        Produto produto = new Produto("X-Burger", "Carne e queijo", new BigDecimal("29.90"), 10, categoria);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        ProdutoResumo resumo = produtoService.reservarEstoque(1L, 3);

        assertEquals(7, resumo.estoque());
        assertEquals(7, produto.getEstoque());
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveLancarConflitoQuandoEstoqueForInsuficiente() {
        Categoria categoria = new Categoria("Hamburgueres", "Artesanais");
        Produto produto = new Produto("X-Burger", "Carne e queijo", new BigDecimal("29.90"), 2, categoria);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        try {
            produtoService.reservarEstoque(1L, 5);
            fail("Era esperada ConflitoException para estoque insuficiente.");
        } catch (ConflitoException ex) {
            assertEquals(2, produto.getEstoque());
        }
    }

    @Test
    void deveLancarConflitoQuandoProdutoEstiverIndisponivel() {
        Categoria categoria = new Categoria("Hamburgueres", "Artesanais");
        Produto produto = new Produto("X-Burger", "Carne e queijo", new BigDecimal("29.90"), 10, categoria);
        produto.setDisponivel(false);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        try {
            produtoService.reservarEstoque(1L, 1);
            fail("Era esperada ConflitoException para produto indisponivel.");
        } catch (ConflitoException ex) {
            assertEquals(10, produto.getEstoque());
        }
    }

    @Test
    void deveLancarConflitoQuandoCategoriaEstiverInativa() {
        Categoria categoria = new Categoria("Sazonais", "Fora de epoca");
        categoria.setAtiva(false);
        Produto produto = new Produto("Burger Natalino", "Edicao especial", new BigDecimal("35.00"), 10, categoria);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        try {
            produtoService.buscarProdutoDisponivel(1L);
            fail("Era esperada ConflitoException para categoria inativa.");
        } catch (ConflitoException ex) {
            // comportamento esperado
        }
    }

    @Test
    void deveLancarDadosInvalidosQuandoQuantidadeReservaForZeroOuNegativa() {
        Categoria categoria = new Categoria("Hamburgueres", "Artesanais");
        Produto produto = new Produto("X-Burger", "Carne e queijo", new BigDecimal("29.90"), 10, categoria);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        try {
            produtoService.reservarEstoque(1L, 0);
            fail("Era esperada DadosInvalidosException para quantidade <= 0.");
        } catch (DadosInvalidosException ex) {
            assertEquals(10, produto.getEstoque());
        }
    }

    @Test
    void deveDevolverEstoqueComSucesso() {
        Categoria categoria = new Categoria("Hamburgueres", "Artesanais");
        Produto produto = new Produto("X-Burger", "Carne e queijo", new BigDecimal("29.90"), 4, categoria);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        ProdutoResumo resumo = produtoService.devolverEstoque(1L, 3);

        assertEquals(7, resumo.estoque());
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveLancarNaoEncontradoQuandoProdutoNaoExistir() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        try {
            produtoService.buscarPorId(99L);
            fail("Era esperada RecursoNaoEncontradoException.");
        } catch (RecursoNaoEncontradoException ex) {
            // comportamento esperado
        }
    }
}