package com.concesionario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.concesionario.dto.UsuarioDTO;
import com.concesionario.dto.VehiculoDTO;
import com.concesionario.model.Cita;
import com.concesionario.model.Usuario;
import com.concesionario.model.Vehiculo;
import com.concesionario.repository.CitaRepository;

// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageRequest;
// import org.springframework.data.domain.Pageable;
// import org.springframework.data.domain.Sort;

@Service
public class CitaService {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private com.concesionario.repository.TrabajadorRepository trabajadorRepository;

    public Cita guardarCita(Cita cita) {
        return citaRepository.save(cita);
    }

    public List<Cita> obtenerTodasLasCitas() {
        return citaRepository.findAllByOrderByIdDesc();
    }

    public List<Cita> obtenerCitasPorTipo(String tipo) {
        return citaRepository.findByTipo(tipo);
    }

    public List<Cita> obtenerCitasPendientes() {
        return citaRepository.findByAtendidaFalseOrderByIdDesc();
    }

    public List<Cita> obtenerCitasPorUsuarioId(String usuarioId) {
        return citaRepository.findByUsuarioIdOrderByFechaCreacionDesc(usuarioId);
    }

    public Cita obtenerCitaPorId(String id) {
        return citaRepository.findById(id).orElse(null);
    }

    // public Page<Cita> obtenerCitasPaginadas(int page, int size){
    // Pageable pageable = PageRequest.of(page, size,
    // Sort.by("fechaCreacion").descending());
    // return citaRepository.findAllByOrderByFechaCreacionDesc(pageable);
    // }

    public void guardarCitaSimple(Cita cita, Usuario usuario, Vehiculo vehiculo) {
        prepararCitaEmbedding(cita, usuario, vehiculo);
        // Lógica específica si la hubiera, en este caso es idéntica
        citaRepository.save(cita);
    }

    public void crearCitaConEmbedding(Cita cita, Usuario usuario, Vehiculo vehiculo) {
        prepararCitaEmbedding(cita, usuario, vehiculo);
        // Lógica específica si la hubiera
        citaRepository.save(cita);
    }

    private void prepararCitaEmbedding(Cita cita, Usuario usuario, Vehiculo vehiculo) {
        cita.setCedula(usuario.getIdentificacionUser());
        cita.setCorreoElectronico(usuario.getCorreoUser());

        // Crear DTOs para embedding
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(usuario.getId());
        usuarioDTO.setNombre(usuario.getNombreUser());
        usuarioDTO.setApellido(usuario.getApellidoUser());
        usuarioDTO.setCorreo(usuario.getCorreoUser());
        usuarioDTO.setIdentificacion(usuario.getIdentificacionUser());

        VehiculoDTO vehiculoDTO = new VehiculoDTO();
        if (vehiculo != null) {
            vehiculoDTO.setId(vehiculo.getId());
            vehiculoDTO.setMarca(vehiculo.getMarca());
            vehiculoDTO.setModelo(vehiculo.getModelo());
            vehiculoDTO.setPrecio(vehiculo.getPrecio());
            vehiculoDTO.setCategoria(vehiculo.getCategoria());
            // vehiculoDTO.setColores(vehiculo.getColores());
        }

        // Establece relaciones embebidas
        cita.setUsuario(usuarioDTO);
        cita.setVehiculo(vehiculoDTO);
        cita.setFechaCreacion(LocalDateTime.now());
        cita.setAtendida(false);
        cita.setLeida(false);
        cita.setEstado("Pendiente");

        cita.setNombres(null);
        cita.setApellidos(null);
        cita.setCedula(null);
        cita.setCorreoElectronico(null);
        cita.setComentario(null); // Unificado
        // cita.setTelefono(null);
        cita.setModelo(null);
        cita.setVehiculoId(null);
        cita.setNombreVehiculo(null);
    }

    public long contarTodasLasCitas() {
        return citaRepository.count();
    }

    public boolean isHoraDisponible(String trabajadorId, String fechaCita, String horaCita) {
        return citaRepository.findByTrabajadorIdAndFechaCitaAndHoraCita(trabajadorId, fechaCita, horaCita).isEmpty();
    }

    public List<String> getHorasOcupadas(String trabajadorId, String fechaCita) {
        List<Cita> citas = citaRepository.findByTrabajadorIdAndFechaCita(trabajadorId, fechaCita);
        return citas.stream().map(Cita::getHoraCita).collect(Collectors.toList());
    }

    public String validarRangoDisponible(String trabajadorId, String citaId, String fecha, String horaInicio,
            String horaFin) {
        // Validar si es día y horario laboral del Trabajador
        com.concesionario.model.Trabajador trabajador = trabajadorRepository.findById(trabajadorId).orElse(null);
        if (trabajador == null) {
            return "Error: Trabajador no encontrado.";
        }

        try {
            java.time.LocalDate localDate = java.time.LocalDate.parse(fecha);
            if (localDate.isBefore(java.time.LocalDate.now())) {
                return "Error: No se pueden asignar citas en fechas pasadas.";
            }

            java.time.DayOfWeek dayOfWeek = localDate.getDayOfWeek();
            String diaEnEspanol = convertirDiaEspanol(dayOfWeek);
            java.util.List<String> diasTrabajo = trabajador.getDiasTrabajo();

            // Validar dia de trabajo (tolerancia a mayúsculas y acentos simples en
            // "Miércoles", "Sábado")
            if (diasTrabajo == null)
                return "Error: Sin días laborales.";
            boolean diaValido = false;
            for (String d : diasTrabajo) {
                if (quitarTildes(d.toUpperCase()).equals(quitarTildes(diaEnEspanol.toUpperCase()))) {
                    diaValido = true;
                    break;
                }
            }
            if (!diaValido) {
                return "Error: El asesor no trabaja un día " + diaEnEspanol + ". (Él trabaja: "
                        + String.join(", ", diasTrabajo) + ")";
            }

            // Validar limites de horas de trabajo
            java.time.LocalTime inicioCita = java.time.LocalTime.parse(horaInicio);
            java.time.LocalTime finCita = java.time.LocalTime.parse(horaFin);
            java.time.LocalTime inicioTrabajo = trabajador.getHoraInicioTrabajo();
            java.time.LocalTime finTrabajo = trabajador.getHoraFinTrabajo();

            if (!inicioCita.isBefore(finCita)) {
                return "Error: Hora inválida (Inicio debe ser antes que Fin).";
            }

            if (inicioCita.isBefore(inicioTrabajo) || finCita.isAfter(finTrabajo)) {
                return "Error: Fuera de jornada laboral del asesor (" + inicioTrabajo.toString() + " a "
                        + finTrabajo.toString() + ").";
            }

            // Validar solapamiento con citas existentes
            List<Cita> citasDelDia = citaRepository.findByTrabajadorIdAndFechaCita(trabajadorId, fecha);
            for (Cita cita : citasDelDia) {
                if (citaId != null && citaId.equals(cita.getId()))
                    continue;

                if (cita.getHoraCita() != null && cita.getHoraFinCita() != null) {
                    try {
                        java.time.LocalTime inicioExistente = java.time.LocalTime.parse(cita.getHoraCita());
                        java.time.LocalTime finExistente = java.time.LocalTime.parse(cita.getHoraFinCita());

                        if (inicioCita.isBefore(finExistente) && inicioExistente.isBefore(finCita)) {
                            return "Error: Solapamiento con otra cita en (" + cita.getHoraCita() + " - "
                                    + cita.getHoraFinCita() + ").";
                        }
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        } catch (Exception e) {
            return "Error: Formato de fecha (YYYY-MM-DD) o de hora (HH:mm) inválido.";
        }
        return "OK";
    }

    private String convertirDiaEspanol(java.time.DayOfWeek dia) {
        switch (dia) {
            case MONDAY:
                return "LUNES";
            case TUESDAY:
                return "MARTES";
            case WEDNESDAY:
                return "MIERCOLES";
            case THURSDAY:
                return "JUEVES";
            case FRIDAY:
                return "VIERNES";
            case SATURDAY:
                return "SABADO";
            case SUNDAY:
                return "DOMINGO";
            default:
                return "";
        }
    }

    private String quitarTildes(String input) {
        if (input == null)
            return null;
        return java.text.Normalizer.normalize(input, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}
