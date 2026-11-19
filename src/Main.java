import java.sql.*;


private static String vytvorTabulku(Connection conn) throws SQLException {
    String ddl = """
                CREATE TABLE IF NOT EXISTS auta (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    spz VARCHAR(15) NOT NULL UNIQUE,
                    znacka VARCHAR(50) NOT NULL,
                    model VARCHAR(50) NOT NULL,
                    datum_vyroby DATE NOT NULL,
                    najete_km INT NOT NULL
                )
                """;

    try (Statement stmt = conn.createStatement()) {
        stmt.execute(ddl);
        System.out.println("Tabulka 'auta' je připravena.");
    }
    return ddl;
}

private static void vyhledejAuta(Connection conn, String znacka, String model, Integer maxKm) throws SQLException {
    StringBuilder sql = new StringBuilder("SELECT id, spz, znacka, model, datum_vyroby, najete_km FROM auta WHERE 1=1");
    List<Object> parametry = new ArrayList<>();

    if (znacka != null && !znacka.isBlank()) {
        sql.append(" AND LOWER(znacka) = LOWER(?)");
        parametry.add(znacka.trim());
    }
    if (model != null && !model.isBlank()) {
        sql.append(" AND LOWER(model) = LOWER(?)");
        parametry.add(model.trim());
    }
    if (maxKm != null) {
        sql.append(" AND najete_km <= ?");
        parametry.add(maxKm);
    }

    try (PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
        for (int i = 0; i < parametry.size(); i++) {
            pstmt.setObject(i + 1, parametry.get(i));
        }

        try (ResultSet rs = pstmt.executeQuery()) {
            System.out.println("\nNalezená vozidla:");
            System.out.println("---------------------------------------------------------------------------------");
            System.out.printf("%-4s | %-10s | %-12s | %-12s | %-14s | %-10s%n",
                    "ID", "SPZ", "Značka", "Model", "Datum výroby", "Najeto km");
            System.out.println("---------------------------------------------------------------------------------");

            boolean nalezeno = false;
            while (rs.next()) {
                nalezeno = true;
                System.out.printf("%-4d | %-10s | %-12s | %-12s | %-14s | %,d km%n",
                        rs.getInt("id"),
                        rs.getString("spz"),
                        rs.getString("znacka"),
                        rs.getString("model"),
                        rs.getDate("datum_vyroby"),
                        rs.getInt("najete_km"));
            }

            if (!nalezeno) {
                System.out.println("Zadaným kritériím neodpovídá žádné vozidlo.");
            }
        }
    }
}
void main() {

    Scanner scanner = new Scanner(System.in);

    Scanner sc = new Scanner(System.in);
    final String url = "jdbc:mysql://localhost:3306/pujcovna_aut";
    String username ="root";
    String password ="";

    try (Connection conn = DriverManager.getConnection(url, username, password);
         Statement stmt = conn.createStatement()) {
        System.out.println("Připojení k databázi bylo úspěšné!");

// Vytvoření nové tabulky


        stmt.executeUpdate(vytvorTabulku(conn));

        System.out.println("Tabulka 'oddeleni' byla vytvořena.");
    } catch (SQLException e) {
        System.err.println("Nastala chyba při práci s databází.");
        e.printStackTrace();
    }

    System.out.println("\n--- VYHLEDÁVÁNÍ VOZIDEL ---");
    System.out.println("(Pokud nechceš dané kritérium použít, stiskni jen Enter)");

    System.out.print("Zadej značku auta (např. Škoda): ");
    String znacka = scanner.nextLine();

    System.out.print("Zadej model auta (např. Octavia): ");
    String model = scanner.nextLine();

    System.out.print("Zadej maximální počet najetých km: ");
    String maxKmInput = scanner.nextLine();
    Integer maxKm = maxKmInput.isBlank() ? null : Integer.parseInt(maxKmInput.trim());

    vyhledejAuta(conn, znacka, model, maxKm);

}
