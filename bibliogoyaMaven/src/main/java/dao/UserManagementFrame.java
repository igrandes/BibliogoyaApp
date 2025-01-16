package dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.Serializable;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import dao.entity.Usuario;
import dao.util.HibernateUtil;

public class UserManagementFrame extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTable userTable;
    private DefaultTableModel tableModel;

    public UserManagementFrame() {
        initializeFrame();
        setupComponents();
        loadUsers();
    }

    private void initializeFrame() {
        setTitle("Gestión de Usuarios");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        JPanel topPanel = createTopPanel();
        setupUserTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("👥 Gestión de Usuarios", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);
        return topPanel;
    }

    private void setupUserTable() {
        String[] columns = {"ID", "Nombre", "Apellidos", "Email", "DNI", "Teléfono", "Rol"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        userTable = new JTable(tableModel);
        userTable.setFont(new Font("Arial", Font.PLAIN, 14));
        userTable.setRowHeight(20);
        userTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        userTable.setSelectionBackground(new Color(210, 228, 238));
    }


    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = createStyledButton("Añadir");
        JButton editButton = createStyledButton("Editar");
        JButton deleteButton = createStyledButton("Eliminar");
        JButton backButton = createStyledButton("Volver");

        addButton.addActionListener(e -> addUser());
        editButton.addActionListener(e -> editUser());
        deleteButton.addActionListener(e -> deleteUser());
        backButton.addActionListener(e -> {
            new AdminFrame().setVisible(true);
            dispose();
        });

        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(backButton);

        return buttonPanel;
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    private void loadUsers() {
        tableModel.setRowCount(0);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Usuario> usuarios = session.createQuery("FROM Usuario", Usuario.class).list();
            for (Usuario usuario : usuarios) {
                tableModel.addRow(new Object[]{
                    usuario.getId(),
                    usuario.getNombre(),
                    usuario.getApellidos(),
                    usuario.getEmail(),
                    usuario.getDni(),
                    usuario.getTelefono(),
                    usuario.getRol()
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al cargar los usuarios: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void addUser() {
        JTextField nombreField = new JTextField();
        JTextField apellidosField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField dniField = new JTextField();
        JTextField telefonoField = new JTextField();
        JComboBox<String> rolComboBox = new JComboBox<>(new String[]{"Usuario", "Administrador"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("Nombre:"));
        panel.add(nombreField);
        panel.add(new JLabel("Apellidos:"));
        panel.add(apellidosField);
        panel.add(new JLabel("Email:"));
        panel.add(emailField);
        panel.add(new JLabel("DNI:"));
        panel.add(dniField);
        panel.add(new JLabel("Teléfono:"));
        panel.add(telefonoField);
        panel.add(new JLabel("Rol:"));
        panel.add(rolComboBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Añadir Usuario",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                try {
                    Usuario nuevoUsuario = new Usuario();
                    nuevoUsuario.setNombre(nombreField.getText());
                    nuevoUsuario.setApellidos(apellidosField.getText());
                    nuevoUsuario.setEmail(emailField.getText());
                    nuevoUsuario.setDni(dniField.getText());
                    nuevoUsuario.setTelefono(telefonoField.getText());
                    nuevoUsuario.setRol((String) rolComboBox.getSelectedItem());

                    session.persist(nuevoUsuario);
                    transaction.commit();
                    loadUsers();
                    JOptionPane.showMessageDialog(this, "Usuario añadido con éxito");
                } catch (Exception e) {
                    transaction.rollback();
                    throw e;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al añadir el usuario: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void editUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para editar");
            return;
        }

        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Usuario usuario = session.get(Usuario.class, id);
                if (usuario != null) {
                    JTextField nombreField = new JTextField(usuario.getNombre());
                    JTextField apellidosField = new JTextField(usuario.getApellidos());
                    JTextField emailField = new JTextField(usuario.getEmail());
                    JTextField dniField = new JTextField(usuario.getDni());
                    JTextField telefonoField = new JTextField(usuario.getTelefono());
                    JComboBox<String> rolComboBox = new JComboBox<>(new String[]{"Usuario", "Administrador"});
                    rolComboBox.setSelectedItem(usuario.getRol());

                    JPanel panel = new JPanel(new GridLayout(0, 1));
                    panel.add(new JLabel("Nombre:"));
                    panel.add(nombreField);
                    panel.add(new JLabel("Apellidos:"));
                    panel.add(apellidosField);
                    panel.add(new JLabel("Email:"));
                    panel.add(emailField);
                    panel.add(new JLabel("DNI:"));
                    panel.add(dniField);
                    panel.add(new JLabel("Teléfono:"));
                    panel.add(telefonoField);
                    panel.add(new JLabel("Rol:"));
                    panel.add(rolComboBox);

                    int result = JOptionPane.showConfirmDialog(this, panel, "Editar Usuario",
                        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
                    if (result == JOptionPane.OK_OPTION) {
                        usuario.setNombre(nombreField.getText());
                        usuario.setApellidos(apellidosField.getText());
                        usuario.setEmail(emailField.getText());
                        usuario.setDni(dniField.getText());
                        usuario.setTelefono(telefonoField.getText());
                        usuario.setRol((String) rolComboBox.getSelectedItem());

                        session.merge(usuario);
                        transaction.commit();
                        loadUsers();
                        JOptionPane.showMessageDialog(this, "Usuario actualizado con éxito");
                    }
                }
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al editar el usuario: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void deleteUser() {
        int selectedRow = userTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario para eliminar");
            return;
        }

        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea eliminar este usuario?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                try {
                    Usuario usuario = session.get(Usuario.class, id);
                    if (usuario != null) {
                        session.remove(usuario);
                        transaction.commit();
                        loadUsers();
                        JOptionPane.showMessageDialog(this, "Usuario eliminado con éxito");
                    }
                } catch (Exception e) {
                    transaction.rollback();
                    throw e;
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar el usuario: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
      }
   }
   
