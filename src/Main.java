public class Main {
    public static void main(String[] args) {
        Database_Access da = new Database_Access();
        String user = "tahina";
        String password = "tahina";
        String databasename = "extraction";
        String port = "5432";
        da.getPostgreSQLConnexion(user, databasename, password, port);
        da.closeConnection();
    }
}