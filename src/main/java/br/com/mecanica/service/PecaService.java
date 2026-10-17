package br.com.mecanica.service;

import br.com.mecanica.dto.PecaRequest;
import br.com.mecanica.dto.PecaResponse;
import br.com.mecanica.entity.PecaEntity;
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
public class PecaService {
    private final PecaRepository repository;

    public List<PecaResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(PecaResponse::from).toList();
    }

    public PecaResponse buscar(Long id) {
        return PecaResponse.from(encontrar(id));
    }

    @Transactional
    public PecaResponse criar(PecaRequest request) {
        var entity = new PecaEntity();
        preencher(entity, request);
        return PecaResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public PecaResponse atualizar(Long id, PecaRequest request) {
        var entity = encontrar(id);
        preencher(entity, request);
        return PecaResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private PecaEntity encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Peca nao encontrado"));
    }

    private void preencher(PecaEntity entity, PecaRequest request) {
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setValor(request.valor());
        entity.setQuantidadeEstoque(request.quantidadeEstoque());
        entity.setFabricante(request.fabricante());
    }
}
