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
import javax.inject.Inject;
import javax.inject.Named;
import org.uss.agendaJdbc.dados.AcessoBancoAgendaJdbc;
import org.uss.agendaJdbc.dados.Tipo;

/**
 *
 * @author duo
 */
@Named
@ConversationScoped
public class TipoBean implements Serializable {
    private @Inject Conversation conversation;
    private Tipo tipo;
    private AcessoBancoAgendaJdbc acessoBanco;
    enum Estado {RECUPERANDO, CRIANDO, ALTERANDO, REMOVENDO};
    private Estado estado = Estado.RECUPERANDO;

    public Tipo getTipo() {
        return tipo;
    }

    public void setTipo(Tipo tipo) {
        this.tipo = tipo;
    }
    

    public List<Tipo> getTipos() {
        if(acessoBanco == null) {
            acessoBanco = new AcessoBancoAgendaJdbc();
        }
        try {
            return acessoBanco.getTipos();
        } catch (SQLException ex) {
            System.out.println("Erro obtendo lista: " + ex);
            Logger.getLogger(TipoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public String criar() {
        tipo = new Tipo();
        if(conversation.isTransient()) {
            conversation.begin();
        }
        estado = Estado.CRIANDO;
        return null;
    }

    public String alterar(Tipo tipo) {
        this.tipo = tipo;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        estado = Estado.ALTERANDO;
        return null;
    }

    public String salvar() {
        if(acessoBanco == null) {
            acessoBanco = new AcessoBancoAgendaJdbc();
        }
        try {
            if (isCriando()) {
                acessoBanco.criarTipo(tipo);
            } else if(isAlterando()) {
                acessoBanco.alterarTipo(tipo);
            }
        } catch (SQLException ex) {
            System.out.println("Erro criando: " + ex);
            Logger.getLogger(TipoBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return abandonar();
    }

    public String abandonar() {
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
}
