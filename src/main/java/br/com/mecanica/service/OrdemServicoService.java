package br.com.mecanica.service;

import java.util.UUID;
import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.entity.ServicoEntity;
import br.com.mecanica.repository.*;
import br.com.mecanica.exception.RecursoNaoEncontradoException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdemServicoService {
    private final ServicoRepository repository;

    public List<OrdemServico> listar() {
        return repository.findAll(Sort.by("id")).stream().map(this::toResponse).toList();
    }

    public OrdemServico buscar(UUID id) {
        return toResponse(encontrar(id));
    }

    @Transactional
    public OrdemServico criar(OrdemServico ordem) {
        var entity = new ServicoEntity();
        entity.setNome(ordem.getDescricao());
        entity.setValor(BigDecimal.ZERO);
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public OrdemServico atualizar(UUID id, OrdemServico ordem) {
        var entity = encontrar(id);
        if (ordem.getDiagnostico() != null) {
            entity.setNome(ordem.getDiagnostico());
        }
        return toResponse(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(UUID id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private ServicoEntity encontrar(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado"));
    }

    private OrdemServico toResponse(ServicoEntity entity) {
        var ordem = new OrdemServico();
        ordem.setId(entity.getId());
        ordem.setValorTotal(entity.getValor());
        return ordem;
    }
}
