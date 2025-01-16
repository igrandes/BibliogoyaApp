package dao;

import javax.swing.*;
import java.awt.*;
import java.io.Serializable;
import org.hibernate.Session;
import org.hibernate.query.Query;
import dao.entity.Usuario;
import dao.util.HibernateUtil;

public class LoginFrame extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTextField userField;
    private JPasswordField passField;
    private JButton loginButton;

    public LoginFrame() {
        setupLookAndFeel();
        initializeComponents();
        setupLayout();
        setupEventListeners();
        setVisible(true);
    }

    // Configuración del aspecto visual
    private void setupLookAndFeel() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inicialización de componentes
    private void initializeComponents() {
        setTitle("Biblioteca Goya - Inicio de Sesión");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        userField = new JTextField(15);
        passField = new JPasswordField(15);
        loginButton = createStyledButton("Iniciar Sesión");
    }

    // Configuración del diseño
    private void setupLayout() {
        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    // Creación del panel superior
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180));
        JLabel logoLabel = new JLabel("📚 Biblioteca Goya");
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 24));
        topPanel.add(logoLabel);
        return topPanel;
    }

    // Creación del panel central
    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(240, 248, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        addLabelAndField(centerPanel, gbc, "Usuario:", userField, 0);
        addLabelAndField(centerPanel, gbc, "Contraseña:", passField, 1);

        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = 2;
        centerPanel.add(loginButton, gbc);

        return centerPanel;
    }

    // Método auxiliar para añadir etiqueta y campo
    private void addLabelAndField(JPanel panel, GridBagConstraints gbc, String labelText, JComponent field, int y) {
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        gbc.gridx = 0;
        gbc.gridy = y;
        panel.add(label, gbc);
        gbc.gridx = 1;
        panel.add(field, gbc);
    }

    // Creación del panel inferior
    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(70, 130, 180));
        JLabel footer = new JLabel("© 2024 Biblioteca");
        footer.setForeground(Color.WHITE);
        bottomPanel.add(footer);
        return bottomPanel;
    }

    // Creación de botón estilizado
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    // Configuración de listeners de eventos
    private void setupEventListeners() {
        loginButton.addActionListener(e -> handleLogin());
    }

    // Manejo del proceso de inicio de sesión
    private void handleLogin() {
        String username = userField.getText();
        String password = new String(passField.getPassword());

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Usuario WHERE nombre = :username AND apellidos = :password";
            Query<Usuario> query = session.createQuery(hql, Usuario.class);
            query.setParameter("username", username);
            query.setParameter("password", password);
            Usuario usuario = query.uniqueResult();

            if (usuario != null) {
                Long usuarioID = usuario.getId();  // Obtener el ID del usuario

                // Si es un administrador
                if ("Administrador".equals(usuario.getRol())) {
                    new AdminFrame().setVisible(true);
                    SessionManager.setIsUser(false);
                    SessionManager.setClienteId(usuarioID);
                } else {
                    // Crear el UserFrame y pasarle el usuarioID
                    UserFrame userFrame = new UserFrame();
                    SessionManager.setClienteId(usuarioID);
                    SessionManager.setIsUser(true);
                    userFrame.setVisible(true);
                }

                // Cerrar la ventana de login
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                    "Usuario o contraseña incorrectos",
                    "Error de inicio de sesión",
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error al conectar con la base de datos: " + ex.getMessage(),
                "Error de base de datos",
                JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame());
    }
}