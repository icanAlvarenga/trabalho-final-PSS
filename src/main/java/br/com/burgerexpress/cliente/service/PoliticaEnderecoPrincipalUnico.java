package br.com.burgerexpress.cliente.service;

import br.com.burgerexpress.cliente.entity.Endereco;
import br.com.burgerexpress.cliente.repository.EnderecoRepository;
import java.util.List;
import org.springframework.stereotype.Component;

/** Política padrão: um cliente tem no máximo um endereço principal; o mais recente vence. */
@Component
public class PoliticaEnderecoPrincipalUnico implements PoliticaEnderecoPrincipal {

    private final EnderecoRepository enderecoRepository;

    public PoliticaEnderecoPrincipalUnico(EnderecoRepository enderecoRepository) {
        this.enderecoRepository = enderecoRepository;
    }

    @Override
    public void aplicar(Long clienteId, Endereco novo) {
        if (!novo.isPrincipal()) {
            return;
        }
        List<Endereco> principaisAnteriores = enderecoRepository.findByClienteIdAndPrincipalTrue(clienteId);
        for (Endereco anterior : principaisAnteriores) {
            anterior.setPrincipal(false);
        }
    }
}
