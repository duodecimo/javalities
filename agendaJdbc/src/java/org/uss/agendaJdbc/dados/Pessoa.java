/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.dados;

import java.io.Serializable;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author duo
 */
public class Pessoa implements Serializable {
    private Integer id;
    private String nome;
    private String email;
    private Double pontos;
    private Date validade;
    private byte[] imagem;
    private List<Telefone> telefones;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getPontos() {
        return pontos;
    }

    public void setPontos(Double pontos) {
        this.pontos = pontos;
    }

    public Date getValidade() {
        return validade;
    }

    public void setValidade(Date validade) {
        this.validade = validade;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Telefone> getTelefones() {
        if(telefones == null) {
            telefones = new ArrayList<>();
        }
        return telefones;
    }

    public void setTelefones(List<Telefone> telefones) {
        this.telefones = telefones;
    }

    public byte[] getImagem() {
        if(imagem == null) {
            imagem = new byte[] {1};
        }
        return imagem;
    }

    public void setImagem(byte[] imagem) {
        this.imagem = imagem;
    }

    public void imagemDeBlob(Blob blob) throws SQLException {
        if (blob != null) {
            imagem = blob.getBytes(1L, (int) blob.length());
        }
    }

    public Blob imagemParaBlob(Blob blob) throws SQLException {
        blob.setBytes(1L, imagem);
        return blob;
    }
}
