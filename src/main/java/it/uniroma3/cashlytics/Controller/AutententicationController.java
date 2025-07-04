package it.uniroma3.cashlytics.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import it.uniroma3.cashlytics.DTO.UserLoginDTO;
import it.uniroma3.cashlytics.DTO.UserRegistrationDTO;
import it.uniroma3.cashlytics.Exceptions.EmailAlreadyExistsException;
import it.uniroma3.cashlytics.Exceptions.UserAlreadyExistsException;
import it.uniroma3.cashlytics.Service.AutenticationService;
import jakarta.validation.Valid;

/**
 * Controller handling authentication-related requests including user
 * registration and login.
 * Maps HTTP requests to their respective view templates and services.
 */
@Controller
public class AutententicationController {

    @Autowired
    private AutenticationService autenticationService;

    /**
     * Displays the registration page.
     * Adds an empty userRegistrationDTO to the model if not already present.
     *
     * @param model The Spring MVC model
     * @return The name of the register template
     */
    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        if (!model.containsAttribute("userRegistrationDTO")) {
            model.addAttribute("userRegistrationDTO", new UserRegistrationDTO());
        }
        return "register";
    }

    /**
     * Handles the user registration form submission.
     * Validates input data, attempts to register the user, and handles possible
     * errors.
     *
     * @param userRegistrationDTO The DTO with registration information from the
     *                            form
     * @param bindingResult       Object containing validation results
     * @param redirectAttributes  Object for passing attributes through a redirect
     * @param model               The Spring MVC model
     * @return The view name to render (either back to register page or redirect to
     *         login)
     */
    @PostMapping("/register")
    public String registerUser(@Valid UserRegistrationDTO userRegistrationDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {
        // Check for validation errors from form inputs
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            // Attempt to register the user
            autenticationService.registerUser(userRegistrationDTO);

            // Add success message that will appear on the login page
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! You can now login.");

            return "redirect:/";
        } catch (UserAlreadyExistsException e) {
            // Handle username already taken scenario
            bindingResult.rejectValue("username", "error.username", e.getMessage());
            model.addAttribute("userRegistrationDTO", userRegistrationDTO);
            return "register";
        } catch (EmailAlreadyExistsException e) {
            // Handle email already registered scenario
            bindingResult.rejectValue("email", "error.email", e.getMessage());
            model.addAttribute("userRegistrationDTO", userRegistrationDTO);
            return "register";
        } catch (Exception e) {
            // Handle any other unexpected errors
            model.addAttribute("userRegistrationDTO", userRegistrationDTO);
            model.addAttribute("errorMessage", "An unexpected error occurred. Please try again.");
            return "register";
        }
    }

    @GetMapping("/login")
    public String GetLoginPage(Model model,
            @RequestParam(required = false) String error) {
        if (!model.containsAttribute("userLoginDTO")) {
            model.addAttribute("userLoginDTO", new UserLoginDTO());
        }
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid username or password.");
        }
        return "login";
    }

}
