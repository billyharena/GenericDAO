/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package function;

import database.DatabaseConnexion;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author billy
 */
public class Function {
    public static Connection getConnection() throws Exception{
        String user = "user_name";
        String databasename = "database_name";
        String password = "password";
        String port = "port";
        Connection conn = new DatabaseConnexion().getPostgreSQLConnexion(user,databasename,password, port);
        return conn;
    }
    /*public static long getYear(Date un, Date deux){
        long p = un.getTime() - deux.getTime();
        long i =  p /(24 * 60 * 60 * 1000) / 365;
        System.out.println("age = "+ i);
        return i;
    }*/
    
    public static String getSqlInsertionWithIdReturn(Object object) throws Exception{
        String sql = "";
        Class clazz = object.getClass();
        String tableName = clazz.getSimpleName();
        sql = "INSERT INTO " + tableName + " (";
        Field[] fields = clazz.getDeclaredFields();
        String value = "";
        for (Field field : fields) {
            field.setAccessible(true);
            Object fieldValue = field.get(object);
            if (fieldValue != null) {
                sql += field.getName() + ",";
                value += "?,";
            }
        }
        if (sql.endsWith(",")) {
            sql = sql.substring(0, sql.length() - 1);
            value = value.substring(0, value.length() - 1);
        }
        sql += ") VALUES (" + value + ") RETURNING id";
        System.out.println("sql == " + sql);
        return sql;

    }
    public static String getSqlSelection(Object object, String recherche) throws IllegalAccessException {
        String sql = "SELECT " + recherche+ " FROM " + object.getClass().getSimpleName();
        boolean hasConditions = false;
        List<Field> fields = getAllFields(new ArrayList<>(), object.getClass());
        for (Field field : fields) {
            field.setAccessible(true);
            if (field.get(object) != null) {
                if (!hasConditions) {
                    sql += " WHERE ";
                    hasConditions = true;
                } else {
                    sql += " AND ";
                }
                sql += field.getName() + " = ?";
            }
        }
        return sql;
    }
    public static List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }

}
