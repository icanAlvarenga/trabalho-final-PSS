package br.com.burgerexpress.cliente.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.burgerexpress.cliente.entity.Cliente;
import br.com.burgerexpress.cliente.repository.ClienteRepository;
import java.util.ArrayList;
import java.util.Optional;

/** Verifica o contrato ClienteConsulta na implementação real (ClienteService). */
class ClienteServiceContractTest extends ClienteConsultaContractTest {

    @Override
    protected ClienteConsulta implementacaoComClienteExistente(Cliente cliente) {
        ClienteRepository repository = mock(ClienteRepository.class);
        when(repository.findById(ID_EXISTENTE)).thenReturn(Optional.of(cliente));
        when(repository.findById(ID_INEXISTENTE)).thenReturn(Optional.empty());
        return new ClienteService(repository, new ClienteMapperImpl(), new ArrayList<RestricaoExclusaoCliente>());
    }
}
