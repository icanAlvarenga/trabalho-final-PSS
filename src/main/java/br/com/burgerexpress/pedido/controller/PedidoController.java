package br.com.burgerexpress.pedido.controller;

import br.com.burgerexpress.pedido.dto.PedidoRequest;
import br.com.burgerexpress.pedido.dto.PedidoResponse;
import br.com.burgerexpress.pedido.service.PedidoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@Valid @RequestBody PedidoRequest request) {
        PedidoResponse pedido = pedidoService.criarPedido(request);
        URI localizacao = URI.create("/api/v1/pedidos/" + pedido.id());
        return ResponseEntity.created(localizacao).body(pedido);
    }

    @GetMapping("/{id}")
    public PedidoResponse buscarPorId(@PathVariable Long id) {
        return pedidoService.buscarPorId(id);
    }

    @GetMapping
    public List<PedidoResponse> listarPorCliente(@RequestParam(required = false) Long clienteId) {
        return pedidoService.listarPorCliente(clienteId);
    }
}
