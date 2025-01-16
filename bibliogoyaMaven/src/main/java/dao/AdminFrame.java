package dao;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;

public class AdminFrame extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;

    public AdminFrame() {
        initializeFrame();
        setupComponents();
    }

    // Inicialización del frame principal
    private void initializeFrame() {
        setTitle("Biblioteca Goya - Panel de Administración");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    // Configuración de los componentes del frame
    private void setupComponents() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    // Creación del panel superior con el título
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180)); // Azul acero
        JLabel titleLabel = new JLabel("📚 Panel de Administración");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(titleLabel);
        return topPanel;
    }

    // Creación del panel central con botones de gestión
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 248, 255)); // Fondo celeste claro
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        addButton(centerPanel, gbc, "Gestión de Libros", 0, this::openBookManagement);
        addButton(centerPanel, gbc, "Gestión de Usuarios", 1, this::openUserManagement);
        addButton(centerPanel, gbc, "Gestión de Reservas", 2, this::openReservaManagement);
        addButton(centerPanel, gbc, "Gerador de informes", 3, this::reportGenerator);
        addButton(centerPanel, gbc, "Regresar", 4, () -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        return centerPanel;
    }

    // Creación del panel inferior con información de pie de página
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

    // Métodos para abrir las diferentes ventanas de gestión
    private void reportGenerator() {
        SwingUtilities.invokeLater(() -> {
            // new ReportGeneratorFrame().setVisible(true);
            dispose();
        });
    }
   
    private void openBookManagement() {
        SwingUtilities.invokeLater(() -> {
            new BookManagementFrame().setVisible(true);
            dispose();
        });
    }

    private void openUserManagement() {
        SwingUtilities.invokeLater(() -> {
            new UserManagementFrame().setVisible(true);
            dispose();
        });
    }

    private void openReservaManagement() {
        SwingUtilities.invokeLater(() -> {
            new ReservaManagementFrame().setVisible(true);
            dispose();
        });
    }
}