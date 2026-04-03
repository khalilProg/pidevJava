package tn.esprit.services;

import tn.esprit.entities.User;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.List;

public class UserService implements IGeneralService<User> {
    Connection cn;
    public UserService(){
        cn = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(User user) throws SQLException {

    }

    @Override
    public void supprimer(User user) {

    }

    @Override
    public int chercher(User user) {
        return 0;
    }

    @Override
    public void modifier(User user) {

    }

    @Override
    public List<User> recuperer() throws SQLException {
        return List.of();
    }
}
