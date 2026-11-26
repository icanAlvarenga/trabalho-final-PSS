package br.com.burgerexpress.cliente.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.entity.Endereco;
import br.com.burgerexpress.cliente.repository.EnderecoRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PoliticaEnderecoPrincipalUnicoTest {

    @Mock
    private EnderecoRepository enderecoRepository;

    private Endereco endereco(boolean principal) {
        Cliente cliente = new Cliente("Maria", "maria@example.com", "27999998888");
        return new Endereco(cliente, "29560000", "Rua A", "10", "Centro", "Guaçuí", null, principal);
    }

    @Test
    void novoEnderecoPrincipalDesmarcaOsPrincipaisAnteriores() {
        Endereco anterior = endereco(true);
        List<Endereco> anteriores = new ArrayList<>();
        anteriores.add(anterior);
        when(enderecoRepository.findByClienteIdAndPrincipalTrue(1L)).thenReturn(anteriores);

        new PoliticaEnderecoPrincipalUnico(enderecoRepository).aplicar(1L, endereco(true));

        assertThat(anterior.isPrincipal()).isFalse();
    }

    @Test
    void novoEnderecoNaoPrincipalNaoMexeEmNenhumEndereco() {
        new PoliticaEnderecoPrincipalUnico(enderecoRepository).aplicar(1L, endereco(false));

        verify(enderecoRepository, never()).findByClienteIdAndPrincipalTrue(anyLong());
    }

    @Test
    void novoEnderecoPrincipalSemAnterioresNaoFalha() {
        when(enderecoRepository.findByClienteIdAndPrincipalTrue(1L)).thenReturn(new ArrayList<Endereco>());

        new PoliticaEnderecoPrincipalUnico(enderecoRepository).aplicar(1L, endereco(true));

        verify(enderecoRepository).findByClienteIdAndPrincipalTrue(1L);
    }
}
