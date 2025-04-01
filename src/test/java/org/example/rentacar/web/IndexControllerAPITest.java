package org.example.rentacar.web;


import org.example.rentacar.user.service.UserService;
import org.example.rentacar.web.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.ModelAndView;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@WebMvcTest(IndexController.class)
public class IndexControllerAPITest {

    @MockitoBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private IndexController indexController;

    @Test
    void getIndexPage_ShouldReturnIndexPage() throws Exception {

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getRegisterPage_ShouldReturnRegisterPageWithRegisterRequest() throws Exception {

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    void processRegister_WithValidData_ShouldRedirectToLogin() throws Exception {

        doNothing().when(userService).register(any(RegisterRequest.class));

        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "testuser")
                        .param("password", "password123")
                        .param("email", "test@example.com")
                        .param("firstName", "Test")
                        .param("lastName", "User")
                        .param("phoneNumber", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        verify(userService).register(any(RegisterRequest.class));
    }
    @Test
    void processRegister_WithInvalidData_ShouldReturnRegisterView() throws Exception {

        RegisterRequest invalidRequest = new RegisterRequest();

        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.hasErrors()).thenReturn(true);

        String viewName = indexController.processRegister(invalidRequest, bindingResult);

        verify(userService, never()).register(any(RegisterRequest.class));
        assertEquals("register", viewName);
    }

    @Test
    void getLoginPage_WithoutErrorParam_ShouldReturnLoginViewWithLoginRequestObject() throws Exception {

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attributeDoesNotExist("errorMessage"));
    }

    @Test
    void getLoginPage_WithErrorParam_ShouldReturnLoginViewWithErrorMessage() throws Exception {

        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginRequest"))
                .andExpect(model().attributeExists("errorMessage"))
                .andExpect(model().attribute("errorMessage", "Incorrect username or password!"));
    }
}
