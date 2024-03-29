package br.ufal.ic.p2.wepayu.ModuleEmpregado.Service.TiposEmpregados;

import br.ufal.ic.p2.wepayu.Core.Exceptions.EmpregadonaoEhComissionado;
import br.ufal.ic.p2.wepayu.Core.Exceptions.ValorInvalido;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.Empregado;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.InformacoesBancarias;

import static br.ufal.ic.p2.wepayu.ModuleEmpregado.Service.EmpregadoValidations.validaBanco;
import static br.ufal.ic.p2.wepayu.Sistema.listaEmpregados;

public class ComissionadoService {
    public static void AlteraMetodoPagamentoEmpregado(String id, String atributo, String valor1, String banco, String agencia, String contaCorrente) throws Exception {
        validaBanco(id,atributo,valor1,banco,agencia,contaCorrente);
        Empregado empregadoAtualizado = listaEmpregados.get(id);
        InformacoesBancarias infosBancariasEmpregado = new InformacoesBancarias(valor1,banco,agencia,contaCorrente);
        empregadoAtualizado.SetInformacoesBancarias(infosBancariasEmpregado);
        empregadoAtualizado.setMetodoDePagamento("banco");
        empregadoAtualizado.setisRecebedorPorBanco(true);
        listaEmpregados.put(id,empregadoAtualizado);
    }
    public static void AlteraEmpregadoComissionado(String id, String atributo, String valor) throws Exception {
        Empregado empregado = listaEmpregados.get(id);
        if (empregado.getTipo().equals("comissionado")){
            empregado.setComissao(valor);
            listaEmpregados.put(id,empregado);
        }
        else
            throw new EmpregadonaoEhComissionado("Empregado nao eh comissionado.");
    }

    public static void ComissionaEmpregado(String id, String atributo, String valor,String comissao) throws  Exception{
        Empregado empregado = listaEmpregados.get(id);
        if (valor.equalsIgnoreCase("comissionado")){
            empregado.setTipo("comissionado");
            empregado.setComissao(comissao);
            listaEmpregados.put(id,empregado);
        }
        if (valor.equalsIgnoreCase("horista")){
            empregado.setTipo("horista");
            empregado.setSalario(comissao);
        }
    }

    public static void alteraComissao(Empregado empregado, String valor) throws ValorInvalido {
        if (valor.equalsIgnoreCase("true")) {
            empregado.setComissao(valor);
            empregado.setIsComissionado(true);
        }
    }

    public static String obterComissao(Empregado empregado) throws EmpregadonaoEhComissionado {
        if (empregado.getTipo().equalsIgnoreCase("comissionado")) {
            return empregado.getComissao();
        } else {
            throw new EmpregadonaoEhComissionado("Empregado nao eh comissionado.");
        }
    }

}
