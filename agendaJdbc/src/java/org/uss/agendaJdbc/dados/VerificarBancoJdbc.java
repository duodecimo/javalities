/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.dados;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author duo
 */
public class VerificarBancoJdbc {
    private Connection connection1;
    private Connection connection2;
    private Statement statement1;
    private Statement statement2;
    private boolean verificarBanco;
    private boolean verificadoTabelaPessoa;
    private boolean verificadoTabelaTipo;
    private boolean verificadoTabelaTelefone;

    public VerificarBancoJdbc() {
        verificarBanco();
        verificarTabelas();
    }
    
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
        if (!verificadoTabelaPessoa) {
            // tabela pessoa ainda não foi verificada nesta sessão
            verificarBanco();
            try {
                String cmd = "CREATE TABLE pessoa("
                        + "id INT NOT NULL GENERATED ALWAYS AS IDENTITY CONSTRAINT pessoaPK PRIMARY KEY, "
                        + "nome VARCHAR(255) CONSTRAINT pessoaNomeUnique UNIQUE, "
                        + "email VARCHAR(255), pontos DECIMAL(12, 2), validade DATE"
                        + ")";
                // comando de criação obtido do JavaDb: 
                // CREATE TABLE PESSOA (ID INTEGER DEFAULT AUTOINCREMENT: start 1 increment 1  
                // NOT NULL GENERATED ALWAYS AS IDENTITY, NOME VARCHAR(255), EMAIL VARCHAR(255), 
                // PONTOS DECIMAL(12, 2), VALIDADE DATE, PRIMARY KEY (ID));
                comandar();
                statement1.execute(cmd);
                // nesta sessão não é mais nescessaŕio verificar a tabela pessoa
                verificadoTabelaPessoa = true;
            } catch (SQLException ex) {
                Logger.getLogger(AcessoBancoAgendaJdbc.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void verificarTabelaTipo() {
        if (!verificadoTabelaTipo) {
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
            verificadoTabelaTipo = true;
        }
    }

    private void verificarTabelaTelefone() {
        // comando de criação obtido do JavaDb:
        // CREATE TABLE TELEFONE (ID INTEGER DEFAULT AUTOINCREMENT: start 1 increment 1  NOT NULL 
        // GENERATED ALWAYS AS IDENTITY, NUMERO VARCHAR(20), TIPO INTEGER, PESSOA INTEGER, PRIMARY KEY (ID));
        if (!verificadoTabelaTelefone) {
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
            verificadoTabelaTelefone = true;
        }
    }

    public boolean isVerificadoTabelaPessoa() {
        return verificadoTabelaPessoa;
    }

    public void setVerificadoTabelaPessoa(boolean verificadoTabelaPessoa) {
        this.verificadoTabelaPessoa = verificadoTabelaPessoa;
    }

    public boolean isVerificadoTabelaTipo() {
        return verificadoTabelaTipo;
    }

    public void setVerificadoTabelaTipo(boolean verificadoTabelaTipo) {
        this.verificadoTabelaTipo = verificadoTabelaTipo;
    }

    public boolean isVerificadoTabelaTelefone() {
        return verificadoTabelaTelefone;
    }

    public void setVerificadoTabelaTelefone(boolean verificadoTabelaTelefone) {
        this.verificadoTabelaTelefone = verificadoTabelaTelefone;
    }

    public static void main(String[] str) {
        new VerificarBancoJdbc();
    }
}
