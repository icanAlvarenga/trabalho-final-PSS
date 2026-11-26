package br.com.burgerexpress.catalogo.service;

import br.com.burgerexpress.catalogo.dto.CategoriaRequest;
import br.com.burgerexpress.catalogo.dto.CategoriaResponse;
import br.com.burgerexpress.catalogo.entity.Categoria;
import br.com.burgerexpress.catalogo.service.CategoriaMapper;
import br.com.burgerexpress.catalogo.repository.CategoriaRepository;
import br.com.burgerexpress.shared.exception.ConflitoException;
import br.com.burgerexpress.shared.exception.RecursoNaoEncontradoException;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    public CategoriaService(CategoriaRepository categoriaRepository, CategoriaMapper categoriaMapper) {
        this.categoriaRepository = categoriaRepository;
        this.categoriaMapper = categoriaMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarTodas() {
        return categoriaRepository.findAll().stream()
                .map(categoriaMapper::paraResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoriaResponse buscarPorId(Long id) {
        return categoriaMapper.paraResponse(buscarEntidade(id));
    }

    @Transactional
    public CategoriaResponse criar(CategoriaRequest request) {
        String nome = request.nome().trim();
        if (categoriaRepository.existsByNomeIgnoreCase(nome)) {
            throw new ConflitoException("Já existe uma categoria cadastrada com o nome: " + nome);
        }

        Categoria salva = categoriaRepository.save(categoriaMapper.paraEntidade(request));
        return categoriaMapper.paraResponse(salva);
    }

    @Transactional
    public CategoriaResponse atualizar(Long id, CategoriaRequest request) {
        Categoria categoria = buscarEntidade(id);
        String nome = request.nome().trim();

        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(nome, id)) {
            throw new ConflitoException("Já existe outra categoria cadastrada com o nome: " + nome);
        }

        categoria.setNome(nome);
        categoria.setDescricao(request.descricao() != null ? request.descricao().trim() : null);
        if (request.ativa() != null) {
            categoria.setAtiva(request.ativa());
        }

        return categoriaMapper.paraResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void desativar(Long id) {
        Categoria categoria = buscarEntidade(id);
        categoria.setAtiva(false);
        categoriaRepository.save(categoria);
    }

    private Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria não encontrada com id: " + id));
    }
}