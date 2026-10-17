package br.com.mecanica.service;

import br.com.mecanica.dto.InsumoRequest;
import br.com.mecanica.dto.InsumoResponse;
import br.com.mecanica.entity.InsumoEntity;
import br.com.mecanica.repository.*;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsumoService {
    private final InsumoRepository repository;

    public List<InsumoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(InsumoResponse::from).toList();
    }

    public InsumoResponse buscar(Long id) {
        return InsumoResponse.from(encontrar(id));
    }

    @Transactional
    public InsumoResponse criar(InsumoRequest request) {
        var entity = new InsumoEntity();
        preencher(entity, request);
        return InsumoResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public InsumoResponse atualizar(Long id, InsumoRequest request) {
        var entity = encontrar(id);
        preencher(entity, request);
        return InsumoResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private InsumoEntity encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Insumo nao encontrado"));
    }

    private void preencher(InsumoEntity entity, InsumoRequest request) {
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setValor(request.valor());
        entity.setQuantidadeEstoque(request.quantidadeEstoque());
        entity.setUnidadeMedida(request.unidadeMedida());
    }
}
