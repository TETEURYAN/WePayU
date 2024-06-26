package br.ufal.ic.p2.wepayu.ModuleFolhaDePagamento.Service;

import br.ufal.ic.p2.wepayu.Core.Exceptions.MetodoDePagamentoInvalido;
import br.ufal.ic.p2.wepayu.ModuleCartaoDePonto.Classes.CartaoDePonto;
import br.ufal.ic.p2.wepayu.ModuleCartaoDePonto.Classes.Horas;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.Empregado;
import br.ufal.ic.p2.wepayu.ModuleVendas.Model.Vendas;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static br.ufal.ic.p2.wepayu.Sistema.*;


public class FolhaService {
    public static double totalHoristas(String data) {
        double valorTotal = 0.0;
        double salarioHora = 0.0;
        String salarioString = "";
        for (Map.Entry<String, CartaoDePonto> entry : listaDeCartoes.entrySet()) {
            String id = entry.getKey();
            CartaoDePonto cartao = entry.getValue();
            if (listaEmpregados.get(id).getTipo().equalsIgnoreCase("horista"))
                salarioString = listaEmpregados.get(id).getSalario();
                salarioString = salarioString.replace(",",".");
                salarioHora = Double.parseDouble(salarioString);
            for (Map.Entry<String, Horas> horasEntry : cartao.DataeHoras.entrySet()) {
                String chave = horasEntry.getKey();
                if (chave.equals(data)) {
                    Horas horas = horasEntry.getValue();
                    double horasNormais = horas.getHorasNormais();
                    double horasExcedentes = horas.getHorasExcedentes();
                    valorTotal += horasNormais * salarioHora;
                    valorTotal += horasExcedentes * (salarioHora*1.5);
                }
            }
        }
        return valorTotal;
    }
    public static double totalVendedores(String data){
        //So devolve algum valor caso a data seja o ultimo dia do mes
        // pois os empregados comissionados recebem apenas no ultimo dia do mes
        DateTimeFormatter formatter;
        if (data.split("/").length == 2) {
            formatter = DateTimeFormatter.ofPattern("dd/M/yyyy");
        } else {
            formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        }

        LocalDate localDate = LocalDate.parse(data, formatter);

        int dia = localDate.getDayOfMonth();
        int ultimoDiaDoMes = localDate.lengthOfMonth();
        if (!(dia == ultimoDiaDoMes))
            return 0.0;

        double valorTotal = 0.0;
        String salarioDoEmpregadoString;
        double salarioDoEmpregado = 0.0;

        for (Map.Entry<String, Vendas> entry : listaDeVendedores.entrySet()) {
            String id = entry.getKey();
            Vendas venda = entry.getValue();

            //caso o empregado seja apenas assalariado.
            if (listaEmpregados.get(id).getTipo().equalsIgnoreCase("assalariado")){
                salarioDoEmpregadoString = listaEmpregados.get(id).getSalario();
                salarioDoEmpregadoString.replace(",",".");
                salarioDoEmpregado = Double.parseDouble(salarioDoEmpregadoString);
                valorTotal += salarioDoEmpregado;
            }
            //caso o empregado seja comissionado, sao pagos
            //if(){
            //}

        }
        return valorTotal;
    }

    public static String geraBalancoTotal(String data) {
        double valorTotal = 0.0;
        valorTotal += totalHoristas(data);
        valorTotal += totalVendedores(data);
        DecimalFormat formato = new DecimalFormat("#,##0,00");
        return formato.format(valorTotal);
    }

    public static void alteraMetodoPagamento(Empregado empregado, String valor) throws MetodoDePagamentoInvalido {
        if (!valor.equalsIgnoreCase("correios") && !valor.equalsIgnoreCase("emMaos") && !valor.equalsIgnoreCase("banco")) {
            throw new MetodoDePagamentoInvalido("Metodo de pagamento invalido.");
        }
        if (!valor.equalsIgnoreCase("banco")) {
            empregado.setisRecebedorPorBanco(false);
        }
        empregado.setMetodoDePagamento(valor);
    }

    public static String obterInformacaoBancaria(Empregado empregado, String atributo) throws MetodoDePagamentoInvalido {
        if (!empregado.isRecebedorPorBanco()) {
            throw new MetodoDePagamentoInvalido("Empregado nao recebe em banco.");
        }
        return empregado.getInformacaoBancaria(atributo);
    }

}