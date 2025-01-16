package dao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import dao.entity.Libro;
import dao.entity.Prestamo;
import dao.entity.Reserva;
import dao.entity.Usuario;
import dao.util.HibernateUtil;

public class BookManagementFrame extends JFrame implements Serializable {
	private static final long serialVersionUID = 1L;
	private JTable bookTable;
	private DefaultTableModel tableModel;
	private boolean showingUnavailableBooks = false;
	private JLabel titleLabel;
	public BookManagementFrame() {
		initializeFrame();
		setupComponents();
		loadBooks();
	}

	private void initializeFrame() {
		setTitle(SessionManager.isUser() ? "Libros Disponibles" : "Gestión de Libros");
		setSize(900, 500);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
	}

	private void setupComponents() {
		JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.setBackground(Color.WHITE);

		JPanel topPanel = createTopPanel();
		setupBookTable();
		JScrollPane scrollPane = new JScrollPane(bookTable);
		mainPanel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = createButtonPanel();
		mainPanel.add(buttonPanel, BorderLayout.SOUTH);

		add(topPanel, BorderLayout.NORTH);
		add(mainPanel, BorderLayout.CENTER);
	}

	private JPanel createTopPanel() {
		JPanel topPanel = new JPanel();
		topPanel.setBackground(new Color(70, 130, 180));
		titleLabel = new JLabel(SessionManager.isUser() ? "📚 Libros Disponibles" : "📚 Gestión de Libros",
				SwingConstants.CENTER);
		titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
		titleLabel.setForeground(Color.WHITE);
		topPanel.add(titleLabel);
		return topPanel;
	}

	private void setupBookTable() {
		String[] columns = { "ID", "Título", "Autor", "Género", "Disponibilidad" };
		tableModel = new DefaultTableModel(columns, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		bookTable = new JTable(tableModel);
		bookTable.setFont(new Font("Arial", Font.PLAIN, 14));
		bookTable.setRowHeight(20);
		bookTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
		bookTable.setSelectionBackground(new Color(210, 228, 238));
	}

	private JPanel createButtonPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		buttonPanel.setBackground(Color.WHITE);

		if (!SessionManager.isUser()) {
			JButton addButton = createStyledButton("Añadir");
			JButton editButton = createStyledButton("Editar");
			JButton deleteButton = createStyledButton("Eliminar");
			JButton backButton = createStyledButton("Volver");

			backButton.addActionListener(e -> {
				if (SessionManager.isUser()) {
					new UserFrame().setVisible(true);
				} else {
					new AdminFrame().setVisible(true);
				}
				dispose();
			});

			addButton.addActionListener(e -> addBook());
			editButton.addActionListener(e -> editBook());
			deleteButton.addActionListener(e -> deleteBook());

			buttonPanel.add(addButton);
			buttonPanel.add(editButton);
			buttonPanel.add(deleteButton);
			buttonPanel.add(backButton);
		} else {
			JButton backButton = createStyledButton("Volver");
			backButton.addActionListener(e -> {
				if (SessionManager.isUser()) {
					new UserFrame().setVisible(true);
				} else {
					new AdminFrame().setVisible(true);
				}
				dispose();
			});
		
			JButton reserveButton = createStyledButton("Reservar");
			reserveButton.addActionListener(e -> reserveBook());

			JButton unavailableBooksButton = createStyledButton("Ver libros no disponibles");
			unavailableBooksButton.addActionListener(e -> toggleBookView(unavailableBooksButton));

			buttonPanel.add(backButton);
			buttonPanel.add(reserveButton); // Agrega el botón "Reservar"
			buttonPanel.add(unavailableBooksButton); // Agrega el botón "Ver libros no disponibles"
		}

		return buttonPanel;
	}

	private void reserveBook() {
	    int selectedRow = bookTable.getSelectedRow();
	    if (selectedRow < 0) {
	        JOptionPane.showMessageDialog(this, "Por favor, seleccione un libro para reservar.");
	        return;
	    }

	    Long libroId = (Long) tableModel.getValueAt(selectedRow, 0);
	    String disponibilidad = (String) tableModel.getValueAt(selectedRow, 4);

	    if ("No disponible".equalsIgnoreCase(disponibilidad)) {
	        JOptionPane.showMessageDialog(this, "El libro seleccionado no está disponible para reservar.");
	        return;
	    }

	    Long clienteId = SessionManager.getClienteId();
	    if (clienteId == null) {
	        JOptionPane.showMessageDialog(this, "No se encontró un cliente autenticado en la sesión.");
	        return;
	    }

	    try (Session session = HibernateUtil.getSessionFactory().openSession()) {
	        Transaction transaction = session.beginTransaction();
	        try {
	            // Obtener el libro
	            Libro libro = session.get(Libro.class, libroId);
	            if (libro != null && libro.isDisponibilidad()) {
	                // Actualizar disponibilidad del libro
	                libro.setDisponibilidad(false);
	                session.merge(libro);

	                // Obtener el usuario autenticado
	                Usuario usuario = session.get(Usuario.class, clienteId);
	                if (usuario == null) {
	                    throw new RuntimeException("Usuario no encontrado con ID: " + clienteId);
	                }

	                // Crear nuevo registro en la tabla de préstamos
	                Prestamo nuevoPrestamo = new Prestamo();
	                nuevoPrestamo.setUsuario(usuario);
	                nuevoPrestamo.setLibro(libro);
	                nuevoPrestamo.setFechaPrestamo(LocalDate.now());
	                nuevoPrestamo.setFechaDevolucion(LocalDate.now().plusMonths(1));

	                session.persist(nuevoPrestamo);

	                // Crear nuevo registro en la tabla de reservas
	                Reserva nuevaReserva = new Reserva();
	                nuevaReserva.setUsuario(usuario);
	                nuevaReserva.setLibro(libro);
	                nuevaReserva.setFechaReserva(new Date());
	                nuevaReserva.setEstado(Reserva.EstadoReserva.Pendiente); // Estado inicial como "Pendiente"

	                session.persist(nuevaReserva);

	                transaction.commit();
	                loadBooks();
	                JOptionPane.showMessageDialog(this, "El libro ha sido reservado con éxito.");
	            } else {
	                JOptionPane.showMessageDialog(this, "No se pudo reservar el libro. Verifique la disponibilidad.");
	            }
	        } catch (Exception e) {
	            transaction.rollback();
	            JOptionPane.showMessageDialog(this, "Error al reservar el libro: " + e.getMessage(), "Error",
	                    JOptionPane.ERROR_MESSAGE);
	        }
	    }
	}


	private void loadUnavailableBooks() {
		tableModel.setRowCount(0); // Limpia la tabla antes de cargar nuevos datos
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			// Consulta para obtener solo los libros no disponibles
			String hql = "FROM Libro WHERE disponibilidad = false";
			List<Libro> librosNoDisponibles = session.createQuery(hql, Libro.class).list();
			for (Libro libro : librosNoDisponibles) {
				tableModel.addRow(new Object[] { libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getGenero(),
						libro.isDisponibilidad() ? "Disponible" : "No disponible" });
				
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error al cargar los libros no disponibles: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private JButton createStyledButton(String text) {
		JButton button = new JButton(text);
		button.setFont(new Font("Arial", Font.BOLD, 16));
		button.setBackground(new Color(70, 130, 180));
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		return button;
	}

	private void loadBooks() {
		tableModel.setRowCount(0);
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			String hql = SessionManager.isUser() ? "FROM Libro WHERE disponibilidad = true" : "FROM Libro";
			List<Libro> libros = session.createQuery(hql, Libro.class).list();
			for (Libro libro : libros) {
				tableModel.addRow(new Object[] { libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getGenero(),
						libro.isDisponibilidad() ? "Disponible" : "No disponible" });
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error al cargar los libros: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void addBook() {
		JTextField tituloField = new JTextField();
		JTextField autorField = new JTextField();
		JTextField generoField = new JTextField();
		JCheckBox disponibilidadCheck = new JCheckBox("Disponible", true);

		JPanel panel = new JPanel(new GridLayout(0, 1));
		panel.add(new JLabel("Título:"));
		panel.add(tituloField);
		panel.add(new JLabel("Autor:"));
		panel.add(autorField);
		panel.add(new JLabel("Género:"));
		panel.add(generoField);
		panel.add(disponibilidadCheck);

		int result = JOptionPane.showConfirmDialog(null, panel, "Añadir Libro", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				Transaction transaction = session.beginTransaction();
				try {
					Libro nuevoLibro = new Libro();
					nuevoLibro.setTitulo(tituloField.getText());
					nuevoLibro.setAutor(autorField.getText());
					nuevoLibro.setGenero(generoField.getText());
					nuevoLibro.setDisponibilidad(disponibilidadCheck.isSelected());

					session.persist(nuevoLibro);
					transaction.commit();
					loadBooks();
					JOptionPane.showMessageDialog(this, "Libro añadido con éxito");
				} catch (Exception e) {
					transaction.rollback();
					throw e;
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error al añadir el libro: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void editBook() {
		int selectedRow = bookTable.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione un libro para editar");
			return;
		}

		Long id = (Long) tableModel.getValueAt(selectedRow, 0);
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction transaction = session.beginTransaction();
			try {
				Libro libro = session.get(Libro.class, id);
				if (libro != null) {
					JTextField tituloField = new JTextField(libro.getTitulo());
					JTextField autorField = new JTextField(libro.getAutor());
					JTextField generoField = new JTextField(libro.getGenero());
					JCheckBox disponibilidadCheck = new JCheckBox("Disponible", libro.isDisponibilidad());

					JPanel panel = new JPanel(new GridLayout(0, 1));
					panel.add(new JLabel("Título:"));
					panel.add(tituloField);
					panel.add(new JLabel("Autor:"));
					panel.add(autorField);
					panel.add(new JLabel("Género:"));
					panel.add(generoField);
					panel.add(disponibilidadCheck);

					int result = JOptionPane.showConfirmDialog(null, panel, "Editar Libro",
							JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
					if (result == JOptionPane.OK_OPTION) {
						libro.setTitulo(tituloField.getText());
						libro.setAutor(autorField.getText());
						libro.setGenero(generoField.getText());
						libro.setDisponibilidad(disponibilidadCheck.isSelected());

						session.merge(libro);
						transaction.commit();
						loadBooks();
						JOptionPane.showMessageDialog(this, "Libro actualizado con éxito");
					}
				}
			} catch (Exception e) {
				transaction.rollback();
				throw e;
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "Error al editar el libro: " + e.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void deleteBook() {
		int selectedRow = bookTable.getSelectedRow();
		if (selectedRow < 0) {
			JOptionPane.showMessageDialog(this, "Por favor, seleccione un libro para eliminar");
			return;
		}

		Long id = (Long) tableModel.getValueAt(selectedRow, 0);
		int confirm = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea eliminar este libro?",
				"Confirmar eliminación", JOptionPane.YES_NO_OPTION);

		if (confirm == JOptionPane.YES_OPTION) {
			try (Session session = HibernateUtil.getSessionFactory().openSession()) {
				Transaction transaction = session.beginTransaction();
				try {
					Libro libro = session.get(Libro.class, id);
					if (libro != null) {
						session.remove(libro);
						transaction.commit();
						loadBooks();
						JOptionPane.showMessageDialog(this, "Libro eliminado con éxito");
					}
				} catch (Exception e) {
					transaction.rollback();
					throw e;
				}
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Error al eliminar el libro: " + e.getMessage(), "Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void toggleBookView(JButton button) {
		 if (showingUnavailableBooks) {
		        loadBooks(); 
		        button.setText("Ver libros no disponibles");
		        titleLabel.setText("Libros Disponibles");
		        showingUnavailableBooks = false;
		    } else {
		        loadUnavailableBooks();
		        button.setText("Ver solo libros disponibles");
		        titleLabel.setText("Libros No Disponibles");
		        showingUnavailableBooks = true;
		    }
	}
}