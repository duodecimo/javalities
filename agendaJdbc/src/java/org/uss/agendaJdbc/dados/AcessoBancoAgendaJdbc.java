/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.dados;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author duo
 */
public class AcessoBancoAgendaJdbc {
    private Connection connection1;
    private Connection connection2;
    private Statement statement1;
    private Statement statement2;
    
    private void conectar() throws SQLException {
        if(connection1 == null) {
            connection1 = 
                    DriverManager.
                            getConnection("jdbc:derby://localhost:1527/agendaJdbc;create=true", 
                                    "agendaJdbc", "agendaJdbc");
            if(!connection1.getAutoCommit()) {
                connection1.setAutoCommit(true);
            }
        }
        if(connection2 == null) {
            connection2 = 
                    DriverManager.
                            getConnection("jdbc:derby://localhost:1527/agendaJdbc;create=true", 
                                    "agendaJdbc", "agendaJdbc");
            if(!connection2.getAutoCommit()) {
                connection2.setAutoCommit(true);
            }
        }
    }

    private void comandar() throws SQLException {
        conectar();
        if(statement1 == null) {
            statement1 = connection1.createStatement();
        }
        if(statement2 == null) {
            statement2 = connection2.createStatement();
        }
    }

    public List<Pessoa> getPessoas() throws SQLException {
        Pessoa pessoa;
        String cmd = "SELECT id, nome, email FROM pessoa ORDER BY nome";
        List<Pessoa> pessoas;
        comandar();
        try (ResultSet resultSet = statement1.executeQuery(cmd)) {
            pessoas = new ArrayList<>();
            while(resultSet.next()) {
                pessoa = new Pessoa();
                pessoa.setId(resultSet.getInt("id"));
                pessoa.setNome(resultSet.getString("nome"));
                pessoa.setEmail(resultSet.getString("email"));
                pessoas.add(pessoa);
            }
        }
        // recuperar os telefones de cada pessoa
        for(Pessoa p : pessoas) {
            p.setTelefones(getTelefones(p.getId(), p));
        }
        return pessoas;
    }

    public Pessoa getPessoa(int pessoaId) throws SQLException {
        Pessoa pessoa = null;
        String cmd = "SELECT id, nome, email FROM pessoa WHERE id = " + pessoaId;
        comandar();
        ResultSet resultSet = statement1.executeQuery(cmd);
        while(resultSet.next()) {
            pessoa = new Pessoa();
            pessoa.setId(resultSet.getInt("id"));
            pessoa.setNome(resultSet.getString("nome"));
            pessoa.setEmail(resultSet.getString("email"));
            // sempre que recuperar uma pessoa, adicionar
            // a lista de telefones desta pessoa
            pessoa.setTelefones(getTelefones(pessoa.getId(), pessoa));
        }
        return pessoa;
    }

    public List<Tipo> getTipos() throws SQLException {
        Tipo tipo;
        String cmd = "SELECT id, nome FROM tipo ORDER BY nome";
        comandar();
        ResultSet resultSet = statement1.executeQuery(cmd);
        List<Tipo> tipos = new ArrayList<>();
        while(resultSet.next()) {
            tipo = new Tipo();
            tipo.setId(resultSet.getInt("id"));
            tipo.setNome(resultSet.getString("nome"));
            tipos.add(tipo);
        }
        return tipos;
    }

    public Tipo getTipo(int tipoId) throws SQLException {
        Tipo tipo = null;
        String cmd = "SELECT id, nome FROM tipo WHERE id = " + tipoId;
        comandar();
        ResultSet resultSet = statement1.executeQuery(cmd);
        while(resultSet.next()) {
            tipo = new Tipo();
            tipo.setId(resultSet.getInt("id"));
            tipo.setNome(resultSet.getString("nome"));
        }
        return tipo;
    }

    public Tipo getTipo(String tipoNome) throws SQLException {
        Tipo tipo = null;
        String cmd = "SELECT id, nome FROM tipo WHERE nome = " + tipoNome.trim();
        comandar();
        ResultSet resultSet = statement1.executeQuery(cmd);
        while(resultSet.next()) {
            tipo = new Tipo();
            tipo.setId(resultSet.getInt("id"));
            tipo.setNome(resultSet.getString("nome"));
        }
        return tipo;
    }

    /**
     * O método não é público: é utilizado internamente
     * pelos métodos de atualização de pessoa
     * 
     * @param pessoaId
     * @return
     * @throws SQLException 
     */
    private List<Telefone> getTelefones(int pessoaId, Pessoa pessoa) throws SQLException {
        Telefone telefone;
        String cmd = "SELECT id, numero, tipo, pessoa FROM telefone WHERE pessoa = " + pessoaId;
        List<Telefone> telefones = new ArrayList<>();
        comandar();
        try (ResultSet resultSet = statement2.executeQuery(cmd)) {
            while(resultSet.next()) {
                telefone = new Telefone();
                telefone.setId(resultSet.getInt("id"));
                telefone.setNumero(resultSet.getString("numero"));
                telefone.setTipo(getTipo(resultSet.getInt("tipo")));
                telefone.setPessoa(pessoa);
                telefones.add(telefone);
            }
        }
        return telefones;
    }

    public void criarPessoa(Pessoa pessoa) throws SQLException {
        String cmd = "INSERT INTO pessoa values("
                + "default, "
                + "'" + pessoa.getNome() + "'" + ", "
                + "'" + pessoa.getEmail() + "'" + ")";
        comandar();
        statement1.execute(cmd);
        // incluir telefones eventualmente presentes
        // na lista de telefones do objeto pessoa.
        // observe que como e um novo objeto pessoa,
        // só podem haver telefones para incluir
        for (Telefone telefone : pessoa.getTelefones()) {
            criarTelefone(telefone);
        }
    }

    public void criarTipo(Tipo tipo) throws SQLException {
        String cmd = "INSERT INTO tipo values("
                + "default, "
                + "'" + tipo.getNome() + "'" +  ")";
        comandar();
        statement1.execute(cmd);
    }

    /**
     * O método não é público: é utilizado internamente
     * pelos métodos de atualização de pessoa
     * @param telefone
     * @throws SQLException 
     */
    protected void criarTelefone(Telefone telefone) throws SQLException {
        String cmd = "INSERT INTO telefone values("
                + "default, "
                + "'" + telefone.getNumero()+ "'" + ", "
                + "" + telefone.getTipo().getId()+ ", " 
                //+ "1, " // fixando tipo em um para debug
                + "" + telefone.getPessoa().getId()+ "" + ")";
        comandar();
        statement2.execute(cmd);
    }

    public void alterarPessoa(Pessoa pessoa) throws SQLException {
        String cmd = "UPDATE pessoa "
                + "SET nome = "
                + "'" + pessoa.getNome() + "'" + ", "
                + "email = "
                + "'" + pessoa.getEmail() + "' "
                + "WHERE id = " + pessoa.getId();
        comandar();
        statement1.execute(cmd);
        // ao alterar uma pessoa, e preciso verificar
        // a lista de telefones da pessoa, podem ter
        // havido inclusoes e alterações.
        //System.out.println("===>>>> Alterando pessoa: atualizando telefones!");
        for (Telefone telefone : pessoa.getTelefones()) {
            //System.out.println("===>>>>    Iterando telefone " + telefone.getNumero());
            if (telefone.getId() != null && telefone.getId() > 0) { // telefone ja existe
                alterarTelefone(telefone);
            } else {
                criarTelefone(telefone);
            }
        }
    }

    public void alterarTipo(Tipo tipo) throws SQLException {
        String cmd = "UPDATE tipo "
                + "SET nome = "
                + "'" + tipo.getNome() + "'" + ", "
                + "WHERE id = " + tipo.getId();
        comandar();
        statement1.execute(cmd);
    }

    /**
     * O método não é público: é utilizado internamente
     * pelos métodos de atualização de pessoa
     * 
     * @param telefone
     * @throws SQLException 
     */
    private void alterarTelefone(Telefone telefone) throws SQLException {
        String cmd = "UPDATE telefone "
                + "SET numero = "
                + "'" + telefone.getNumero()+ "'" + ", "
                + "tipo = "
                + "" + telefone.getTipo().getId()+ ", "
                + "pessoa = "
                + "" + telefone.getPessoa().getId()+ " "
                + "WHERE id = " + telefone.getId();
        comandar();
        statement2.execute(cmd);
    }
}
