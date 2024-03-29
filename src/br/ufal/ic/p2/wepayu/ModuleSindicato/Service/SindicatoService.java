package br.ufal.ic.p2.wepayu.ModuleSindicato.Service;
import br.ufal.ic.p2.wepayu.Core.Exceptions.EmpregadoNaoEhSindicalizado;
import br.ufal.ic.p2.wepayu.Core.Exceptions.ValorInvalido;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.Empregado;
import br.ufal.ic.p2.wepayu.ModuleSindicato.Classes.MembroSindicato;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import static br.ufal.ic.p2.wepayu.ModuleEmpregado.Service.EmpregadoValidations.validaSindicalizacao;
import static br.ufal.ic.p2.wepayu.ModuleSindicato.Service.SindicatoValidations.*;
import static br.ufal.ic.p2.wepayu.Sistema.*;

public class SindicatoService {

    public static void criaVinculoSindical(String id, String idSindical, String taxaSindical){
        MembroSindicato novoMembro = new MembroSindicato(id, idSindical, taxaSindical);
        listaDeSindicalizados.put(id,novoMembro);
    }
    public static void LancaTaxaServico(String idMembro, String data, String Valor) throws Exception {
        String ValorDaTaxa = Valor.replace(',', '.');
        double valorDaTaxa = Double.parseDouble(ValorDaTaxa);
        lancamentoDeTaxaValidations(idMembro, data, valorDaTaxa);
        for (String id : listaDeSindicalizados.keySet()) {
            MembroSindicato membroSindicato = listaDeSindicalizados.get(id);
            if (membroSindicato.getIdSindical().equals(idMembro)) {
                membroSindicato.cadastrarNovaTaxa(data,valorDaTaxa);
            }
        }
    }
    public static String GetTaxasServico(String idEmpregado, String dataInicial, String dataFinal) throws  Exception {
        getTaxaValidations(idEmpregado,dataInicial,dataFinal);
        MembroSindicato empregado = listaDeSindicalizados.get(idEmpregado);
        double taxasDeServicoTotais = 0.0;
        LocalDate inicio = LocalDate.parse(dataInicial, DateTimeFormatter.ofPattern("d/M/yyyy"));
        LocalDate fim = LocalDate.parse(dataFinal, DateTimeFormatter.ofPattern("d/M/yyyy"));

        for (String data : empregado.DataeTaxa.keySet()) {
            LocalDate dataFormatted = LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));
            if ((dataFormatted.isAfter(inicio) && dataFormatted.isBefore(fim)) || dataFormatted.isEqual(inicio) && !dataFormatted.isEqual(fim)) {
                double taxaDoDia = empregado.DataeTaxa.get(data);
                taxasDeServicoTotais += taxaDoDia;
            }
        }
        DecimalFormat df = new DecimalFormat("#,##0.00");
        return df.format(taxasDeServicoTotais).replace('.', ',');
    }

    public static void alteraSindicalizado(Empregado empregado, String valor) throws ValorInvalido {
        if (valor.equalsIgnoreCase("true")) {
            empregado.SetisSindicalizado(true);
        } else if (valor.equalsIgnoreCase("false")) {
            empregado.SetisSindicalizado(false);
        } else {
            throw new ValorInvalido("Valor inválido");
        }
    }

    public static void AlteraEmpregadoSindicato(String id, String atributo, String valor) throws Exception {
        Empregado empregado = listaEmpregados.get(id);
        if (valor.equalsIgnoreCase("false"))
            empregado.SetisSindicalizado(false);

        if (valor.equalsIgnoreCase("true"))
            empregado.SetisSindicalizado(true);

        if (!empregado.getTipo().equalsIgnoreCase("comissionado"))
            throw new EmpregadoNaoEhSindicalizado("Empregado nao eh sindicalizado.");
    }

    public static void SindicalizaEmpregado(String id, String atributo, String valor, String idSindical, String taxaSindical) throws Exception {
        validaSindicalizacao(id,atributo,valor,idSindical,taxaSindical);
        alteraEmpregadoValidation(idSindical);
        Empregado empregado = listaEmpregados.get(id);
        empregado.SetisSindicalizado(true);
        criaVinculoSindical(id,idSindical,taxaSindical);
        listaEmpregados.put(id,empregado);
    }

    public static String obterIdSindicato(Empregado empregado) throws EmpregadoNaoEhSindicalizado {
        if (!empregado.getIsSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizado("Empregado nao eh sindicalizado.");
        }
        MembroSindicato sindicalista = listaDeSindicalizados.get(empregado.getId());
        return sindicalista.getIdSindical();
    }

    public static String obterTaxaSindical(Empregado empregado) throws EmpregadoNaoEhSindicalizado {
        if (!empregado.getIsSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizado("Empregado nao eh sindicalizado.");
        }
        MembroSindicato sindicalista = listaDeSindicalizados.get(empregado.getId());
        return sindicalista.getTaxaSindical();
    }

}
