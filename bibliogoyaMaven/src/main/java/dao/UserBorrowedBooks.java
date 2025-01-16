package dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.io.Serializable;
import org.hibernate.Session;
import org.hibernate.query.Query;
import dao.entity.Libro;
import dao.entity.Prestamo;
import dao.util.HibernateUtil;

/**
 * Frame para mostrar los libros prestados a un usuario específico. Esta clase
 * proporciona una vista tabular de: - Los libros actualmente prestados al
 * usuario - Información detallada de cada préstamo
 */
public class UserBorrowedBooks extends JFrame implements Serializable {
	private static final long serialVersionUID = 1L;

// Componentes de la UI
	private JTable reservedBooksTable;
	private DefaultTableModel tableModel;

// Constantes visuales
	private static final Color PRIMARY_COLOR = new Color(70, 130, 180);
	private static final Color BACKGROUND_COLOR = Color.WHITE;
	private static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
	private static final Font TABLE_FONT = new Font("Arial", Font.PLAIN, 14);
	private static final Font TABLE_HEADER_FONT = new Font("Arial", Font.BOLD, 14);

// Constructor que requiere ID de usuario

	public UserBorrowedBooks() {
		initializeFrame();
		setupComponents();
		loadBorrowedBooks();
	}

// Inicializa las propiedades básicas del frame
	private void initializeFrame() {
		setTitle("Libros Prestados");
		setSize(800, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
	}

// Configura y añade todos los componentes al frame
	private void setupComponents() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.setBackground(BACKGROUND_COLOR);

		mainPanel.add(createTopPanel(), BorderLayout.NORTH);

		setupBorrowedBooksTable();
		JScrollPane scrollPane = new JScrollPane(reservedBooksTable);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

		add(mainPanel);
	}

// Crea el panel superior con el título

	private JPanel createTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setBackground(PRIMARY_COLOR);

		JLabel titleLabel = new JLabel("Libros Prestados", SwingConstants.CENTER);
		titleLabel.setFont(TITLE_FONT);
		titleLabel.setForeground(Color.WHITE);
		topPanel.add(titleLabel);

		return topPanel;
	}

// Configura la tabla de libros prestados

	private void setupBorrowedBooksTable() {
		String[] columns = { "ID", "Título", "Autor", "Género", "Fecha de Préstamo" };
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		reservedBooksTable = new JTable(tableModel);
		reservedBooksTable.setFont(TABLE_FONT);
		reservedBooksTable.setRowHeight(20);
		reservedBooksTable.getTableHeader().setFont(TABLE_HEADER_FONT);
		reservedBooksTable.setSelectionBackground(new Color(210, 228, 238));
	}

// Crea el panel de botones inferior
	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setBackground(BACKGROUND_COLOR);

		JButton backButton = createStyledButton("Volver");
		backButton.addActionListener(e -> {
			new UserFrame().setVisible(true);
			dispose();
		});

		JButton devolver = createStyledButton("Devolver");
		devolver.addActionListener(e -> devolverLibro());
		buttonPanel.add(devolver);

		buttonPanel.add(backButton);

		return buttonPanel;
	}

// Crea un botón con el estilo consistente de la aplicación
	private JButton createStyledButton(String text) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 16));
		button.setBackground(PRIMARY_COLOR);
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		return button;
	}

	/*
	 * Carga los libros prestados desde la base de datos Utiliza Hibernate para
	 * obtener los préstamos del usuario
	 */
	private void loadBorrowedBooks() {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			String hql = "SELECT p FROM Prestamo p JOIN FETCH p.libro l WHERE p.usuario.id = :usuarioId";
			Query<Prestamo> query = session.createQuery(hql, Prestamo.class);
			query.setParameter("usuarioId", SessionManager.getClienteId());
			List<Prestamo> prestamos = query.list();

			tableModel.setRowCount(0); // Limpiar tabla existente

			for (Prestamo prestamo : prestamos) {
				Libro libro = prestamo.getLibro();
				tableModel.addRow(new Object[] { libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getGenero(),
						prestamo.getFechaPrestamo() });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error al recuperar los libros prestados: " + e.getMessage(),
					"Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}

	private void devolverLibro() {
		int selectedRow = reservedBooksTable.getSelectedRow();

		if (selectedRow == -1) {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione un libro para devolver.", "Selección requerida",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		Long libroId = (Long) tableModel.getValueAt(selectedRow, 0); // ID del libro

		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			session.beginTransaction();

// Buscar el préstamo correspondiente
			String hql = "SELECT p FROM Prestamo p WHERE p.libro.id = :libroId AND p.usuario.id = :usuarioId";
			Query<Prestamo> query = session.createQuery(hql, Prestamo.class);
			query.setParameter("libroId", libroId);
			query.setParameter("usuarioId", SessionManager.getClienteId());
			Prestamo prestamo = query.uniqueResult();

			if (prestamo != null) {
// Eliminar el préstamo
				session.remove(prestamo);

// Actualizar la disponibilidad del libro
				Libro libro = prestamo.getLibro();
				libro.setDisponibilidad(true);
				session.update(libro);

				session.getTransaction().commit();

// Quitar la fila de la tabla
				tableModel.removeRow(selectedRow);

				JOptionPane.showMessageDialog(this, "El libro ha sido devuelto con éxito y está disponible nuevamente.",
						"Operación exitosa", JOptionPane.INFORMATION_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(this, "No se encontró el préstamo correspondiente.", "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Error al devolver el libro: " + ex.getMessage(),
					"Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
	}

}