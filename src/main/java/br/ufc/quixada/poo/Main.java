package br.ufc.quixada.poo;

public class Main {
  public static void main(String[] args) {
    Estacionamento estacionamento = new Estacionamento(2, 3);

    Veiculo carro1 = new Carro("CAR001");
    Veiculo moto1 = new Moto("MOTO001");
    Veiculo bike1 = new Bike("BIKE001");

    // Registrar entradas
    estacionamento.registrarEntrada(carro1);
    estacionamento.registrarEntrada(moto1);
    estacionamento.registrarEntrada(bike1);

    // Listar veículos
    imprimirVeiculosEstacionados(estacionamento);

    // Pagar tickets
    estacionamento.registrarSaida("CAR001", estacionamento.getTicketBy("CAR001").getHoraEntrada().plusMinutes(20));
    estacionamento.registrarSaida("MOTO001", estacionamento.getTicketBy("MOTO001").getHoraEntrada().plusHours(2));

    // Valores pagos
    for (String placa : new String[]{"CAR001", "MOTO001"}) {
      Ticket ticket = estacionamento.getTicketBy(placa);
      System.out.printf("%s pagou R$ %.2f%n", placa, ticket.getValorPago());
    }

    // Listar veículos novamente (apenas a bike continua estacionada)
    imprimirVeiculosEstacionados(estacionamento);
  }

  private static void imprimirVeiculosEstacionados(Estacionamento estacionamento) {
    System.out.println("Veículos estacionados:");
    for (Veiculo veiculo : estacionamento.listarVeiculosEstacionados()) {
      Ticket ticket = estacionamento.getTicketBy(veiculo.getIdentificador());
      System.out.printf("- %s (%s) | entrada: %s%n",
          veiculo.getIdentificador(),
          veiculo.getClass().getSimpleName(),
          ticket.getHoraEntrada());
    }
  }
}
