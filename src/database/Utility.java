/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import function.Function;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author billy
 */
public class Utility {
    private String user = "user_name";
    private String databasename = "database_name";
    private String password = "password";
    private String port = "port";

    // FONCTION INSERTION GENERALISEE RECEVANT UNE CONNECTION ET NE LE FERME PAS
    public void insertObject(Object object, Connection con) throws SQLException, IllegalAccessException {
        String tableName = object.getClass().getSimpleName();
        StringBuilder columns = new StringBuilder();
        StringBuilder values = new StringBuilder();
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (!field.getName().equals("id")) {
                Object value = field.get(object);
                if (value != null) {
                    columns.append(field.getName()).append(",");
                    values.append("'").append(value).append("',");
                }
            }
        }
        Class superClass = object.getClass().getSuperclass();
        while (superClass != Object.class) {
            for (Field field : superClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (!field.getName().equals("id")) {
                    Object value = field.get(object);
                    if (value != null) {
                        columns.append(field.getName()).append(",");
                        values.append("'").append(value).append("',");
                    }
                }
            }
            superClass = superClass.getSuperclass();
        }
        columns.deleteCharAt(columns.length() - 1);
        values.deleteCharAt(values.length() - 1);
        String sql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
        System.out.println(sql);
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            con.close();
            if (con.isClosed()) System.out.println("Connection fermée dû à une exception !");
            throw e;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // FONCTION INSERTION GENERALISEE NE RECEVANT PAS DE CONNEXION MAIS LA FERME
    public void insertObject(Object object) throws Exception {
        DatabaseConnexion con = new DatabaseConnexion();
        Connection conn = con.getPostgreSQLConnexion(this.user, this.databasename, this.password, this.port);
        insertObject(object, conn);
        conn.close();
        if (conn.isClosed()) System.out.println("Connection fermée !!");
        else System.out.println("Connection en cours !!");
    }

    // FONCTION SELECTION GENERALISEE RECEVANT UNE CONNEXION EN ARGUMENT ET NE LA FERMANT PAS
    public List<Object> selectObjects(Object clazz, Connection con) throws Exception {
        List<Object> objects = new ArrayList<>();
        Class c = clazz.getClass();
        String sql = Function.getSqlSelection(clazz, "*");
        System.out.println("mon sql = " + sql);
        Connection connection = con;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            System.out.println("Connection =" + connection);
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Object object = c.newInstance();
                for (Field field : object.getClass().getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(object, resultSet.getObject(field.getName()));
                }
                // Si l'objet est une classe dérivée, on récupère également les valeurs des champs de la classe parente
                Class superClass = object.getClass().getSuperclass();
                while (superClass != Object.class) {
                    for (Field field : superClass.getDeclaredFields()) {
                        field.setAccessible(true);
                        field.set(object, resultSet.getObject(field.getName()));
                    }
                    superClass = superClass.getSuperclass();
                }
                objects.add(object);
            }
        } catch (Exception e) {
            e.printStackTrace();
            connection.close();
            if (connection.isClosed()) System.out.println("Connection fermée dû à une exception !");
            throw e;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (statement != null) statement.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw ex;
            }
        }
        return objects;
    }

    // FONCTION SELECTION GENERALISEE N'AYANT PAS DE CONNEXION EN ARGUMENT ET QUI LA FERME
    public List<Object> selectObjects(Object object) throws Exception {
        List<Object> objects = new ArrayList<>();
        DatabaseConnexion con = new DatabaseConnexion();
        Connection conn = con.getPostgreSQLConnexion(this.user, this.databasename, this.password, this.port);
        objects = selectObjects(object, conn);
        conn.close();
        if (conn.isClosed()) System.out.println("Connection fermée !!");
        else System.out.println("Connection en cours !!");
        return objects;
    }

    //  Fonction de modification de n'importe quel objet en héritage dans une bdd en java
    public void updateObject(Object object, int id, Connection con) throws SQLException, IllegalAccessException {
        String tableName = object.getClass().getSimpleName();
        StringBuilder updates = new StringBuilder();
        for (Field field : object.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if (field.get(object) != null) {
                updates.append(field.getName()).append(" = '").append(field.get(object)).append("',");
            }
        }
        // Si l'objet est une classe dérivée, on ajoute également les mises à jour des champs de la classe parente
        Class superClass = object.getClass().getSuperclass();
        while (superClass != Object.class) {
            for (Field field : superClass.getDeclaredFields()) {
                field.setAccessible(true);
                if (field.get(object) != null) {
                    updates.append(field.getName()).append(" = '").append(field.get(object)).append("',");
                }
            }
            superClass = superClass.getSuperclass();
        }
        if (updates.length() == 0) {
            return;
        }
        updates.deleteCharAt(updates.length() - 1);
        String sql = "UPDATE " + tableName + " SET " + updates + " WHERE id = " + id;
        System.out.println("sql modif = "+sql);
        Statement stmt = null;
        try {
            stmt = con.createStatement();
            stmt.executeUpdate(sql);
        } catch (Exception e) {
            e.printStackTrace();
            con.close();
            if (con.isClosed()) System.out.println("Connection fermée dû à une exception !");
            throw e;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    // UPDATE SANS CONNEXION
    public void updateObject(Object object, int id) throws Exception {
        DatabaseConnexion con = new DatabaseConnexion();
        Connection conn = con.getPostgreSQLConnexion(this.user, this.databasename, this.password, this.port);
        updateObject(object, id, conn);
        conn.close();
        if (conn.isClosed()) System.out.println("Connection fermée !!");
        else System.out.println("Connection en cours !!");
    }

    // FONCTION POUR AVOIR L'ID D'UN OBJET
    public int getId(Object object, Connection conn) throws Exception {
        int id = 0;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Field[] fields = object.getClass().getDeclaredFields();
        String sql = Function.getSqlSelection(object, "id");
        System.out.println("sql = " + sql);
        try {
            preparedStatement = conn.prepareStatement(sql);
            // Remplissez les paramètres de la requête
            int i = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                if (!field.getType().isPrimitive() && field.get(object) != null ||
                        (field.getType().isPrimitive() && (field.get(object) != null && !field.get(object).equals(0)))) {
                    System.out.println("field =" + field.get(object));
                    preparedStatement.setObject(i, field.get(object));
                    i++;
                }
            }
            System.out.println("sql 2 = " + sql);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) id = resultSet.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
            conn.close();
            if (conn.isClosed()) System.out.println("Connection fermée dû à une exception !");
            throw e;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("ici encore " + id);
        return id;
    }

    public int getId(Object object) throws Exception {
        int id = 0;
        DatabaseConnexion con = new DatabaseConnexion();
        Connection conn = con.getPostgreSQLConnexion(this.user, this.databasename, this.password, this.port);
        id = getId(object, conn);
        System.out.println("ici " + id);
        conn.close();
        if (conn.isClosed()) System.out.println("Connection fermée !!");
        else System.out.println("Connection en cours !!");
        return id;
    }

    // INSERTION QUI RETURNE L'ID DU DERNIER ELEMENT INSERE
    public int InsertionWithIdOfLastElement(Object object, Connection conn) throws Exception {
        int id = 0;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        Field[] fields = object.getClass().getDeclaredFields();
        String sql = Function.getSqlInsertionWithIdReturn(object);
        try {
            preparedStatement = conn.prepareStatement(sql);
            // Remplissez les paramètres de la requête
            int i = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                if (!field.getType().isPrimitive() && field.get(object) != null ||
                        (field.getType().isPrimitive() && (field.get(object) != null && !field.get(object).equals(0))))
                    preparedStatement.setObject(i, field.get(object));
                i++;
            }
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) id = resultSet.getInt("id");
        } catch (Exception e) {
            e.printStackTrace();
            conn.close();
            if (conn.isClosed()) System.out.println("Connection fermée dû à une exception !");
            throw e;
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (preparedStatement != null) preparedStatement.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return id;
    }

    public int InsertionWithIdOfLastElement(Object object) throws Exception {
        int id = 0;
        DatabaseConnexion con = new DatabaseConnexion();
        Connection conn = con.getPostgreSQLConnexion(this.user, this.databasename, this.password, this.port);
        id = InsertionWithIdOfLastElement(object, conn);
        conn.close();
        if (conn.isClosed()) System.out.println("Connection fermée !!");
        else System.out.println("Connection en cours !!");
        return id;
    }

    // SELECTION DU DERNIER ELEMENT INSERER DANS UNE TABLE
    public Integer getIdOfLastElement(Object object, Connection conn) throws Exception {
        Integer id = 0;
        String tablename = object.getClass().getSimpleName();
        String sql = "SELECT last_value FROM " + tablename + "_id_seq";
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        if (resultSet.next()) id = resultSet.getInt("last_value");
        return id;
    }

    public Integer getIdOfLastElement(Object object) throws Exception {
        Integer id = 0;
        DatabaseConnexion con = new DatabaseConnexion();
        Connection conn = con.getPostgreSQLConnexion(this.user, this.databasename, this.password, this.port);
        id = getIdOfLastElement(object, conn);
        conn.close();
        return id;
    }

    public static List advancedSearch(Object object, Connection conn) throws Exception {
        List resultList = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        String sql = Function.getSqlSelection(object, "*");
        System.out.println("sal = "+sql);
        try {
            preparedStatement = conn.prepareStatement(sql);
            List<Field> fields = Function.getAllFields(new ArrayList<>(), object.getClass());
            // Remplissez les paramètres de la requête
            int i = 1;
            for (Field field : fields) {
                field.setAccessible(true);
                if (!field.getType().isPrimitive() && field.get(object) != null ||
                        (field.getType().isPrimitive() && (field.get(object) != null && !field.get(object).equals(0)))) {
                    preparedStatement.setObject(i, field.get(object));
                    i++;
                }
            }
            resultSet = preparedStatement.executeQuery();
            // Remplissez la liste de résultats
            Class<?> clazz = object.getClass();
            while (resultSet.next()) {
                Object resultObject = (Object) clazz.newInstance();
                for (Field field : fields) {
                    field.setAccessible(true);
                    field.set(resultObject, resultSet.getObject(field.getName()));
                }
                resultList.add(resultObject);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultList;
    }

    public List advancedSearch(Object object) throws Exception{
        List<Object> objects = new ArrayList<>();
        DatabaseConnexion con = new DatabaseConnexion();
        Connection conn = con.getPostgreSQLConnexion(this.user, this.databasename, this.password, this.port);
        objects = advancedSearch(object, conn);
        conn.close();
        if (conn.isClosed()) System.out.println("Connection fermée !!");
        else System.out.println("Connection en cours !!");
        return objects;
    }
}