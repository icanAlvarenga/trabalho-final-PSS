package br.com.burgerexpress.pedido.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "pedidos", indexes = @Index(name = "idx_pedidos_cliente", columnList = "cliente_id"))
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cliente_id", nullable = false, updatable = false)
    private Long clienteId;

    @Column(nullable = false, updatable = false)
    private Long enderecoId;

    @Column(nullable = false, updatable = false)
    private Instant dataCriacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusPedido status;

    @Lob
    private String observacoes;

    @Column(nullable = false, precision = 30, scale = 2)
    private BigDecimal total;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.PERSIST)
    @OrderBy("id ASC")
    private List<ItemPedido> itens = new ArrayList<>();

    protected Pedido() {
    }

    public Pedido(Long clienteId, Long enderecoId, String observacoes) {
        this.clienteId = clienteId;
        this.enderecoId = enderecoId;
        this.observacoes = observacoes;
        this.dataCriacao = Instant.now();
        this.status = StatusPedido.AGUARDANDO_PAGAMENTO;
        this.total = BigDecimal.ZERO.setScale(2);
    }

    public void adicionarItem(Long produtoId, int quantidade, BigDecimal precoUnitario) {
        ItemPedido item = new ItemPedido(this, produtoId, quantidade, precoUnitario);
        itens.add(item);
        total = total.add(item.getSubtotal());
    }

    public Long getId() {
        return id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public Long getEnderecoId() {
        return enderecoId;
    }

    public Instant getDataCriacao() {
        return dataCriacao;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public List<ItemPedido> getItens() {
        return Collections.unmodifiableList(itens);
    }
}
