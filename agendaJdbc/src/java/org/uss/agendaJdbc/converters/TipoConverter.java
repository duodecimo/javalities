/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.uss.agendaJdbc.converters;

import java.sql.SQLException;
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
@FacesConverter("org.uss.agendaJdbc.converters.TipoConverter")
public class TipoConverter implements Converter {
    AcessoBancoAgendaJdbc acessoBanco;

    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        if (acessoBanco == null) {
            acessoBanco = new AcessoBancoAgendaJdbc();
        }
        Integer tipo = new Integer(value);
        try {
            return acessoBanco.getTipo(tipo);
        } catch (SQLException ex) {
            Logger.getLogger(TipoConverter.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, Object value) {
        Tipo tipo = (Tipo) value;
        return "" + tipo.getId();
    }
    
}
