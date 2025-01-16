package dao;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import org.hibernate.Session;
import dao.entity.Usuario;
import dao.util.HibernateUtil;


public class UserFrame extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    
    // Constantes para personalización visual
    private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
    private static final Color BACKGROUND_COLOR = new Color(240, 248, 255);
    private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    private static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 16);

    public UserFrame() {
        initializeFrame();
        setupComponents();
    }

    /*Inicializa las propiedades básicas del frame*/
    private void initializeFrame() {
        setTitle("Biblioteca Goya - Panel de Usuario");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    /*Configura y añade todos los componentes al frame*/
    private void setupComponents() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    /*Crea el panel superior con el título*/
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(PRIMARY_COLOR);
        
        JLabel titleLabel = new JLabel("📚 Panel de Usuario");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(TITLE_FONT);
        topPanel.add(titleLabel);
        
        return topPanel;
    }

    /*Crea el panel central con los botones de acción*/
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Añadir botones con sus respectivas acciones
        addButton(centerPanel, gbc, "Ver Libros Disponibles", 0, this::openAvailableBooks);
        addButton(centerPanel, gbc, "Ver Mis Libros Prestados", 1, this::openBorrowedBooks);
        addButton(centerPanel, gbc, "Cerrar Sesión", 2, () -> {
            new LoginFrame().setVisible(true);
            this.dispose();
        });

        return centerPanel;
    }

    /*Crea el panel inferior con información de copyright*/
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(PRIMARY_COLOR);
        
        JLabel footer = new JLabel("© 2024 Biblioteca Goya");
        footer.setForeground(Color.WHITE);
        bottomPanel.add(footer);
        
        return bottomPanel;
    }

    /*Método utilitario para añadir botones al panel*/
    private void addButton(JPanel panel, GridBagConstraints gbc, String text, int y, Runnable action) {
        JButton button = createStyledButton(text);
        button.addActionListener(e -> action.run());
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(button, gbc);
    }

    //crea un botón con el estilo consistente de la aplicación
     
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(BUTTON_FONT);
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    /*
     Abre la ventana de libros disponibles
     Maneja las excepciones y muestra mensajes de error apropiados
     */
    private void openAvailableBooks() {
        SwingUtilities.invokeLater(() -> {
            try {
                new BookManagementFrame().setVisible(true);
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al abrir la lista de libros disponibles: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    /*
     Abre la ventana de libros prestados del usuario
     Requiere que usuarioID esté establecido
     */
    private void openBorrowedBooks() {
        SwingUtilities.invokeLater(() -> {
            try {
                if (SessionManager.getClienteId() == null) {
                    throw new IllegalStateException("ID de usuario no establecido");
                }
                new UserBorrowedBooks().setVisible(true);
                System.out.println("" + SessionManager.getClienteId());
                dispose();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error al abrir la lista de libros prestados: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}