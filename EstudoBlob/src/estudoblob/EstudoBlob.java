/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package estudoblob;

import dados.Pessoa;
import gui.MostrarPessoas;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import javax.swing.ImageIcon;

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
            ObjectOutputStream objectOutputStream;
            blob = connection.createBlob();
            objectOutputStream = new ObjectOutputStream(blob.setBinaryStream(1));
            // adicionar pessoas
            pessoa = new Pessoa();
            pessoa.setNome("Astofoboldo Neves");
            pessoa.setImagem(new ImageIcon("images/astofoboldo.jpg"));
            // persistir pessoa
            prepareStatement.setString(1, pessoa.getNome());
            blob = connection.createBlob();
            objectOutputStream = new ObjectOutputStream(blob.setBinaryStream(1));
            objectOutputStream.writeObject(pessoa.getImagem());
            objectOutputStream.close();
            prepareStatement.setBlob(2, blob);
            prepareStatement.execute();
            // adicionar pessoas
            pessoa = new Pessoa();
            pessoa.setNome("Miforonalda Esteves");
            pessoa.setImagem(new ImageIcon("images/miforonalda.jpg"));
            // persistir pessoa
            prepareStatement.setString(1, pessoa.getNome());
            blob = connection.createBlob();
            objectOutputStream = new ObjectOutputStream(blob.setBinaryStream(1));
            objectOutputStream.writeObject(pessoa.getImagem());
            objectOutputStream.close();
            prepareStatement.setBlob(2, blob);
            prepareStatement.execute();
            // recuperar dados
            ResultSet resultSet = statement.executeQuery("SELECT * FROM pessoa");
            ObjectInputStream objectInputStream;
            List<Pessoa> pessoas = new ArrayList<>();
            while(resultSet.next()) {
                pessoa = new Pessoa();
                pessoa.setId(resultSet.getInt("id"));
                pessoa.setNome(resultSet.getString("nome"));
                objectInputStream = new ObjectInputStream(resultSet.getBlob("imagem").getBinaryStream());
                try {
                    pessoa.setImagem((ImageIcon) objectInputStream.readObject());
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(EstudoBlob.class.getName()).log(Level.SEVERE, null, ex);
                    System.out.println("===>>> Erro recuperando imagem : " + ex);
                }
                pessoas.add(pessoa);
            }
            // mostar lista de pessoas
            new MostrarPessoas(pessoas);
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
