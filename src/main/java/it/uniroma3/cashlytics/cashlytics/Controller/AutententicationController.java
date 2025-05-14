package it.uniroma3.cashlytics.cashlytics.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;

import it.uniroma3.cashlytics.cashlytics.DTO.userRegistrationDTO;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;



@Controller
public class AutententicationController {

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        if(!model.containsAttribute("userRegistrationDTO")) {
            model.addAttribute("userRegistrationDTO", new userRegistrationDTO());
        }
        return "register";
    }


    /*
      da implementare
    */ 
    @PostMapping("path")
    public String registerUser(@Valid @RequestBody userRegistrationDTO userRegistrationDTO,
     
            BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        if(bindingResult.hasErrors()) {
             return "register";
        

        
        //da implemntare
        return "redirect:/login";
    }
    
    
     
}
