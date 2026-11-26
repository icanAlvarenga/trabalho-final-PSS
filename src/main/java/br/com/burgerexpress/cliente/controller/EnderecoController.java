package br.com.burgerexpress.cliente.controller;

import br.com.burgerexpress.cliente.dto.EnderecoRequest;
import br.com.burgerexpress.cliente.dto.EnderecoResponse;
import br.com.burgerexpress.cliente.service.EnderecoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Somente protocolo HTTP: delega todas as regras ao EnderecoService. */
@RestController
@RequestMapping("/api/v1/clientes/{clienteId}/enderecos")
public class EnderecoController {

    private final EnderecoService enderecoService;

    public EnderecoController(EnderecoService enderecoService) {
        this.enderecoService = enderecoService;
    }

    @PostMapping
    public ResponseEntity<EnderecoResponse> criar(@PathVariable Long clienteId,
                                                  @Valid @RequestBody EnderecoRequest request) {
        EnderecoResponse criado = enderecoService.criar(clienteId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/clientes/" + clienteId + "/enderecos/" + criado.id()))
                .body(criado);
    }

    @GetMapping
    public List<EnderecoResponse> listar(@PathVariable Long clienteId) {
        return enderecoService.listar(clienteId);
    }
}
