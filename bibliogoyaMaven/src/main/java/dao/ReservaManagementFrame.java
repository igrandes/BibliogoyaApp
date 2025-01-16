package dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.Serializable;
import java.util.List;
import java.util.Date;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import dao.entity.Reserva;
import dao.entity.Libro;
import dao.entity.Prestamo;
import dao.entity.Usuario;
import dao.util.HibernateUtil;

public class ReservaManagementFrame extends JFrame implements Serializable {
    private static final long serialVersionUID = 1L;
    private JTable reservaTable;
    private DefaultTableModel tableModel;

    public ReservaManagementFrame() {
        initializeFrame();
        setupComponents();
        loadReservas();
    }

    // Inicialización del frame
    private void initializeFrame() {
        setTitle("Gestión de Reservas");
        setSize(900, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
    }

    // Configuración de los componentes del frame
    private void setupComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(Color.WHITE);

        JPanel topPanel = createTopPanel();
        setupReservaTable();
        JScrollPane scrollPane = new JScrollPane(reservaTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);
    }

    // Creación del panel superior
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70, 130, 180));
        JLabel titleLabel = new JLabel("📚 Gestión de Reservas", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        topPanel.add(titleLabel);
        return topPanel;
    }

    // Configuración de la tabla de reservas
    private void setupReservaTable() {
        String[] columns = {"ID", "Libro", "Autor", "Fecha Reserva", "Estado", "Reservado por"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        reservaTable = new JTable(tableModel);
        reservaTable.setFont(new Font("Arial", Font.PLAIN, 14));
        reservaTable.setRowHeight(20);
        reservaTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
        reservaTable.setSelectionBackground(new Color(210, 228, 238));
    }

    // Creación del panel de botones
    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setBackground(Color.WHITE);

        JButton addButton = createStyledButton("Añadir");
        JButton editButton = createStyledButton("Editar");
        JButton deleteButton = createStyledButton("Eliminar");
        JButton backButton = createStyledButton("Volver");

        addButton.addActionListener(e -> addReserva());
        editButton.addActionListener(e -> editReserva());
        deleteButton.addActionListener(e -> deleteReserva());
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

    // Método para crear botones con estilo consistente
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setBackground(new Color(70, 130, 180));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        return button;
    }

    // Carga de reservas desde la base de datos
    private void loadReservas() {
        tableModel.setRowCount(0);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            // Consulta HQL para incluir el usuario asociado
            String hql = "SELECT p FROM Prestamo p JOIN FETCH p.libro l JOIN FETCH p.usuario u";
            Query<Prestamo> query = session.createQuery(hql, Prestamo.class);
            List<Prestamo> prestamos = query.list();

            tableModel.setRowCount(0); // Limpiar tabla existente
            
            for (Prestamo prestamo : prestamos) {
                Libro libro = prestamo.getLibro();
                Usuario usuario = prestamo.getUsuario(); // Obtener el usuario del préstamo
                tableModel.addRow(new Object[]{
                    libro.getId(),
                    libro.getTitulo(),
                    libro.getAutor(),
                    libro.getGenero(),
                    prestamo.getFechaPrestamo(),
                    usuario.getNombre() // Incluir el nombre del usuario en la tabla
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error al recuperar los libros prestados: " + e.getMessage(),
                "Error de Base de Datos", 
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }


    // Método para añadir una nueva reserva
    private void addReserva() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Libro> libros = session.createQuery("FROM Libro WHERE disponibilidad = true", Libro.class).list();
            List<Usuario> usuarios = session.createQuery("FROM Usuario", Usuario.class).list();

            if (libros.isEmpty() || usuarios.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay libros disponibles o usuarios registrados");
                return;
            }

            Libro libroSeleccionado = (Libro) JOptionPane.showInputDialog(this,
                "Seleccione un libro:", "Añadir Reserva",
                JOptionPane.QUESTION_MESSAGE, null, libros.toArray(), libros.get(0));

            if (libroSeleccionado != null) {
                Usuario usuarioSeleccionado = (Usuario) JOptionPane.showInputDialog(this,
                    "Seleccione un usuario:", "Añadir Reserva",
                    JOptionPane.QUESTION_MESSAGE, null, usuarios.toArray(), usuarios.get(0));

                if (usuarioSeleccionado != null) {
                    Transaction transaction = session.beginTransaction();
                    try {
                        Reserva nuevaReserva = new Reserva();
                        nuevaReserva.setLibro(libroSeleccionado);
                        nuevaReserva.setUsuario(usuarioSeleccionado);
                        nuevaReserva.setFechaReserva(new Date());
                        nuevaReserva.setEstado(Reserva.EstadoReserva.Pendiente);

                        session.persist(nuevaReserva);
                        transaction.commit();

                        loadReservas();
                        JOptionPane.showMessageDialog(this, "Reserva añadida con éxito");
                    } catch (Exception e) {
                        transaction.rollback();
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al añadir la reserva: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para editar una reserva existente
    private void editReserva() {
        int selectedRow = reservaTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una reserva para editar");
            return;
        }

        Long id = (Long) tableModel.getValueAt(selectedRow, 0);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                Reserva reserva = session.get(Reserva.class, id);
                if (reserva != null) {
                    String[] estados = {"Pendiente", "Completada", "Cancelada"};
                    String estadoSeleccionado = (String) JOptionPane.showInputDialog(this,
                        "Seleccione el nuevo estado:", "Editar Reserva",
                        JOptionPane.QUESTION_MESSAGE, null,
                        estados, reserva.getEstado().toString());

                    if (estadoSeleccionado != null) {
                        reserva.setEstado(Reserva.EstadoReserva.valueOf(estadoSeleccionado));
                        session.merge(reserva);
                        transaction.commit();
                        loadReservas();
                        JOptionPane.showMessageDialog(this, "Reserva actualizada con éxito");
                    }
                }
            } catch (Exception e) {
                transaction.rollback();
                throw e;
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al editar la reserva: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Método para eliminar una reserva
    private void deleteReserva() {
        int selectedRow = reservaTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un préstamo para eliminar");
            return;
        }

        Long id;
        try {
            // Obtén el ID del préstamo seleccionado en la tabla
            id = Long.valueOf(tableModel.getValueAt(selectedRow, 0).toString());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al obtener el ID del préstamo seleccionado: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Está seguro de que desea eliminar este préstamo?",
            "Confirmar eliminación",
            JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction transaction = session.beginTransaction();
                try {		
                    // Busca el préstamo en la base de datos usando su ID
                    Prestamo prestamo = session.get(Prestamo.class, id);
                    if (prestamo != null) {
                        session.remove(prestamo); // Elimina el préstamo
                        transaction.commit();
                        loadReservas(); // Recarga la tabla de datos
                        JOptionPane.showMessageDialog(this, "Préstamo eliminado con éxito");
                    } else {
                        JOptionPane.showMessageDialog(this, "El préstamo no fue encontrado en la base de datos");
                    }
                } catch (Exception e) {
                    transaction.rollback();
                    JOptionPane.showMessageDialog(this, "Error al eliminar el préstamo: " + e.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error al eliminar el préstamo: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
    }

}