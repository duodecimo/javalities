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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duo
 */
public class AcessoBancoAgendaJdbc {
    private Connection connection1;
    private Connection connection2;
    private Statement statement1;
    private Statement statement2;
    private boolean verificarBanco;
    private boolean verificarTabelaPessoa;
    private boolean verificarTabelaTipo;
    private boolean verificarTabelaTelefone;
    
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

    private void verificarBanco() {
        if(!verificarBanco) {
            try {
                String cmd = "CREATE SCHEMA agendaJdbc";
                comandar();
                statement1.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                String cmd = "SET SCHEMA agendaJdbc";
                comandar();
                statement1.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
            verificarBanco = true;
        }
    }

    private void verificarTabelas() {
        verificarTabelaPessoa();
        verificarTabelaTipo();
        verificarTabelaTelefone();
    }

    private void verificarTabelaPessoa() {
        if (!verificarTabelaPessoa) {
            // tabela pessoa ainda não foi verificada nesta sessão
            verificarBanco();
            try {
                String cmd = "CREATE TABLE pessoa("
                        + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT pessoaPK PRIMARY KEY, "
                        + "nome VARCHAR(255) CONSTRAINT pessoaNomeUnique UNIQUE, "
                        + "email VARCHAR(255)"
                        + ")";
                comandar();
                statement1.execute(cmd);
                // nesta sessão não é mais nescessaŕio verificar a tabela pessoa
                verificarTabelaPessoa = true;
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void verificarTabelaTipo() {
        if (!verificarTabelaTipo) {
            // tabela tipo ainda não foi verificada nesta sessão
            verificarBanco();
            try {
                String cmd = "CREATE TABLE tipo("
                        + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT tipoPK PRIMARY KEY, "
                        + "nome VARCHAR(255) CONSTRAINT tipoNomeUnique UNIQUE "
                        + ")";
                comandar();
                statement1.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
            // nesta sessão não é mais nescessaŕio verificar a tabela tipo
            verificarTabelaTipo = true;
        }
    }

    private void verificarTabelaTelefone() {
        if (!verificarTabelaTelefone) {
            // tabela telefone ainda não foi verificada nesta sessão
            verificarBanco();
            try {
                String cmd = "CREATE TABLE telefone("
                        + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT telefonePK PRIMARY KEY, "
                        + "numero VARCHAR(20) CONSTRAINT telefoneNumeroUnique UNIQUE, "
                        + "tipo INT, "
                        + "pessoa INT"
                        + ")";
                comandar();
                statement1.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                String cmd = "ALTER TABLE telefone "
                        + "ADD CONSTRAINT telefoneTipoFK "
                        + "FOREIGN KEY (tipo) REFERENCES tipo (id) ON DELETE CASCADE ON UPDATE RESTRICT";
                comandar();
                statement1.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
            try {
                String cmd = "ALTER TABLE telefone "
                        + "ADD CONSTRAINT telefonePessoaFK "
                        + "FOREIGN KEY (pessoa) REFERENCES pessoa (id) ON DELETE CASCADE ON UPDATE RESTRICT";
                comandar();
                statement1.execute(cmd);
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
            // nesta sessão não é mais nescessaŕio verificar a tabela telefone
            verificarTabelaTelefone = true;
        }
    }

    public List<Pessoa> getPessoas() throws SQLException {
        Pessoa pessoa;
        String cmd = "SELECT id, nome, email FROM pessoa ORDER BY nome";
        verificarTabelaPessoa();
        List<Pessoa> pessoas;
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
            p.setTelefones(getTelefones(p.getId()));
        }
        return pessoas;
    }

    public Pessoa getPessoa(int pessoaId) throws SQLException {
        Pessoa pessoa = null;
        String cmd = "SELECT id, nome, email FROM pessoa WHERE id = " + pessoaId;
        verificarTabelaPessoa();
        ResultSet resultSet = statement1.executeQuery(cmd);
        while(resultSet.next()) {
            pessoa = new Pessoa();
            pessoa.setId(resultSet.getInt("id"));
            pessoa.setNome(resultSet.getString("nome"));
            pessoa.setEmail(resultSet.getString("email"));
            // sempre que recuperar uma pessoa, adicionar
            // a lista de telefones desta pessoa
            pessoa.setTelefones(getTelefones(pessoa.getId()));
        }
        return pessoa;
    }

    public List<Tipo> getTipos() throws SQLException {
        Tipo tipo;
        String cmd = "SELECT id, nome FROM tipo ORDER BY nome";
        verificarTabelaTipo();
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
        verificarTabelaTipo();
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
        verificarTabelaTipo();
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
    private List<Telefone> getTelefones(int pessoaId) throws SQLException {
        Telefone telefone;
        String cmd = "SELECT id, numero, tipo, pessoa FROM telefone WHERE pessoa = " + pessoaId;
        verificarTabelas();
        List<Telefone> telefones = new ArrayList<>();
        try (ResultSet resultSet = statement2.executeQuery(cmd)) {
            while(resultSet.next()) {
                telefone = new Telefone();
                telefone.setId(resultSet.getInt("id"));
                telefone.setNumero(resultSet.getString("numero"));
                telefone.setTipo(getTipo(resultSet.getInt("tipo")));
                telefone.setPessoa(getPessoa(resultSet.getInt("pessoa")));
                telefones.add(telefone);
            }
        }
        return telefones;
    }

    public void criarPessoa(Pessoa pessoa) throws SQLException {
        verificarTabelaPessoa();
        String cmd = "INSERT INTO pessoa values("
                + "default, "
                + "'" + pessoa.getNome() + "'" + ", "
                + "'" + pessoa.getEmail() + "'" + ")";
        statement1.execute(cmd);
        // incluir telefones presentes no objeto

        for (Telefone telefone : pessoa.getTelefones()) {
            criarTelefone(telefone);
        }
    }

    public void criarTipo(Tipo tipo) throws SQLException {
        verificarTabelaTipo();
        String cmd = "INSERT INTO tipo values("
                + "default, "
                + "'" + tipo.getNome() + "'" +  ")";
        statement1.execute(cmd);
    }

    /**
     * O método não é público: é utilizado internamente
     * pelos métodos de atualização de pessoa
     * @param telefone
     * @throws SQLException 
     */
    protected void criarTelefone(Telefone telefone) throws SQLException {
        verificarTabelas();
        String cmd = "INSERT INTO telefone values("
                + "default, "
                + "'" + telefone.getNumero()+ "'" + ", "
                + "" + telefone.getTipo().getId()+ ", " 
                + "" + telefone.getPessoa().getId()+ "" + ")";
        statement2.execute(cmd);
    }

    public void alterarPessoa(Pessoa pessoa) throws SQLException {
        verificarTabelaPessoa();
        String cmd = "UPDATE pessoa "
                + "SET nome = "
                + "'" + pessoa.getNome() + "'" + ", "
                + "email = "
                + "'" + pessoa.getEmail() + "' "
                + "WHERE id = " + pessoa.getId();
        statement1.execute(cmd);
        // ao alterar uma pessoa, e preciso verificar
        // a lista de telefones da pessoa, podem ter
        // havido inclusoes e alterações.
        for (Telefone telefone : pessoa.getTelefones()) {
            if (telefone.getId() > 0) { // telefone ja existe
                alterarTelefone(telefone);
            } else {
                criarTelefone(telefone);
            }
        }
    }

    public void alterarTipo(Tipo tipo) throws SQLException {
        verificarTabelaPessoa();
        String cmd = "UPDATE tipo "
                + "SET nome = "
                + "'" + tipo.getNome() + "'" + ", "
                + "WHERE id = " + tipo.getId();
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
        verificarTabelas();
        String cmd = "UPDATE telefone "
                + "SET numero = "
                + "'" + telefone.getNumero()+ "'" + ", "
                + "tipo = "
                + "" + telefone.getTipo().getId()+ ", "
                + "pessoa = "
                + "" + telefone.getPessoa().getId()+ " "
                + "WHERE id = " + telefone.getId();
        statement2.execute(cmd);
    }

    public boolean isVerificarTabelaPessoa() {
        return verificarTabelaPessoa;
    }

    public void setVerificarTabelaPessoa(boolean verificarTabelaPessoa) {
        this.verificarTabelaPessoa = verificarTabelaPessoa;
    }

    public boolean isVerificarTabelaTipo() {
        return verificarTabelaTipo;
    }

    public void setVerificarTabelaTipo(boolean verificarTabelaTipo) {
        this.verificarTabelaTipo = verificarTabelaTipo;
    }

    public boolean isVerificarTabelaTelefone() {
        return verificarTabelaTelefone;
    }

    public void setVerificarTabelaTelefone(boolean verificarTabelaTelefone) {
        this.verificarTabelaTelefone = verificarTabelaTelefone;
    }
}
