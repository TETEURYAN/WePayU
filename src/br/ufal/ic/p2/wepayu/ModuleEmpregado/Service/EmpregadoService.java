package br.ufal.ic.p2.wepayu.ModuleEmpregado.Service;
import br.ufal.ic.p2.wepayu.Core.Exceptions.*;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.Empregado;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.InformacoesBancarias;
import br.ufal.ic.p2.wepayu.ModuleSindicato.Classes.MembroSindicato;
import java.util.UUID;

import static br.ufal.ic.p2.wepayu.ModuleCartaoDePonto.Service.ServiceCartaoDePonto.CriaCartao;
import static br.ufal.ic.p2.wepayu.ModuleEmpregado.Service.EmpregadoValidations.*;
import static br.ufal.ic.p2.wepayu.ModuleSindicato.Service.SindicatoService.criaVinculoSindical;
import static br.ufal.ic.p2.wepayu.ModuleSindicato.Service.SindicatoValidations.alteraEmpregadoValidation;
import static br.ufal.ic.p2.wepayu.Sistema.*;
import static br.ufal.ic.p2.wepayu.ModuleVendas.Service.VendasService.criaCartaoDeVendas;

public class EmpregadoService {

    public String GerarId(){
        String id = UUID.randomUUID().toString();
        return id;
    }
    public static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    //Create
    public String AddEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        if (tipo.equalsIgnoreCase("comissionado"))
            throw new TipoInvalido("Tipo nao aplicavel.");
        EmpregadoValidations.validarEmpregado(nome,endereco, tipo,salario);
        String id = GerarId();
        if (tipo.equalsIgnoreCase("horista"))
            CriaCartao(id);
        Empregado Empregado = new Empregado(id,nome,endereco,tipo,salario);
        listaEmpregados.put(id,Empregado);
        return id;
    }
    public String AddEmpregado(String nome, String endereco, String tipo, String salario, String ValorComissao) throws  Exception {
        if (!tipo.equalsIgnoreCase("comissionado"))
            throw new TipoInvalido("Tipo nao aplicavel.");

        EmpregadoValidations.validarEmpregado(nome,endereco, tipo,salario,ValorComissao);
        String id = GerarId();
        criaCartaoDeVendas(id);
        Empregado Empregado = new Empregado(id,nome,endereco,tipo,salario, ValorComissao);
        listaEmpregados.put(id,Empregado);
        return id;
    }

    //Read
    public String GetEmpregadoPorNome(String nome, int indice) throws Exception {
        int i = 0;
        for (Empregado empregado : listaEmpregados.values()) {
            if (empregado.getNome().equals(nome))
                i++;

            if (indice == i) {
                return empregado.getId();
            }
        }
        throw new NomeInvalido("Nao ha empregado com esse nome.");
    }
    public String GetAtributoEmpregado(String id, String atributo) throws Exception {
        validaGetAtributoEmpregado(id, atributo);
        Empregado empregado = listaEmpregados.get(id);

        if(id.isEmpty() ){
            throw new EmpregadoNaoExiste("Identificacao do empregado nao pode ser nula.");
        }if (empregado == null) {
            throw new EmpregadoNaoExiste("Empregado nao existe.");
        }

        switch (atributo.toLowerCase()) {
            case "nome":
                return empregado.getNome();
            case "endereco":
                return empregado.getEndereco();
            case "tipo":
                return empregado.getTipo();
            case "salario":
                return formatarSalario(empregado.getSalario());
            case "sindicalizado":
                return Boolean.toString(empregado.getIsSindicalizado());
            case "comissao":
                return obterComissao(empregado);
            case "metodopagamento":
                return empregado.getMetodoDePagamento();
            case "idsindicato":
                return obterIdSindicato(empregado);
            case "taxasindical":
                return obterTaxaSindical(empregado);
            default:
                if (atributo.equalsIgnoreCase("banco") || atributo.equalsIgnoreCase("agencia") || atributo.equalsIgnoreCase("contacorrente") || atributo.equalsIgnoreCase("valor")) {
                    return obterInformacaoBancaria(empregado, atributo);
                } else {
                    throw new AtributoInvalido("Atributo nao existe.");
                }
        }
    }

    private String formatarSalario(String salarioStr) {
        if (isNumeric(salarioStr)) {
            double salario = Double.parseDouble(salarioStr);
            if (salario % 1 == 0) { // se for inteiro
                return String.format("%.0f,00", salario);
            } else { //se for decimal
                return String.format("%.2f", salario).replace(".", ",");
            }
        } else {
            return salarioStr; // Retornar a string original se não for um número
        }
    }

    private String obterComissao(Empregado empregado) throws EmpregadonaoEhComissionado {
        if (empregado.getTipo().equalsIgnoreCase("comissionado")) {
            return empregado.getComissao();
        } else {
            throw new EmpregadonaoEhComissionado("Empregado nao eh comissionado.");
        }
    }

    private String obterIdSindicato(Empregado empregado) throws EmpregadoNaoEhSindicalizado {
        if (!empregado.getIsSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizado("Empregado nao eh sindicalizado.");
        }
        MembroSindicato sindicalista = listaDeSindicalizados.get(empregado.getId());
        return sindicalista.getIdSindical();
    }

    private String obterTaxaSindical(Empregado empregado) throws EmpregadoNaoEhSindicalizado {
        if (!empregado.getIsSindicalizado()) {
            throw new EmpregadoNaoEhSindicalizado("Empregado nao eh sindicalizado.");
        }
        MembroSindicato sindicalista = listaDeSindicalizados.get(empregado.getId());
        return sindicalista.getTaxaSindical();
    }

    private String obterInformacaoBancaria(Empregado empregado, String atributo) throws MetodoDePagamentoInvalido {
        if (!empregado.isRecebedorPorBanco()) {
            throw new MetodoDePagamentoInvalido("Empregado nao recebe em banco.");
        }
        return empregado.getInformacaoBancaria(atributo);
    }

    //UPDATE
    public static void AlteraDefaultAtributoEmpregado(String id, String atributo, String valor) throws Exception {
        validaAtributosDefaultUpdate(id, atributo, valor);
        Empregado empregadoAtualizado = listaEmpregados.get(id);
        if (empregadoAtualizado == null) {
            throw new EmpregadoNaoExiste("Empregado não encontrado");
        }

        switch (atributo.toLowerCase()) {
            case "nome":
                empregadoAtualizado.setNome(valor);
                break;
            case "endereco":
                empregadoAtualizado.setEndereco(valor);
                break;
            case "tipo":
                empregadoAtualizado.setTipo(valor);
                break;
            case "salario":
                empregadoAtualizado.setSalario(valor);
                break;
            case "comissao":
                alteraComissao(empregadoAtualizado, valor);
                break;
            case "metodopagamento":
                alteraMetodoPagamento(empregadoAtualizado, valor);
                break;
            case "sindicalizado":
                alteraSindicalizado(empregadoAtualizado, valor);
                break;
            default:
                throw new AtributoInvalido("Atributo não existe.");
        }

        listaEmpregados.put(id, empregadoAtualizado);
    }

    private static void alteraComissao(Empregado empregado, String valor) throws ValorInvalido {
        if (valor.equalsIgnoreCase("true")) {
            empregado.setComissao(valor);
            empregado.setIsComissionado(true);
        }
    }

    private static void alteraMetodoPagamento(Empregado empregado, String valor) throws MetodoDePagamentoInvalido {
        if (!valor.equalsIgnoreCase("correios") && !valor.equalsIgnoreCase("emMaos") && !valor.equalsIgnoreCase("banco")) {
            throw new MetodoDePagamentoInvalido("Metodo de pagamento invalido.");
        }
        if (!valor.equalsIgnoreCase("banco")) {
            empregado.setisRecebedorPorBanco(false);
        }
        empregado.setMetodoDePagamento(valor);
    }

    private static void alteraSindicalizado(Empregado empregado, String valor) throws ValorInvalido {
        if (valor.equalsIgnoreCase("true")) {
            empregado.SetisSindicalizado(true);
        } else if (valor.equalsIgnoreCase("false")) {
            empregado.SetisSindicalizado(false);
        } else {
            throw new ValorInvalido("Valor inválido");
        }
    }
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
    public static void AlteraEmpregadoSindicato(String id, String atributo, String valor) throws Exception {
        Empregado empregado = listaEmpregados.get(id);
        if (valor.equalsIgnoreCase("false"))
            empregado.SetisSindicalizado(false);

        if (valor.equalsIgnoreCase("true"))
            empregado.SetisSindicalizado(true);

        if (!empregado.getTipo().equalsIgnoreCase("comissionado"))
            throw new EmpregadoNaoEhSindicalizado("Empregado nao eh sindicalizado.");
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

    public static void SindicalizaEmpregado(String id, String atributo, String valor, String idSindical, String taxaSindical) throws Exception {
        validaSindicalizacao(id,atributo,valor,idSindical,taxaSindical);
        alteraEmpregadoValidation(idSindical);
        Empregado empregado = listaEmpregados.get(id);
        empregado.SetisSindicalizado(true);
        criaVinculoSindical(id,idSindical,taxaSindical);
        listaEmpregados.put(id,empregado);
    }


    //DELETE
    public void removerEmpregado(String id) throws Exception {
        validaRemocao(id);
        listaEmpregados.remove(id);
    }

}
