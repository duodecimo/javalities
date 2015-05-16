/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dados;

import java.sql.Blob;
import java.sql.SQLException;
import javax.swing.ImageIcon;

/**
 *
 * @author duo
 */
public class Pessoa {
    private Integer id;
    private String nome;
    private byte[] imagem;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public byte[] getImagem() {
        return imagem;
    }

    public void setImagem(byte[] imagem) {
        this.imagem = imagem;
    }

    public void imagemDeBlob(Blob blob) throws SQLException {
	imagem = blob.getBytes(1L, (int) blob.length());
    }

    public Blob imagemParaBlob(Blob blob) throws SQLException {
        blob.setBytes(1L, imagem);
        return blob;
    }
}
