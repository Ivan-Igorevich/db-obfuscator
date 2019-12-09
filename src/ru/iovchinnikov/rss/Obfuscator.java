package ru.iovchinnikov.rss;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Obfuscator {
    private static Connection connection;
    private static Statement statement;
    private static final int UPPERCASE = 65;
    private static final int LOWERCASE = 97;

    private static String generateSymbols(int len, int shift) {
        StringBuilder sb = new StringBuilder("ii_");
        for (int i = 0; i < len; i++) {
            sb.append((char) ((Math.random() * 26) + shift));
        }
        return sb.toString();
    }
    // param example: base_table=column separated by spaces
    public static void main(String[] args) {
        Map<String, ArrayList<String>> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String[] op = args[i].split("=");
            if (chkLen(op, 2)) return;
            String[] tbl = op[0].split("_");
            if (chkLen(tbl, 2)) return;
            params.computeIfAbsent(op[0], k -> new ArrayList<>());
            params.get(op[0]).add(op[1]);
        }

        try {
            Class.forName("org.postgresql.Driver");

            connection = DriverManager.getConnection("jdbc:postgresql://localhost/mobdekbkp?user=postgres&password=postgres");
            statement = connection.createStatement();
            params.forEach((table, columnsList) -> {
                try {
                    ResultSet rs = statement.executeQuery(String.format("select * from %s e", table));
                    while (rs.next()) {
                        for (String value : columnsList) {
                            String q = String.format("update public.%s set %s='%s' where id='%s';", table, value, generateSymbols(15, UPPERCASE), rs.getString("id"));
                            statement.addBatch(q);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            statement.executeBatch();
            statement.close();
            connection.close();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static boolean chkLen(String[] arr, int length) {
        if (arr.length != length) {
            System.out.println("Check parameters");
            return true;
        }
        return false;
    }
}
