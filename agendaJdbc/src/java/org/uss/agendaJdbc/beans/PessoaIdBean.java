/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.beans;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Named;
import javax.enterprise.context.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.uss.agendaJdbc.dados.Pessoa;

/**
 *
 * @author duo
 */
@Named
@SessionScoped
public class PessoaIdBean implements Serializable {
    private Pessoa pessoa;

    public StreamedContent getStreamedImage() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) context.getExternalContext().getResponse();
        response.setHeader("Cache-Control", "no-cache"); // Prevents HTTP 1.1 caching.
        response.setHeader("Pragma", "no-cache"); // Prevents HTTP 1.0 caching.
        response.setDateHeader("Expires", -1); // Prevents proxy caching.
        System.out.println("Pessoa a localizar para imagem tem id (parametro): " + pessoa.getId());
        //Pessoa pessoaDaImagem = new AcessoBancoAgendaJdbc().getPessoa(pessoaId);
        System.out.println("Pessoa localizada para atender requisição de renderizar imagem: " + pessoa.getNome());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pessoa.getImagem());
        DefaultStreamedContent defaultStreamedContent
                = new DefaultStreamedContent(byteArrayInputStream);
        // new DefaultStreamedContent(byteArrayInputStream, "image/jpg", pessoa.getId() + "pessoa.jpg");
        try {
            System.out.println("===>>> DefaultStreamedContent: " +  
                    " - tamanho: " + defaultStreamedContent.getStream().available());
            //System.out.println("===>>> DefaultStreamedContent: " + defaultStreamedContent.getContentEncoding()
            //        + ", " + defaultStreamedContent.getContentType() + ", "
            //        + defaultStreamedContent.getName() + " - tamanho: " + defaultStreamedContent.getStream().available());
        } catch (IOException ex) {
            Logger.getLogger(PessoaBean.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("===>>> DefaultStreamedContent: Erro: " + ex);
        }
        return defaultStreamedContent;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public void setPessoa(Pessoa pessoa) {
        this.pessoa = pessoa;
    }
}
