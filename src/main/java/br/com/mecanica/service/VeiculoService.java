package br.com.mecanica.service;

import br.com.mecanica.entity.mapper.VeiculoEntityMapper;
import br.com.mecanica.domain.entity.Veiculo;
import br.com.mecanica.entity.ClienteEntity;
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

    public List<Veiculo> listar() {
        return repository.findAll(Sort.by("id")).stream().map(VeiculoEntityMapper::toDomain).toList();
    }

    public Veiculo buscar(Long id) {
        return VeiculoEntityMapper.toDomain(encontrar(id));
    }

    @Transactional
    public Veiculo criar(Veiculo veiculo) {
        var cliente = encontrarCliente(veiculo.getClienteId());
        var entity = VeiculoEntityMapper.toEntity(veiculo);
        entity.setCliente(cliente);
        return VeiculoEntityMapper.toDomain(repository.saveAndFlush(entity));
    }

    @Transactional
    public Veiculo atualizar(Long id, Veiculo veiculo) {
        var entity = encontrar(id);
        VeiculoEntityMapper.updateEntity(veiculo, entity);
        entity.setCliente(encontrarCliente(veiculo.getClienteId()));
        return VeiculoEntityMapper.toDomain(repository.saveAndFlush(entity));
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

    private ClienteEntity encontrarCliente(Long clienteId) {
        return clienteRepository.findById(clienteId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cliente nao encontrado"));
    }
}
