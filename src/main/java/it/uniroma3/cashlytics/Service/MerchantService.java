package it.uniroma3.cashlytics.Service;

import java.util.Optional;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;

import it.uniroma3.cashlytics.DTO.TransactionDTO;
import it.uniroma3.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.Model.User;
import it.uniroma3.cashlytics.Repository.MerchantRepository;

@Service
public class MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;

    /**
     * Restituisce tutti i merchant associati all’utente corrente.
     */
    public Set<Merchant> findAllByUser(User currentUser) {
        return merchantRepository.findAllByUser(currentUser);
    }

    /**
     * Cerca un Merchant per ID verificando che appartenga all’utente passato.
     */
    public Optional<Merchant> findByIdAndUser(Long merId, User user) {
        return merchantRepository.findByIdAndUser(merId, user);
    }

    /**
     * Cerca un Merchant per nome verificando che appartenga all’utente passato.
     */
    public Optional<Merchant> findByNameAndUser(String merName, User user) {
        return merchantRepository.findByNameIgnoreCaseAndUser(merName, user);
    }

    /**
     * Salva (inserisce o aggiorna) un Merchant nel repository.
     */
    public Merchant save(Merchant newMer) {
        return merchantRepository.save(newMer);
    }

    /**
     * Risolve o crea un merchant basato sui dati del DTO per le transazioni.
     */
    public Merchant resolveOrCreateMerchant(TransactionDTO dto, User user, BindingResult bindingResult) {
        // Caso 1: Merchant ID fornito → verifica che esista
        Long merchantId = dto.getMerchantId();
        if (merchantId != null) {
            Optional<Merchant> opt = findByIdAndUser(merchantId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue("merchantId", "error.transactionDTO", "Esercente non valido.");
                return null;
            }
        }
        // Caso 2: Merchant nuovo → nome obbligatorio
        String merchantName = dto.getMerchantName() != null ? dto.getMerchantName().trim() : "";
        if (merchantName.isBlank()) {
            bindingResult.rejectValue("merchantName", "error.transactionDTO", "Inserisci il nome dell'esercente.");
            return null;
        }
        // Cerca se esiste già un esercente con quel nome per l’utente
        Optional<Merchant> optByName = findByNameAndUser(merchantName, user);
        if (optByName.isPresent()) {
            return optByName.get(); // Evita duplicati
        }
        // Altrimenti, crea nuovo merchant
        Merchant newMer = new Merchant();
        newMer.setName(merchantName);
        newMer.setUser(user);
        return save(newMer);
    }

}
