package br.com.confisystem.service;

import br.com.confisystem.entity.CompraItem;
import br.com.confisystem.repository.CompraItemRepository;
import br.com.confisystem.repository.OrdemProducaoItemRepository;
import br.com.confisystem.repository.OrdemProducaoRepository;
import br.com.confisystem.repository.TratoRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
public class EstoqueService {

    private final CompraItemRepository compraItemRepository;
    private final OrdemProducaoItemRepository ordemProducaoItemRepository;
    private final OrdemProducaoRepository ordemProducaoRepository;
    private final TratoRepository tratoRepository;

    public EstoqueService(
            CompraItemRepository compraItemRepository,
            OrdemProducaoItemRepository ordemProducaoItemRepository,
            OrdemProducaoRepository ordemProducaoRepository,
            TratoRepository tratoRepository
    ) {
        this.compraItemRepository = compraItemRepository;
        this.ordemProducaoItemRepository = ordemProducaoItemRepository;
        this.ordemProducaoRepository = ordemProducaoRepository;
        this.tratoRepository = tratoRepository;
    }

    public BigDecimal custoAtualKg(Integer materiaPrimaId) {

        Optional<CompraItem> ultimaCompra =
                compraItemRepository.findTopByMateriaPrimaIdOrderByCompraDataDesc(materiaPrimaId);

        return ultimaCompra.map(CompraItem::getCustoKg).orElse(null);
    }

    public BigDecimal estoqueMateriaPrimaKg(Integer materiaPrimaId) {

        BigDecimal comprado = compraItemRepository.somarQuantidadeCompradaPorMateriaPrima(materiaPrimaId);
        BigDecimal consumidoEmOrdens = ordemProducaoItemRepository.somarQuantidadeConsumidaPorMateriaPrima(materiaPrimaId);

        return comprado.subtract(consumidoEmOrdens);
    }

    public BigDecimal estoqueRacaoProntaKg(Integer formulaId) {

        BigDecimal produzido = ordemProducaoRepository.somarQuantidadeFinalizadaPorFormula(formulaId);
        BigDecimal usadoEmTratos = tratoRepository.somarQuantidadeUsadaPorFormula(formulaId);

        return produzido.subtract(usadoEmTratos);
    }

    public BigDecimal custoMedioRacaoKg(Integer formulaId) {

        BigDecimal produzido = ordemProducaoRepository.somarQuantidadeFinalizadaPorFormula(formulaId);

        if (produzido.signum() <= 0) {
            return null;
        }

        BigDecimal custo = ordemProducaoRepository.somarCustoFinalizadoPorFormula(formulaId);

        return custo.divide(produzido, 4, java.math.RoundingMode.HALF_UP);
    }

}
