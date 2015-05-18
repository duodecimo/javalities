/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.beans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.uss.agendaJdbc.dados.AcessoBancoAgendaJdbc;
import org.uss.agendaJdbc.dados.Pessoa;

/**
 *
 * @author duo
 */
@Named
@SessionScoped
public class PessoaIdBean implements Serializable {
    private Integer pessoaId;

    public StreamedContent getStreamedImage() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
            // So, we're rendering the HTML. Return a stub StreamedContent so that it will generate right URL.
            String pessoaIdStr = context.getExternalContext().getRequestParameterMap().get("pessoaId");
            System.out.println("Pessoa a localizar para imagem tem id (parametro): " + pessoaId);
            if(pessoaIdStr != null) {
                pessoaId = new Integer(pessoaIdStr);
            }
            return new DefaultStreamedContent();
        } else {
            try {
                if (pessoaId != null) {
                    Pessoa pessoaDaImagem = new AcessoBancoAgendaJdbc().getPessoa(pessoaId);
                    System.out.println("Pessoa localizada para atender requisição de renderizar imagem: " + pessoaDaImagem.getNome());
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pessoaDaImagem.getImagem());
                    DefaultStreamedContent defaultStreamedContent
                            = new DefaultStreamedContent(byteArrayInputStream, "image/jpg", pessoaDaImagem.getId() + "pessoa.jpg");
                    try {
                        System.out.println("===>>> DefaultStreamedContent: " + defaultStreamedContent.getContentEncoding()
                                + ", " + defaultStreamedContent.getContentType() + ", "
                                + defaultStreamedContent.getName() + " - tamanho: " + defaultStreamedContent.getStream().available());
                    } catch (IOException ex) {
                        Logger.getLogger(PessoaBean.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("===>>> DefaultStreamedContent: Erro: " + ex);
                    }
                    return defaultStreamedContent;
                }
            } catch (SQLException ex) {
                Logger.getLogger(PessoaBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new DefaultStreamedContent();
    }
}
