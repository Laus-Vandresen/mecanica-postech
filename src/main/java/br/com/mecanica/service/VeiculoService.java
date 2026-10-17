package br.com.mecanica.service;

import br.com.mecanica.dto.VeiculoRequest;
import br.com.mecanica.dto.VeiculoResponse;
import br.com.mecanica.entity.VeiculoEntity;
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
public class VeiculoService {
    private final VeiculoRepository repository;
    private final ClienteRepository clienteRepository;

    public List<VeiculoResponse> listar() {
        return repository.findAll(Sort.by("id")).stream().map(VeiculoResponse::from).toList();
    }

    public VeiculoResponse buscar(Long id) {
        return VeiculoResponse.from(encontrar(id));
    }

    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        var entity = new VeiculoEntity();
        preencher(entity, request);
        return VeiculoResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
        var entity = encontrar(id);
        preencher(entity, request);
        return VeiculoResponse.from(repository.saveAndFlush(entity));
    }

    @Transactional
    public void excluir(Long id) {
        repository.delete(encontrar(id));
        repository.flush();
    }

    private VeiculoEntity encontrar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Veiculo nao encontrado"));
    }

    private void preencher(VeiculoEntity entity, VeiculoRequest request) {
        entity.setPlaca(request.placa());
        entity.setModelo(request.modelo());
        entity.setMarca(request.marca());
        entity.setAno(request.ano());
        entity.setCliente(clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado")));
    }
}
