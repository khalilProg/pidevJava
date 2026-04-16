package tn.esprit.services;

import java.sql.SQLException;
import java.util.List;

public interface IGeneralService<T> {
    void ajouter(T t) throws SQLException;
    void supprimer(T t);
    int chercher(T t);
    void modifier(T t);
    List<T> recuperer() throws SQLException;
}
