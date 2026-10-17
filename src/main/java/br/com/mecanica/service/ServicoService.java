package br.com.mecanica.service;

import br.com.mecanica.dto.ServicoRequest;
import br.com.mecanica.dto.ServicoResponse;
import br.com.mecanica.entity.ServicoEntity;
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
public class ServicoService {
    private final ServicoRepository repository;

    public List<ServicoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(ServicoResponse::from).toList();
    }

    public ServicoResponse buscar(Long id) {
        return ServicoResponse.from(encontrar(id));
    }

    @Transactional
    public ServicoResponse criar(ServicoRequest request) {
        var entity = new ServicoEntity();
        preencher(entity, request);
        return ServicoResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public ServicoResponse atualizar(Long id, ServicoRequest request) {
        var entity = encontrar(id);
        preencher(entity, request);
        return ServicoResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private ServicoEntity encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Servico nao encontrado"));
    }

    private void preencher(ServicoEntity entity, ServicoRequest request) {
        entity.setNome(request.nome());
        entity.setValor(request.valor());
    }
}
