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
import it.uniroma3.cashlytics.Service.AuthenticationService;
import jakarta.validation.Valid;

/**
 * Controller handling authentication-related requests including user
 * registration and login (both form and OAuth2).
 */
@Controller
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    /**
     * Displays the registration page.
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
            authenticationService.registerUser(userRegistrationDTO);

            // Add success message that will appear on the login page
            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration successful! You can now login.");

            return "redirect:/login";
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
    public String getLoginPage(Model model,
            @RequestParam(required = false) String error) {
        
        if (!model.containsAttribute("userLoginDTO")) {
            model.addAttribute("userLoginDTO", new UserLoginDTO());
        }
        
        if (error != null) {
            switch (error) {
                case "oauth2":
                    model.addAttribute("errorMessage", "OAuth2 authentication failed. Please try again.");
                    break;
                case "true":
                default:
                    model.addAttribute("errorMessage", "Invalid email or password.");
                    break;
            }
        }
        
        return "login";
    }
}