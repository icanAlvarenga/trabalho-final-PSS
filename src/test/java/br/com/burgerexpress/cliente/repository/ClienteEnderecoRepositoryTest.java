package br.com.burgerexpress.cliente.repository;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.entity.Endereco;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

/** Persistência via repositories (padrão Repository), contra H2. */
@DataJpaTest
class ClienteEnderecoRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Test
    void persisteEConsultaClientePorEmailIgnorandoCaixa() {
        clienteRepository.saveAndFlush(new Cliente("Maria", "maria@example.com", "27999998888"));

        assertThat(clienteRepository.existsByEmailIgnoreCase("MARIA@EXAMPLE.COM")).isTrue();
        assertThat(clienteRepository.existsByEmailIgnoreCase("outro@example.com")).isFalse();
    }

    @Test
    void existsPorEmailIgnorandoOProprioCliente() {
        Cliente salvo = clienteRepository.saveAndFlush(new Cliente("Maria", "maria@example.com", "27999998888"));

        assertThat(clienteRepository.existsByEmailIgnoreCaseAndIdNot("maria@example.com", salvo.getId())).isFalse();
        assertThat(clienteRepository.existsByEmailIgnoreCaseAndIdNot("maria@example.com", salvo.getId() + 1)).isTrue();
    }

    @Test
    void consultaEnderecosSomenteDoClienteIndicado() {
        Cliente maria = clienteRepository.saveAndFlush(new Cliente("Maria", "maria@example.com", "27999998888"));
        Cliente joao = clienteRepository.saveAndFlush(new Cliente("João", "joao@example.com", "27988887777"));
        Endereco deMaria = enderecoRepository.saveAndFlush(
                new Endereco(maria, "29560000", "Rua A", "10", "Centro", "Guaçuí", null, true));
        enderecoRepository.saveAndFlush(
                new Endereco(joao, "29560000", "Rua B", "20", "Centro", "Guaçuí", null, false));

        assertThat(enderecoRepository.findByClienteIdOrderByIdAsc(maria.getId())).hasSize(1);
        assertThat(enderecoRepository.findByIdAndClienteId(deMaria.getId(), maria.getId())).isPresent();
        assertThat(enderecoRepository.findByIdAndClienteId(deMaria.getId(), joao.getId())).isEmpty();
        assertThat(enderecoRepository.findByClienteIdAndPrincipalTrue(maria.getId())).hasSize(1);
    }
}
