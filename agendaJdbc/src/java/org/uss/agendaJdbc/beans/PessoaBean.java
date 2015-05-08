/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.beans;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Conversation;
import javax.enterprise.context.ConversationScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import org.uss.agendaJdbc.dados.AcessoBancoAgendaJdbc;
import org.uss.agendaJdbc.dados.Pessoa;
import org.uss.agendaJdbc.dados.Telefone;
import org.uss.agendaJdbc.dados.Tipo;

/**
 *
 * @author duo
 */
@Named
@ConversationScoped
public class PessoaBean implements Serializable {
    private @Inject Conversation conversation;
    private Pessoa pessoa;
    private Telefone telefone;
    private AcessoBancoAgendaJdbc acessoBanco;
    enum Estado {RECUPERANDO, CRIANDO, ALTERANDO, REMOVENDO, 
    CRIARTELEFONE, ALTERARTELEFONE};
    private Estado estado = Estado.RECUPERANDO;
    private Estado estadoPrevio;
    private List<Tipo> tipos;

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }

    public Telefone getTelefone() {
        return telefone;
    }

    public void setTelefone(Telefone telefone) {
        this.telefone = telefone;
    }

    public List<Pessoa> getPessoas() {
        if(acessoBanco == null) {
            acessoBanco = new AcessoBancoAgendaJdbc();
        }
        try {
            return acessoBanco.getPessoas();
        } catch (SQLException ex) {
            System.out.println("Erro obtendo lista: " + ex);
            Logger.getLogger(PessoaBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public List<Tipo> getTipos() {
        if (tipos==null) {
            if (acessoBanco == null) {
                acessoBanco = new AcessoBancoAgendaJdbc();
            }
            try {
                tipos = acessoBanco.getTipos();
            } catch (SQLException ex) {
                System.out.println("Erro obtendo lista: " + ex);
                Logger.getLogger(PessoaBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return tipos;
    }

    public String criar() {
        pessoa = new Pessoa();
        if(conversation.isTransient()) {
            conversation.begin();
        }
        estado = Estado.CRIANDO;
        return null;
    }

    public String alterar(Pessoa pessoa) {
        this.pessoa = pessoa;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        estado = Estado.ALTERANDO;
        System.out.println("===>>> Alterando pessoa " + pessoa.getNome());
        return null;
    }

    public String adicionarTelefone() {
        telefone = new Telefone();
        telefone.setPessoa(pessoa);
        estadoPrevio = estado;
        estado = Estado.CRIARTELEFONE;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        return null;
    }
    public String alterarTelefone(Telefone telefoneSelecionado) {
        telefone = telefoneSelecionado;
        estadoPrevio = estado;
        estado = Estado.ALTERARTELEFONE;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        return null;
    }

    public String confirmaAdicionarTelefone() {
        pessoa.getTelefones().add(telefone);
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", 
                        "Telefone adicionado. Alteração só terá efeito após salvar pessoa."));
        //System.out.println("===>>>> Adicionado telefone " + telefone.getNumero() + 
        //        " a pessoa " + pessoa.getNome() +
        //        " voltando para estado " + estadoPrevio);
        return abandonaTelefone();
    }

    public String confirmaAlterarTelefone() {
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", 
                        "Telefone alterado. Alteração só terá efeito após salvar pessoa."));
        return abandonaTelefone();
    }

    public String abandonaTelefone() {
        if(!conversation.isTransient()) {
            conversation.end();
        }
        estado = estadoPrevio;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        return null;
    }

    public String salvar() {
        System.out.println("===>>> salvando Alteração/Criação");
        if(acessoBanco == null) {
            acessoBanco = new AcessoBancoAgendaJdbc();
        }
        try {
            if (isCriando()) {
                System.out.println("===>>> salvando a pessoa " + pessoa.getNome());
                acessoBanco.criarPessoa(pessoa);
            } else if(isAlterando()) {
                System.out.println("===>>> salvando Alteração da pessoa " + pessoa.getNome());
                acessoBanco.alterarPessoa(pessoa);
            }
        } catch (SQLException ex) {
            System.out.println("Erro criando: " + ex);
            Logger.getLogger(PessoaBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return abandonar();
    }

    public String abandonar() {
        System.out.println("===>>> abandonando Alteração/Criação");
        if(!conversation.isTransient()) {
            conversation.end();
        }
        estado = Estado.RECUPERANDO;
        return null;
    }

    public boolean isRecuperando() {
        return estado == Estado.RECUPERANDO;
    }

    public boolean isCriando() {
        return estado == Estado.CRIANDO;
    }

    public boolean isAlterando() {
        return estado == Estado.ALTERANDO;
    }

    public boolean isRemovendo() {
        return estado == Estado.REMOVENDO;
    }

    public boolean isCriandoTelefone() {
        return estado == Estado.CRIARTELEFONE;
    }

    public boolean isAlterandoTelefone() {
        return estado == Estado.ALTERARTELEFONE;
    }
}
