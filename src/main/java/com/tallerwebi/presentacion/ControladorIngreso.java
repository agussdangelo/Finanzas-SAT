package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.enums.TipoEgreso;
import com.tallerwebi.dominio.enums.TipoIngreso;
import com.tallerwebi.dominio.excepcion.RecursoNoEncontrado;
import com.tallerwebi.dominio.models.Egreso;
import com.tallerwebi.dominio.models.Ingreso;
import com.tallerwebi.dominio.interfaces.ServicioIngreso;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.List;

@Controller
public class ControladorIngreso {

    private final ServicioIngreso ingresoService;

    public ControladorIngreso(ServicioIngreso ingresoService) {
        this.ingresoService = ingresoService;
    }

    // Mostrar la vista de ingreso
    @GetMapping("/ingreso")
    public String mostrarIngreso(Model model) {
        model.addAttribute("datosIngreso", new DatosIngreso());
        return "ingreso";
    }

    // Metodo para obtener todos los ingreso
    @GetMapping("api/ingreso")
    public ModelAndView verIngresos(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        Long userId = (Long) request.getSession().getAttribute("id");

        if (userId == null) {
            modelAndView.addObject("error", "No se pudo identificar al usuario.");
            modelAndView.setViewName("index");
            return modelAndView;
        }

        List<Ingreso> listaIngresos = ingresoService.getIngresosPorUserId(userId);
        modelAndView.setViewName("index"); // Asumiendo que "index" es la vista principal
        modelAndView.addObject("datosIngreso", listaIngresos);

        return modelAndView;
    }


    // Metodo para crear un nuevo egreso
    @PostMapping("/ingreso")
    public ModelAndView crearIngreso(
            @RequestParam("monto") Double monto,
            @RequestParam("fecha") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam("descripcion") String descripcion,
            @RequestParam("tipoIngreso") TipoIngreso tipoIngreso, HttpServletRequest request) {

        ModelAndView modelAndView = new ModelAndView();

        if (descripcion == null || descripcion.isEmpty()) {
            modelAndView.addObject("error", "La descripción no puede estar vacía");
            return modelAndView;
        }

        if (monto == null || monto <= 0) {
            modelAndView.addObject("error", "El monto no puede ser nulo o menor a cero");
            return modelAndView;
        }

        if (fecha == null) {
            modelAndView.addObject("error", "La fecha no puede ser nula");
            return modelAndView;
        }

        if (tipoIngreso == null) {
            modelAndView.addObject("error", "El tipo de ingreso no puede ser nulo");
            return modelAndView;
        }

        Long userId = (Long) request.getSession().getAttribute("id");
        if (userId == null) {
            modelAndView.addObject("error", "No se pudo identificar al usuario.");
            return modelAndView;
        }

        Ingreso ingreso = new Ingreso();
        ingreso.setMonto(monto);
        ingreso.setFecha(fecha);
        ingreso.setDescripcion(descripcion);
        ingreso.setTipoIngreso(tipoIngreso);
        ingreso.setUserId(userId);

        ingresoService.crearIngreso(ingreso, userId);

        modelAndView.setViewName("redirect:/ingreso");
        return modelAndView;
    }


    // Metodo para ver los detalles de un ingreso específico
    @GetMapping("/ingreso/detalle")
    public ModelAndView verDetalleIngreso(@RequestParam("monto") double monto, @RequestParam("id") int id) throws RecursoNoEncontrado {
        ModelAndView modelAndView = new ModelAndView();
        Ingreso ingreso = ingresoService.consultarIngreso(monto, id);  // Usar la instancia ingresoService

        if (ingreso != null) {
            modelAndView.setViewName("detalleIngreso");
            modelAndView.addObject("datosIngreso", ingreso);
        } else {
            throw new RecursoNoEncontrado("Ingreso no encontrado con monto: " + monto + " e id: " + id);
        }

        return modelAndView;
    }








}
