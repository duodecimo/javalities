/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.converters;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import org.uss.agendaJdbc.dados.AcessoBancoAgendaJdbc;
import org.uss.agendaJdbc.dados.Tipo;

/**
 *
 * @author duo
 */
@FacesConverter(forClass = Tipo.class, value = "TipoConverter")
public class TipoConverter implements Converter {
    AcessoBancoAgendaJdbc acessoBanco;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (acessoBanco == null) {
            acessoBanco = new AcessoBancoAgendaJdbc();
        }
        Tipo tipo = null;
        Integer tipoId = new Integer(value);
        //System.out.println("==>>> Sera feita tentativa de converter " + value + " para objeto da classe Tipo!!!");
        try {
            tipo = acessoBanco.getTipo(tipoId);
            //System.out.println("==>>> Conversao: obtido tipo " + tipo.getNome() + " !!!");
        } catch (Exception ex) {
            Logger.getLogger(TipoConverter.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("==>>> Erro convertendo " + value + " para objeto da classe Tipo!!!");
        }
        return tipo;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        try {
            Tipo tipo = (Tipo) value;
            //System.out.println("==>>> Conversao: obtido id " + tipo.getId() + " do objeto " + tipo.getNome());
            return "" + tipo.getId();
        } catch (Exception ex) {
            Logger.getLogger(TipoConverter.class.getName()).log(Level.SEVERE, null, ex);
            //System.out.println("==>>> Erro convertendo objeto " + value + " para string com id !!!");
        }
        return null;
    }
}
