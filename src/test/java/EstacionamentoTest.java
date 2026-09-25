import br.ufc.quixada.poo.*;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Estacionamento (2 vagas de carro, 3 vagas de moto/bike)")
class EstacionamentoTest {

  Estacionamento estacionamento;

  @BeforeEach
  void setUp() {
    estacionamento = new Estacionamento(2, 3);
  }

  /** Registra a saída do veículo após a quantidade de minutos informada e devolve o ticket pago. */
  private Ticket sairApos(String placa, long minutos) {
    Ticket ticket = estacionamento.getTicketBy(placa);
    assertNotNull(ticket, "getTicketBy(\"" + placa + "\") deveria retornar o ticket do veículo estacionado");
    assertTrue(estacionamento.registrarSaida(placa, ticket.getHoraEntrada().plusMinutes(minutos)),
        "Deveria ter permitido a saída de " + placa);
    return ticket;
  }

  // ---------------------------------------------------------------- Vagas

  @Test
  @DisplayName("Um estacionamento novo deve ter todas as vagas disponíveis")
  void deveIniciarComTodasAsVagasDisponiveis() {
    assertEquals(2, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "Vagas de carro disponíveis");
    assertEquals(3, estacionamento.vagasDisponiveisPara(TipoVaga.MOTO_E_BIKE), "Vagas de moto/bike disponíveis");
  }

  @Test
  @DisplayName("Um tipo de vaga com capacidade 0 não deve aceitar veículos")
  void deveRecusarEntradaQuandoCapacidadeZero() {
    Estacionamento semVagasDeCarro = new Estacionamento(0, 3);

    assertEquals(0, semVagasDeCarro.vagasDisponiveisPara(TipoVaga.CARRO), "Não há vagas de carro");
    assertFalse(semVagasDeCarro.registrarEntrada(new Carro("CAR001")), "Não há vagas de carro, o carro não pode entrar");
    assertTrue(semVagasDeCarro.registrarEntrada(new Moto("MOTO001")), "Ainda há vagas de moto/bike disponíveis");
    assertEquals(0, semVagasDeCarro.vagasDisponiveisPara(TipoVaga.CARRO), "A entrada recusada não deve alterar as vagas");
  }

  // ---------------------------------------------------------------- Entrada

  @Test
  @DisplayName("Deve registrar a entrada de um carro ocupando uma vaga de carro")
  void deveRegistrarEntradaDeCarro() {
    Veiculo carro = new Carro("CAR001");
    assertTrue(estacionamento.registrarEntrada(carro), "Deveria ter permitido a entrada do carro");
    assertEquals(1, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "Apenas uma vaga para carro deveria estar disponível");
    assertEquals(3, estacionamento.vagasDisponiveisPara(TipoVaga.MOTO_E_BIKE), "Carro não deveria ocupar vaga de moto/bike");
  }

  @Test
  @DisplayName("Moto e bike devem ocupar vagas de moto/bike")
  void deveRegistrarEntradaDeMotoEBike() {
    Veiculo moto = new Moto("MOTO001");
    Veiculo bike = new Bike("BIKE001");

    assertTrue(estacionamento.registrarEntrada(moto), "Deveria ter permitido a entrada da moto");
    assertTrue(estacionamento.registrarEntrada(bike), "Deveria ter permitido a entrada da bike");
    assertEquals(1, estacionamento.vagasDisponiveisPara(TipoVaga.MOTO_E_BIKE), "Apenas uma vaga para moto e bike deveria estar disponível");
    assertEquals(2, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "Moto e bike não deveriam ocupar vaga de carro");
  }

  @Test
  @DisplayName("Ao entrar, o veículo deve receber um ticket não pago com o horário de entrada")
  void deveGerarTicketNaEntrada() {
    LocalDateTime antes = LocalDateTime.now();
    estacionamento.registrarEntrada(new Carro("CAR001"));
    LocalDateTime depois = LocalDateTime.now();

    Ticket ticket = estacionamento.getTicketBy("CAR001");
    assertNotNull(ticket, "Deveria ter sido gerado um ticket para o veículo");
    assertFalse(ticket.isPago(), "Um ticket recém-gerado não deve estar pago");
    assertNotNull(ticket.getHoraEntrada(), "O ticket deve registrar o horário de entrada");
    assertFalse(ticket.getHoraEntrada().isBefore(antes) || ticket.getHoraEntrada().isAfter(depois),
        "O horário de entrada deve ser o momento em que o veículo entrou");
  }

  @Test
  @DisplayName("Não deve permitir a entrada de um veículo que já está estacionado")
  void naoDevePermitirVeiculosDuplicados() {
    Veiculo carro = new Carro("CAR001");
    assertTrue(estacionamento.registrarEntrada(carro), "Deveria ter permitido a primeira entrada do carro");
    assertFalse(estacionamento.registrarEntrada(carro), "Não deve permitir o registro de veículos duplicados.");
    assertEquals(1, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "A tentativa recusada não deve ocupar outra vaga");
  }

  @Test
  @DisplayName("Não deve permitir a entrada de outro objeto com a mesma placa de um veículo estacionado")
  void naoDevePermitirPlacaDuplicada() {
    assertTrue(estacionamento.registrarEntrada(new Carro("CAR001")), "Deveria ter permitido a primeira entrada do carro");
    assertFalse(estacionamento.registrarEntrada(new Carro("CAR001")),
        "A placa identifica o veículo: dois objetos com a mesma placa são o mesmo veículo");
  }

  @Test
  @DisplayName("Não deve permitir a entrada de carro quando as vagas de carro estão lotadas")
  void naoDeveRegistrarEntradaQuandoEstacionamentoLotado() {
    assertTrue(estacionamento.registrarEntrada(new Carro("CAR001")), "Deveria ter permitido a entrada do carro");
    assertTrue(estacionamento.registrarEntrada(new Carro("CAR002")), "Deveria ter permitido a entrada do carro");
    assertFalse(estacionamento.registrarEntrada(new Carro("CAR003")), "Vagas de carro lotadas, nenhum carro pode entrar");
    assertNull(estacionamento.getTicketBy("CAR003"), "Um veículo recusado não deve receber ticket");
  }

  @Test
  @DisplayName("Não deve permitir a entrada de moto quando as vagas de moto/bike estão lotadas")
  void deveImpedirRegistrarEntradaDeVeiculosQuandoEstacionamentoLotado() {
    assertTrue(estacionamento.registrarEntrada(new Moto("MOTO001")), "Deveria ter permitido a entrada da moto");
    assertTrue(estacionamento.registrarEntrada(new Moto("MOTO002")), "Deveria ter permitido a entrada da moto");
    assertTrue(estacionamento.registrarEntrada(new Moto("MOTO003")), "Deveria ter permitido a entrada da moto");

    assertFalse(estacionamento.registrarEntrada(new Moto("MOTO004")), "Não deve permitir entrada quando todas as vagas estão ocupadas.");
  }

  @Test
  @DisplayName("Motos e bikes devem disputar as mesmas vagas")
  void motoEBikeDevemCompartilharVagas() {
    assertTrue(estacionamento.registrarEntrada(new Moto("MOTO001")), "Deveria ter permitido a entrada da moto");
    assertTrue(estacionamento.registrarEntrada(new Bike("BIKE001")), "Deveria ter permitido a entrada da bike");
    assertTrue(estacionamento.registrarEntrada(new Moto("MOTO002")), "Deveria ter permitido a entrada da moto");

    assertFalse(estacionamento.registrarEntrada(new Bike("BIKE002")), "As vagas de moto/bike estão lotadas, a bike não pode entrar");
  }

  @Test
  @DisplayName("Lotar as vagas de carro não deve impedir a entrada de motos")
  void vagasDeCarroEMotoDevemSerIndependentes() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    estacionamento.registrarEntrada(new Carro("CAR002"));

    assertTrue(estacionamento.registrarEntrada(new Moto("MOTO001")), "Ainda há vagas de moto/bike disponíveis");
  }

  // ---------------------------------------------------------------- Saída

  @Test
  @DisplayName("Ao sair, o ticket deve ser marcado como pago com o valor calculado")
  void deveRegistrarSaidaECalcularValorCorretamente() {
    estacionamento.registrarEntrada(new Carro("CAR001"));

    Ticket ticket = sairApos("CAR001", 5 * 60);

    assertTrue(ticket.isPago(), "O ticket deve ser marcado como pago");
    assertEquals(30.0, ticket.getValorPago(), 0.01, "5 horas (300 min) x R$ 0,10 = R$ 30,00");
  }

  @Test
  @DisplayName("Ao sair, a vaga ocupada pelo veículo deve ser liberada")
  void deveLiberarVagaAposPagamento() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    sairApos("CAR001", 10);
    assertEquals(2, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "A vaga antes ocupada deveria ter sido liberada.");

    estacionamento.registrarEntrada(new Bike("BIKE001"));
    sairApos("BIKE001", 10);
    assertEquals(3, estacionamento.vagasDisponiveisPara(TipoVaga.MOTO_E_BIKE), "A vaga antes ocupada deveria ter sido liberada.");
  }

  @Test
  @DisplayName("Não deve permitir a saída de um veículo que não entrou")
  void naoDevePermitirPagamentoDeTicketInexistente() {
    assertTrue(estacionamento.registrarEntrada(new Carro("CAR001")), "Deveria ter permitido a entrada do carro");

    assertNull(estacionamento.getTicketBy("INVALID001"), "Não existe ticket para um veículo que não entrou");
    assertFalse(estacionamento.registrarSaida("INVALID001", LocalDateTime.now()), "Ticket não encontrado para o identificador: INVALID001");
    assertEquals(1, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "A saída recusada não deve liberar vaga");
  }

  @Test
  @DisplayName("Não deve permitir registrar a saída de um ticket já pago")
  void naoDevePermitirRegistrarSaidaJaPago() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    Ticket ticket = sairApos("CAR001", 60);

    assertFalse(estacionamento.registrarSaida("CAR001", ticket.getHoraEntrada().plusMinutes(120)), "Ticket já foi pago.");
    assertEquals(6.0, ticket.getValorPago(), 0.01, "A segunda tentativa de saída não deve alterar o valor pago");
    assertEquals(2, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "A segunda tentativa de saída não deve liberar vagas a mais");
  }

  @Test
  @DisplayName("Não deve permitir registrar a saída com horário anterior à entrada")
  void naoDevePermitirSaidaAntesDaEntrada() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    Ticket ticket = estacionamento.getTicketBy("CAR001");
    assertNotNull(ticket, "Deveria ter sido gerado um ticket para o veículo");

    assertFalse(estacionamento.registrarSaida("CAR001", ticket.getHoraEntrada().minusMinutes(1)),
        "O horário de saída não pode ser anterior ao horário de entrada");
    assertFalse(ticket.isPago(), "A saída recusada não deve marcar o ticket como pago");
    assertEquals(1, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO), "A saída recusada não deve liberar a vaga");
    assertEquals(1, estacionamento.listarVeiculosEstacionados().length, "O carro continua estacionado");

    assertTrue(estacionamento.registrarSaida("CAR001", ticket.getHoraEntrada().plusMinutes(60)),
        "Com um horário válido, a saída deve ser permitida");
  }

  @Test
  @DisplayName("Um veículo pode voltar após sair, recebendo um novo ticket")
  void devePoderVoltarAposPagamento() {
    Veiculo carro = new Carro("CAR001");
    estacionamento.registrarEntrada(carro);
    Ticket primeiro = sairApos("CAR001", 60);
    assertEquals(2, estacionamento.vagasDisponiveisPara(TipoVaga.CARRO));

    assertTrue(estacionamento.registrarEntrada(carro), "Um veículo pode estacionar várias vezes durante o dia");

    Ticket segundo = estacionamento.getTicketBy("CAR001");
    assertNotSame(primeiro, segundo, "A nova entrada deve gerar um novo ticket");
    assertFalse(segundo.isPago(), "O novo ticket ainda não foi pago");
    assertTrue(estacionamento.registrarSaida("CAR001", segundo.getHoraEntrada().plusMinutes(10)),
        "Deveria permitir a saída do veículo que voltou");
    assertEquals(6.0, primeiro.getValorPago(), 0.01, "O ticket anterior não deve ser alterado");
  }

  // ---------------------------------------------------------------- Cálculo do valor

  @Test
  @DisplayName("Carro: permanência curta paga o mínimo de R$ 5,00")
  void deveCalcularValorDeCarroComMinimoDe5Reais() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    Ticket ticket = sairApos("CAR001", 10);
    assertEquals(5.0, ticket.getValorPago(), 0.001, "10 min x R$ 0,10 = R$ 1,00, abaixo do mínimo de R$ 5,00");
  }

  @Test
  @DisplayName("Carro: acima do mínimo, paga R$ 0,10 por minuto")
  void deveCalcularValorDeCarroPorMinuto() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    Ticket ticket = sairApos("CAR001", 51);
    assertEquals(5.10, ticket.getValorPago(), 0.001, "51 min x R$ 0,10 = R$ 5,10");
  }

  @Test
  @DisplayName("Carro: fração de minuto é cobrada como minuto inteiro")
  void deveCobrarFracaoDeMinutoDeCarroComoMinutoInteiro() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    Ticket ticket = estacionamento.getTicketBy("CAR001");
    assertNotNull(ticket, "Deveria ter sido gerado um ticket para o veículo");

    estacionamento.registrarSaida("CAR001", ticket.getHoraEntrada().plusMinutes(60).plusSeconds(1));
    assertEquals(6.10, ticket.getValorPago(), 0.001, "60 min e 1 s são cobrados como 61 min: 61 x R$ 0,10 = R$ 6,10");
  }

  @Test
  @DisplayName("Moto: 1 minuto de permanência paga o mínimo de R$ 3,00")
  void deveCalcularValorDeMotoComMinimoDe3Reais() {
    estacionamento.registrarEntrada(new Moto("MOTO001"));
    Ticket ticket = sairApos("MOTO001", 1);
    assertEquals(3.0, ticket.getValorPago(), 0.001, "1 min x R$ 0,05 = R$ 0,05, abaixo do mínimo de R$ 3,00");
  }

  @Test
  @DisplayName("Moto: 30 minutos de permanência paga o mínimo de R$ 3,00")
  void deveCalcularValorParaMotosComPermanenciaCurta() {
    estacionamento.registrarEntrada(new Moto("MOTO001"));
    Ticket ticket = sairApos("MOTO001", 30);
    assertEquals(3.0, ticket.getValorPago(), 0.01, "30 min x R$ 0,05 = R$ 1,50, abaixo do mínimo de R$ 3,00");
  }

  @Test
  @DisplayName("Moto: acima do mínimo, paga R$ 0,05 por minuto")
  void deveCalcularValorDeMotoPorMinuto() {
    estacionamento.registrarEntrada(new Moto("MOTO001"));
    Ticket ticket = sairApos("MOTO001", 2 * 60);
    assertEquals(6.0, ticket.getValorPago(), 0.001, "120 min x R$ 0,05 = R$ 6,00");
  }

  @Test
  @DisplayName("Moto: fração de minuto é cobrada como minuto inteiro")
  void deveCobrarFracaoDeMinutoDeMotoComoMinutoInteiro() {
    estacionamento.registrarEntrada(new Moto("MOTO001"));
    Ticket ticket = estacionamento.getTicketBy("MOTO001");
    assertNotNull(ticket, "Deveria ter sido gerado um ticket para o veículo");

    estacionamento.registrarSaida("MOTO001", ticket.getHoraEntrada().plusMinutes(120).plusSeconds(30));
    assertEquals(6.05, ticket.getValorPago(), 0.001, "120 min e 30 s são cobrados como 121 min: 121 x R$ 0,05 = R$ 6,05");
  }

  @Test
  @DisplayName("Bike: paga R$ 3,00 fixo em uma permanência curta")
  void deveCalcularValorParaBikesComPermanenciaCurta() {
    estacionamento.registrarEntrada(new Bike("BIKE001"));
    Ticket ticket = sairApos("BIKE001", 5);
    assertEquals(3.0, ticket.getValorPago(), 0.01, "Bike paga valor fixo de R$ 3,00");
  }

  @Test
  @DisplayName("Bike: paga R$ 3,00 fixo em uma permanência longa")
  void deveCalcularValorParaBikesComPermanenciaLonga() {
    estacionamento.registrarEntrada(new Bike("BIKE001"));
    Ticket ticket = sairApos("BIKE001", 120);
    assertEquals(3.0, ticket.getValorPago(), 0.01, "Bike paga valor fixo de R$ 3,00");
  }

  // ---------------------------------------------------------------- Listagem

  @Test
  @DisplayName("Deve listar os veículos estacionados na ordem de entrada")
  void deveListarVeiculosEstacionados() {
    Veiculo carro = new Carro("CAR001");
    Veiculo moto = new Moto("MOTO001");
    Veiculo bike = new Bike("BIKE001");
    Veiculo[] veiculos = {carro, moto, bike};
    for (Veiculo veiculo : veiculos) {
      estacionamento.registrarEntrada(veiculo);
    }

    Veiculo[] estacionados = estacionamento.listarVeiculosEstacionados();
    assertNotNull(estacionados, "A lista de veículos estacionados não pode ser nula");
    assertEquals(veiculos.length, estacionados.length, "Quantidade de veículos estacionados");
    for (int i = 0; i < veiculos.length; i++) {
      assertEquals(veiculos[i].getIdentificador(), estacionados[i].getIdentificador(), "Veículo na posição " + i);
    }
  }

  @Test
  @DisplayName("Um estacionamento vazio deve retornar uma lista vazia (e não null)")
  void deveListarNenhumVeiculoQuandoVazio() {
    Veiculo[] estacionados = estacionamento.listarVeiculosEstacionados();
    assertNotNull(estacionados, "A lista de veículos estacionados não pode ser nula");
    assertEquals(0, estacionados.length, "Nenhum veículo deveria estar estacionado");
  }

  @Test
  @DisplayName("Veículos que já saíram não devem aparecer na lista")
  void naoDeveListarVeiculosQueSairam() {
    estacionamento.registrarEntrada(new Carro("CAR001"));
    estacionamento.registrarEntrada(new Moto("MOTO001"));
    estacionamento.registrarEntrada(new Bike("BIKE001"));
    sairApos("MOTO001", 30);

    Veiculo[] estacionados = estacionamento.listarVeiculosEstacionados();
    assertEquals(2, estacionados.length, "Apenas 2 veículos continuam estacionados");
    assertEquals("CAR001", estacionados[0].getIdentificador());
    assertEquals("BIKE001", estacionados[1].getIdentificador());
  }
}
