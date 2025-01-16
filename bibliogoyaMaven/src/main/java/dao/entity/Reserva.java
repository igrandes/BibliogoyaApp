package dao.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import java.util.Date;

@Entity
@Table(name = "reservas")
public class Reserva {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "LibroID")
    private Libro libro;

    @ManyToOne
    @JoinColumn(name = "UsuarioID")
    private Usuario usuario;

    @Column(name = "FechaReserva")
    @Temporal(TemporalType.DATE)
    private Date fechaReserva;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado")
    private EstadoReserva estado;

    public enum EstadoReserva {
        Pendiente, Completada, Cancelada
    }

    // Constructors
    public Reserva() {}

    public Reserva(Libro libro, Usuario usuario, Date fechaReserva, EstadoReserva estado) {
        this.libro = libro;
        this.usuario = usuario;
        this.fechaReserva = fechaReserva;
        this.estado = estado;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Libro getLibro() {
        return libro;
    }

    public void setLibro(Libro libro) {
        this.libro = libro;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Date getFechaReserva() {
        return fechaReserva;
    }

    public void setFechaReserva(Date fechaReserva) {
        this.fechaReserva = fechaReserva;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Reserva{" +
                "id=" + id +
                ", libro=" + libro +
                ", usuario=" + usuario +
                ", fechaReserva=" + fechaReserva +
                ", estado=" + estado +
                '}';
    }
}