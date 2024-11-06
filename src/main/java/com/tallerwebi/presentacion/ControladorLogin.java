package com.tallerwebi.presentacion;

import com.tallerwebi.dominio.interfaces.ServicioEgreso;
import com.tallerwebi.dominio.interfaces.ServicioIngreso;
import com.tallerwebi.dominio.interfaces.ServicioLogin;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import com.tallerwebi.dominio.models.Egreso;
import com.tallerwebi.dominio.models.Ingreso;
import com.tallerwebi.dominio.models.Usuario;
import com.tallerwebi.dominio.excepcion.UsuarioExistente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class ControladorLogin {

    private ServicioLogin servicioLogin;
    private ServicioEgreso servicioEgreso;
    private ServicioIngreso servicioIngreso;
    private ServicioUsuario servicioUsuario;

    @Autowired
    public ControladorLogin(ServicioLogin servicioLogin, ServicioEgreso servicioEgreso, ServicioIngreso servicioIngreso, ServicioUsuario servicioUsuario){
        this.servicioLogin = servicioLogin;
        this.servicioEgreso = servicioEgreso;
        this.servicioIngreso = servicioIngreso;
        this.servicioUsuario = servicioUsuario;
    }

    @RequestMapping("/login")
    public ModelAndView irALogin() {

        ModelMap modelo = new ModelMap();
        modelo.put("datosLogin", new DatosLogin());
        return new ModelAndView("login", modelo);
    }
    @RequestMapping(path = "/validar-login", method = RequestMethod.POST)
    public ModelAndView validarLogin(@ModelAttribute("datosLogin") DatosLogin datosLogin, HttpServletRequest request) {
        ModelMap model = new ModelMap();

        Usuario usuarioBuscado = servicioLogin.consultarUsuario(datosLogin.getUsername(), datosLogin.getPassword());
        if (usuarioBuscado != null) {
            request.getSession().setAttribute("ROL", usuarioBuscado.getRol());
            request.getSession().setAttribute("id", usuarioBuscado.getId());
            return new ModelAndView("redirect:/index");
        }
        else {
            model.put("error", "Usuario o clave incorrecta");
        }
        return new ModelAndView("login", model);
    }

    @RequestMapping(path = "/registrarme", method = RequestMethod.POST)
    public ModelAndView registrarme(@ModelAttribute("usuario") Usuario usuario) {
        ModelMap model = new ModelMap();
        try{
            servicioLogin.registrar(usuario);
        } catch (UsuarioExistente e){
            model.put("error", "El usuario ya existe");
            return new ModelAndView("nuevo-usuario", model);
        } catch (Exception e){
            model.put("error", "Error al registrar el nuevo usuario");
            return new ModelAndView("nuevo-usuario", model);
        }
        return new ModelAndView("redirect:/login");
    }

    @RequestMapping(path = "/nuevo-usuario", method = RequestMethod.GET)
    public ModelAndView nuevoUsuario() {
        ModelMap model = new ModelMap();
        model.put("usuario", new Usuario());
        return new ModelAndView("nuevo-usuario", model);
    }

    @RequestMapping(path = "/index", method = RequestMethod.GET)
    public ModelAndView irAHome(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("index");

        Long userId = (Long) request.getSession().getAttribute("id");

        if (userId == null) {
            modelAndView.setViewName("redirect:/login");
            modelAndView.addObject("error", "Debe iniciar sesión para acceder a esta página.");
            return modelAndView;
        }

        Usuario usuario = servicioUsuario.obtenerUsuarioPorId(userId);
        Double saldo = (usuario != null) ? usuario.getSaldo() : 0.0;

        modelAndView.addObject("saldo", saldo);
        modelAndView.addObject("egresos", servicioEgreso.getAllEgresos());
        modelAndView.addObject("ingresos", servicioIngreso.getAllIngresos());

        return modelAndView;
    }


    @RequestMapping(path = "/", method = RequestMethod.GET)
    public ModelAndView inicio() {
        return new ModelAndView("redirect:/login");
    }
}

