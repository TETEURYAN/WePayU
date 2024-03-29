package br.ufal.ic.p2.wepayu.ModuleEmpregado.Service;
import br.ufal.ic.p2.wepayu.Core.Exceptions.*;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.Service.TiposEmpregados.ComissionadoService;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.Empregado;
import br.ufal.ic.p2.wepayu.ModuleEmpregado.model.InformacoesBancarias;
import br.ufal.ic.p2.wepayu.ModuleFolhaDePagamento.Service.FolhaService;
import br.ufal.ic.p2.wepayu.ModuleSindicato.Classes.MembroSindicato;
import br.ufal.ic.p2.wepayu.ModuleSindicato.Service.SindicatoService;
import br.ufal.ic.p2.wepayu.Utils.Utils;

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
                return Utils.formatarSalario(empregado.getSalario());
            case "sindicalizado":
                return Boolean.toString(empregado.getIsSindicalizado());
            case "comissao":
                return ComissionadoService.obterComissao(empregado);
            case "metodopagamento":
                return empregado.getMetodoDePagamento();
            case "idsindicato":
                return SindicatoService.obterIdSindicato(empregado);
            case "taxasindical":
                return SindicatoService.obterTaxaSindical(empregado);
            default:
                if (atributo.equalsIgnoreCase("banco") || atributo.equalsIgnoreCase("agencia") || atributo.equalsIgnoreCase("contacorrente") || atributo.equalsIgnoreCase("valor")) {
                    return FolhaService.obterInformacaoBancaria(empregado, atributo);
                } else {
                    throw new AtributoInvalido("Atributo nao existe.");
                }
        }
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
                ComissionadoService.alteraComissao(empregadoAtualizado, valor);
                break;
            case "metodopagamento":
                FolhaService.alteraMetodoPagamento(empregadoAtualizado, valor);
                break;
            case "sindicalizado":
                SindicatoService.alteraSindicalizado(empregadoAtualizado, valor);
                break;
            default:
                throw new AtributoInvalido("Atributo não existe.");
        }

        listaEmpregados.put(id, empregadoAtualizado);
    }

    //DELETE
    public void removerEmpregado(String id) throws Exception {
        validaRemocao(id);
        listaEmpregados.remove(id);
    }

}
