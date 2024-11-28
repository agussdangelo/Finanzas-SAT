package com.tallerwebi.integracion;

import com.tallerwebi.integracion.config.HibernateTestConfig;
import com.tallerwebi.integracion.config.SpringWebTestConfig;
import com.tallerwebi.dominio.models.Deuda;
import com.tallerwebi.dominio.models.Usuario;
import com.tallerwebi.dominio.interfaces.ServicioDeuda;
import com.tallerwebi.dominio.interfaces.ServicioUsuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.text.IsEqualIgnoringCase.equalToIgnoringCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SpringWebTestConfig.class, HibernateTestConfig.class})
public class ControladorDeudaIntegTest {

    @Autowired
    private WebApplicationContext wac;
    private MockMvc mockMvc;

    @Autowired
    private ServicioDeuda servicioDeuda;

    @Autowired
    private ServicioUsuario servicioUsuario;

    @BeforeEach
    public void init() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    private Long registrarYObtenerIdUsuario(String username, String password) throws Exception {
        // Crear un usuario con el formulario de registro
        MvcResult result = mockMvc.perform(post("/registrarme")
                        .param("username", username)
                        .param("password", password)
                        .param("nombre", "Usuario de Prueba")
                        .param("saldo", "100.0"))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Ahora que el usuario está registrado, vamos a hacer login
        MvcResult loginResult = mockMvc.perform(post("/validar-login")
                        .param("username", username)
                        .param("password", password))
                .andExpect(status().is3xxRedirection())
                .andReturn();

        // Obtener el ID del usuario desde la sesión
        Long userId = (Long) loginResult.getRequest().getSession().getAttribute("id");
        return userId;
    }

    @Test
    public void deberiaEliminarDeuda() throws Exception {
        String username = "testuser2";
        String password = "testpassword2";

        // Registrar y obtener el ID del usuario
        Long userId = registrarYObtenerIdUsuario(username, password);

        // Crear una deuda para eliminarla
        Usuario usuario = servicioUsuario.obtenerUsuarioPorId(userId);
        Deuda deuda = new Deuda();
        deuda.setMonto(100.0);
        deuda.setDescripcion("Deuda para eliminar");
        deuda.setUsuario(usuario);
        servicioDeuda.agregarDeuda(deuda);

        // Verificar que la deuda fue creada
        MvcResult resultBeforeDelete = this.mockMvc.perform(get("/deudas")
                        .sessionAttr("id", userId)) // Pasamos el ID del usuario en la sesión
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndViewBeforeDelete = resultBeforeDelete.getModelAndView();
        assert modelAndViewBeforeDelete != null;
        assertThat(modelAndViewBeforeDelete.getViewName(), equalToIgnoringCase("deudas"));
        assertThat(modelAndViewBeforeDelete.getModel().containsKey("debo"), is(true));

        // Realizar una solicitud DELETE para eliminar la deuda
        MvcResult resultDelete = this.mockMvc.perform(delete("/deudas/{deudaId}", deuda.getId())
                        .sessionAttr("id", userId)) // Pasamos el ID del usuario en la sesión
                .andExpect(status().is3xxRedirection()) // Esperamos redirección
                .andReturn();

        // Verificar la redirección después de la eliminación
        assertThat(resultDelete.getResponse().getRedirectedUrl(), equalToIgnoringCase("/deudas"));

        // Verificar que la deuda ya no existe
        MvcResult resultAfterDelete = this.mockMvc.perform(get("/deudas")
                        .sessionAttr("id", userId)) // Pasamos el ID del usuario en la sesión
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndViewAfterDelete = resultAfterDelete.getModelAndView();
        assert modelAndViewAfterDelete != null;
        assertThat(modelAndViewAfterDelete.getViewName(), equalToIgnoringCase("deudas"));
        assertThat(modelAndViewAfterDelete.getModel().containsKey("debo"), is(true)); // Verificamos si la deuda ya no está en el modelo
    }
    
    @Test
    public void deberiaRetornarPaginaDeudasCuandoUsuarioEstaAutenticado() throws Exception {
        String username = "testuser";
        String password = "testpassword";

        // Registrar y obtener el ID del usuario
        Long userId = registrarYObtenerIdUsuario(username, password);

        // Crear una deuda para el usuario
        Usuario usuario = servicioUsuario.obtenerUsuarioPorId(userId);
        Deuda deuda = new Deuda();
        deuda.setMonto(100.0);
        deuda.setDescripcion("Deuda de prueba");
        deuda.setUsuario(usuario); // Asociar la deuda al usuario creado
        servicioDeuda.agregarDeuda(deuda);

        // Realizar una solicitud GET para acceder a la página de deudas
        MvcResult result = this.mockMvc.perform(get("/deudas")
                        .sessionAttr("id", userId))  // Pasamos el ID del usuario en la sesión
                .andExpect(status().isOk())
                .andReturn();

        ModelAndView modelAndView = result.getModelAndView();
        assert modelAndView != null;
        assertThat(modelAndView.getViewName(), equalToIgnoringCase("deudas"));
        assertThat(modelAndView.getModel().containsKey("debo"), is(true));
        assertThat(modelAndView.getModel().containsKey("medeben"), is(true));
    }

}