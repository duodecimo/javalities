/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estudoblob;

import dados.Pessoa;
import gui.MostrarPessoas;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author duo
 */
public class EstudoBlob {

    Pessoa pessoa;
    private Connection connection;
    private Statement statement;

    public EstudoBlob() {
        try { 
            connection =
                    DriverManager.
                            getConnection("jdbc:derby://localhost:1527/estudoBlob;create=true", 
                                    "estudoBlob", "estudoBlob");
            if(!connection.getAutoCommit()) {
                connection.setAutoCommit(true);
            }
            statement = connection.createStatement();
            /*            try {
            String cmd = "CREATE SCHEMA estudoBlob";
            statement.execute(cmd);
            } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
            try {
            String cmd = "SET SCHEMA estudoBlob";
            statement.execute(cmd);
            } catch (SQLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }*/
            try {
                String cmd = "CREATE TABLE pessoa("
                        + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT pessoaPK PRIMARY KEY, "
                        + "nome VARCHAR(255) CONSTRAINT pessoaNomeUnique UNIQUE, "
                        + "imagem BLOB"
                        + ")";
                statement.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
            try {
                // limpar a tabela
                String cmd = "DELETE FROM pessoa";
                statement.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
            }
            PreparedStatement prepareStatement = 
                    connection.prepareStatement("INSERT INTO pessoa(nome, imagem) VALUES (?, ?)");
            Blob blob;
            BufferedImage originalImage;
            ByteArrayOutputStream baos;
            byte[] imageInByte;
            
            blob = connection.createBlob();
            // adicionar pessoas
            pessoa = new Pessoa();
            pessoa.setNome("Astofoboldo Neves");
            originalImage = ImageIO.read(new File("images/astofoboldo.jpg"));
            baos = new ByteArrayOutputStream();
            ImageIO.write( originalImage, "jpg", baos );
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();
            pessoa.setImagem(imageInByte);
            // persistir pessoa
            prepareStatement.setString(1, pessoa.getNome());
            blob = connection.createBlob();
            blob = pessoa.imagemParaBlob(blob);
            prepareStatement.setBlob(2, blob);
            prepareStatement.execute();
            // adicionar pessoas
            pessoa = new Pessoa();
            pessoa.setNome("Miforonalda Esteves");
            originalImage = ImageIO.read(new File("images/miforonalda.jpg"));
            baos = new ByteArrayOutputStream();
            ImageIO.write( originalImage, "jpg", baos );
            baos.flush();
            imageInByte = baos.toByteArray();
            baos.close();
            pessoa.setImagem(imageInByte);
            // persistir pessoa
            prepareStatement.setString(1, pessoa.getNome());
            blob = connection.createBlob();
            blob = pessoa.imagemParaBlob(blob);
            prepareStatement.setBlob(2, blob);
            prepareStatement.execute();
            // recuperar dados
            ResultSet resultSet = statement.executeQuery("SELECT * FROM pessoa");
            List<Pessoa> pessoas = new ArrayList<>();
            while(resultSet.next()) {
                pessoa = new Pessoa();
                pessoa.setId(resultSet.getInt("id"));
                pessoa.setNome(resultSet.getString("nome"));
                pessoa.imagemDeBlob(resultSet.getBlob("imagem"));
                pessoas.add(pessoa);
            }
            // mostar lista de pessoas
            MostrarPessoas mostrarPessoas = new MostrarPessoas(pessoas);
        } catch (SQLException | IOException ex) {
            Logger.getLogger(EstudoBlob.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EstudoBlob estudoBlob = new EstudoBlob();
    }
    
}
