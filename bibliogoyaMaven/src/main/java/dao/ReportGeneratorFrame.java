package dao;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

public class ReportGeneratorFrame extends JFrame {

    private JButton btnBooksLoanedReport;
    private JButton btnTopUsersReport;
    private JButton btnPopularBooksReport;
    private JButton btnFinancialReport;
    private JButton btnGenreTrendsReport;
    private JButton btnBackToAdminFrame;

    public ReportGeneratorFrame() {
        setTitle("Generador de Informes");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Panel superior (estilo similar al AdminFrame)
        add(createTopPanel(), BorderLayout.NORTH);

        // Panel central con los botones
        JPanel centerPanel = createCenterPanel();
        add(centerPanel, BorderLayout.CENTER);

        // Panel inferior (estilo similar al AdminFrame)
        add(createBottomPanel(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // Creación del panel superior
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180)); // Azul acero
        JLabel titleLabel = new JLabel("📚 Generador de Informes");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel);
        return topPanel;
    }

    // Creación del panel central con los botones
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 248, 255)); // Fondo celeste claro
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addButton(centerPanel, gbc, "Informe de libros prestados", 0, this::generateBooksLoanedReport);
        addButton(centerPanel, gbc, "Informe de usuarios con más libros prestados", 1, this::generateTopUsersReport);
        addButton(centerPanel, gbc, "Informe de libros más populares", 2, this::generatePopularBooksReport);
        addButton(centerPanel, gbc, "Reporte financiero", 3, this::generateFinancialReport);
        addButton(centerPanel, gbc, "Tendencias por género literario", 4, this::generateGenreTrendsReport);
        addButton(centerPanel, gbc, "Volver al Panel de Administración", 5, this::goBackToAdminFrame);

        return centerPanel;
    }

    // Creación del panel inferior
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(70, 130, 180));
        JLabel footer = new JLabel("© 2024 Biblioteca Goya");
        footer.setForeground(Color.WHITE);
        bottomPanel.add(footer);
        return bottomPanel;
    }

    // Método para añadir botones al panel central
    private void addButton(JPanel panel, GridBagConstraints gbc, String text, int y, Runnable action) {
        JButton button = createStyledButton(text);
        button.addActionListener(e -> action.run());
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(button, gbc);
    }

    // Método para crear botones con estilo consistente
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    // Lógica de generación de informes
    private void generateBooksLoanedReport() {
        String query = "SELECT L.Titulo, P.FechaPrestamo, P.FechaDevolucion FROM Prestamos P JOIN Libros L ON P.LibroID = L.ID WHERE P.FechaPrestamo BETWEEN ? AND ?";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/Biblioteca", "root", "root");
             PreparedStatement ps = conn.prepareStatement(query)) {
            // Reemplaza estos valores con los de la interfaz o seleccionados por el usuario
            ps.setDate(1, Date.valueOf("2024-01-01"));
            ps.setDate(2, Date.valueOf("2024-12-31"));
            ResultSet rs = ps.executeQuery();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("InformeLibrosPrestados.pdf"));
            document.open();
            document.add(new Paragraph("Informe de Libros Prestados entre las Fechas"));
            while (rs.next()) {
                document.add(new Paragraph("Título: " + rs.getString("Titulo") + " | Fecha de Préstamo: " + rs.getDate("FechaPrestamo") + " | Fecha de Devolución: " + rs.getDate("FechaDevolucion")));
            }
            document.close();
            JOptionPane.showMessageDialog(this, "Informe de libros prestados generado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar el informe: " + e.getMessage());
        }
    }

    private void generateTopUsersReport() {
        String query = "SELECT U.Nombre, U.Apellidos, COUNT(P.ID) AS NumeroDePrestamos FROM Usuarios U JOIN Prestamos P ON U.ID = P.UsuarioID GROUP BY U.ID ORDER BY NumeroDePrestamos DESC LIMIT 10";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/Biblioteca", "root", "");
             PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("InformeUsuariosTop.pdf"));
            document.open();
            document.add(new Paragraph("Informe de Usuarios con Más Libros Prestados"));
            while (rs.next()) {
                document.add(new Paragraph("Nombre: " + rs.getString("Nombre") + " " + rs.getString("Apellidos") + " | Número de Préstamos: " + rs.getInt("NumeroDePrestamos")));
            }
            document.close();
            JOptionPane.showMessageDialog(this, "Informe de usuarios generado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar el informe: " + e.getMessage());
        }
    }

    private void generatePopularBooksReport() {
        String query = "SELECT L.Titulo, COUNT(P.ID) AS PrestamosRealizados FROM Prestamos P JOIN Libros L ON P.LibroID = L.ID GROUP BY L.ID ORDER BY PrestamosRealizados DESC LIMIT 10";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/Biblioteca", "root", "");
             PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("InformeLibrosPopulares.pdf"));
            document.open();
            document.add(new Paragraph("Informe de Libros Más Populares"));
            while (rs.next()) {
                document.add(new Paragraph("Título: " + rs.getString("Titulo") + " | Número de Préstamos: " + rs.getInt("PrestamosRealizados")));
            }
            document.close();
            JOptionPane.showMessageDialog(this, "Informe de libros populares generado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar el informe: " + e.getMessage());
        }
    }

    private void generateFinancialReport() {
        String query = "SELECT P.LibroID, SUM(P.MultaGenerada) AS TotalMultas FROM Prestamos P WHERE P.FechaDevolucion < P.FechaPrestamo GROUP BY P.LibroID";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/Biblioteca", "root", "");
             PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("ReporteFinanciero.pdf"));
            document.open();
            document.add(new Paragraph("Reporte Financiero de Multas por Devoluciones Tardías"));
            while (rs.next()) {
                document.add(new Paragraph("ID Libro: " + rs.getInt("LibroID") + " | Total Multas: " + rs.getDouble("TotalMultas")));
            }
            document.close();
            JOptionPane.showMessageDialog(this, "Reporte financiero generado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar el reporte: " + e.getMessage());
        }
    }

    private void generateGenreTrendsReport() {
        String query = "SELECT L.Genero, COUNT(P.ID) AS NumeroDePrestamos FROM Prestamos P JOIN Libros L ON P.LibroID = L.ID GROUP BY L.Genero ORDER BY NumeroDePrestamos DESC";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost/Biblioteca", "root", "");
             PreparedStatement ps = conn.prepareStatement(query)) {
            ResultSet rs = ps.executeQuery();
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream("TendenciasGeneroLiterario.pdf"));
            document.open();
            document.add(new Paragraph("Tendencias por Género Literario"));
            while (rs.next()) {
                document.add(new Paragraph("Género: " + rs.getString("Genero") + " | Número de Préstamos: " + rs.getInt("NumeroDePrestamos")));
            }
            document.close();
            JOptionPane.showMessageDialog(this, "Informe de tendencias por género generado correctamente.");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al generar el informe: " + e.getMessage());
        }
    }

    // Método para regresar al AdminFrame
    private void goBackToAdminFrame() {
        SwingUtilities.invokeLater(() -> {
            new AdminFrame().setVisible(true);
            dispose();
        });
    }

    public static void main(String[] args) {
        new ReportGeneratorFrame();
    }
}