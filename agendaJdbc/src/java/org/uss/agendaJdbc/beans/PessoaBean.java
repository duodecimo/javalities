/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.beans;

import java.io.ByteArrayInputStream;
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
import java.io.IOException;
import javax.faces.event.PhaseId;
import javax.servlet.http.Part;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

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
    enum Estado {RECUPERANDO, CRIANDO, ALTERANDO, 
    VISUALIZANDO, REMOVENDO, 
    CRIANDOTELEFONE, ALTERANDOTELEFONE, REMOVENDOTELEFONE};
    private Estado estado = Estado.RECUPERANDO;
    private Estado estadoPrevio;
    private List<Tipo> tipos;
    private Part uploadedFile;
    private StreamedContent imagemPessoa;
    

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

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
        System.out.println("===>>> arquivo de imagem nome: " + 
                uploadedFile.getSubmittedFileName() +
                " do tipo " + uploadedFile.getContentType() +
                " recebido do jsf para ser armazenado em pessoa.");
        uploadPessoaImagem();
    }

    public StreamedContent getImagemPessoa() {
        return imagemPessoa;
    }

    public void setImagemPessoa(StreamedContent imagemPessoa) {
        this.imagemPessoa = imagemPessoa;
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
        System.out.println("===>>> Alterando pessoa " +pessoa.getNome() + 
                " imagem tamanho: " + pessoa.getImagem().length);
        if(conversation.isTransient()) {
            conversation.begin();
        }
        estado = Estado.ALTERANDO;
        return null;
    }

    public String remover(Pessoa pessoa) {
        this.pessoa = pessoa;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        estado = Estado.REMOVENDO;
        System.out.println("===>>> Removendo pessoa " + pessoa.getNome());
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_WARN, "Aviso: ", 
                        "Após confirmar a remoção da pessoa, esta, junto com "
                                + "todos os seus telefones serão removidos.\n "
                                + "A operação não poderá ser desfeita."));
        return null;
    }

    public String visualizar(Pessoa pessoa) {
        this.pessoa = pessoa;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        estado = Estado.VISUALIZANDO;
        System.out.println("===>>> Visualizar pessoa " + pessoa.getNome());
        return null;
    }

    public String operar() {
        System.out.println("===>>> salvando Alteração/Criação/Remoção");
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
            } else if(isRemovendo()) {
                System.out.println("===>>> remover pessoa " + pessoa.getNome());
                acessoBanco.removerPessoa(pessoa);
            }
        } catch (SQLException ex) {
            System.out.println("Erro em operação de pessoa no banco de dados: " + ex);
            Logger.getLogger(PessoaBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        return abandonar();
    }

    public String abandonar() {
        System.out.println("===>>> abandonando Alteração/Criação/Remoção/Visualização");
        if(!conversation.isTransient()) {
            conversation.end();
        }
        estado = Estado.RECUPERANDO;
        return null;
    }

    public String adicionarTelefone() {
        telefone = new Telefone();
        telefone.setPessoa(pessoa);
        estadoPrevio = estado;
        estado = Estado.CRIANDOTELEFONE;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        return null;
    }

    public String alterarTelefone(Telefone telefoneSelecionado) {
        telefone = telefoneSelecionado;
        estadoPrevio = estado;
        estado = Estado.ALTERANDOTELEFONE;
        if(conversation.isTransient()) {
            conversation.begin();
        }
        return null;
    }

    public String removerTelefone(Telefone telefoneSelecionado) {
        telefone = telefoneSelecionado;
        estadoPrevio = estado;
        estado = Estado.REMOVENDOTELEFONE;
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

    public String confirmaRemoverTelefone() {
        pessoa.getTelefones().remove(telefone);
        FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Aviso: ", 
                        "Telefone removido. Alteração só terá efeito após salvar pessoa."));
        //System.out.println("===>>>> Adicionado telefone " + telefone.getNumero() + 
        //        " a pessoa " + pessoa.getNome() +
        //        " voltando para estado " + estadoPrevio);
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

    public boolean isVisualizando() {
        return estado == Estado.VISUALIZANDO;
    }

    public boolean isCriandoTelefone() {
        return estado == Estado.CRIANDOTELEFONE;
    }

    public boolean isAlterandoTelefone() {
        return estado == Estado.ALTERANDOTELEFONE;
    }

    public boolean isRemovendoTelefone() {
        return estado == Estado.REMOVENDOTELEFONE;
    }

    public void uploadPessoaImagem() {

        if (null != uploadedFile) {
            try {
                int avail = uploadedFile.getInputStream().available();
                byte[] bytesFromFile = new byte[avail];
                uploadedFile.getInputStream().read(bytesFromFile, 0, avail);
                pessoa.setImagem(bytesFromFile);
            } catch (IOException ex) {
                System.out.println("===>>> problema adicionando arquivo " + uploadedFile.getSubmittedFileName()
                        + " do tipo " + uploadedFile.getContentType()
                        + " da imagem a pessoa: " + ex.getMessage());
            }
        }
    }
}
