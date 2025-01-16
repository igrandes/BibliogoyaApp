package dao.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "prestamos")
public class Prestamo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "libroID")
    private Libro libro;
    
    @ManyToOne
    @JoinColumn(name = "usuarioID")
    private Usuario usuario;
    
    @Column(name = "fechaprestamo")
    @Temporal(TemporalType.DATE)
    private LocalDate fechaPrestamo;
    
    @Column(name = "fechadevolucion")
    @Temporal(TemporalType.DATE)  // Puedes usar @Temporal para mapear LocalDate
    private LocalDate fechaDevolucion;

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Libro getLibro() { return libro; }
    public void setLibro(Libro libro) { this.libro = libro; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    
    public LocalDate getFechaPrestamo() { return fechaPrestamo; }
    public void setFechaPrestamo(LocalDate fechaPrestamo) { this.fechaPrestamo = fechaPrestamo; }
    
    public LocalDate getFechaDevolucion() { return fechaDevolucion; }
    public void setFechaDevolucion(LocalDate fechaDevolucion) { this.fechaDevolucion = fechaDevolucion; }
}
