package br.com.mecanica.domain.service;

import br.com.mecanica.domain.entity.OrdemServico;
import br.com.mecanica.domain.valueObject.TempoMedioFinalizacao;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class OrdemServicoDomainService {

    public TempoMedioFinalizacao calcularTempoMedioFinalizacao(List<OrdemServico> ordens) {
        var tempos = ordens.stream()
                .map(OrdemServico::tempoDeFinalizacao)
                .flatMap(Optional::stream)
                .toList();
        if (tempos.isEmpty()) {
            return new TempoMedioFinalizacao(0, Duration.ZERO);
        }
        var total = tempos.stream().reduce(Duration.ZERO, Duration::plus);
        return new TempoMedioFinalizacao(tempos.size(), total.dividedBy(tempos.size()));
    }
}
