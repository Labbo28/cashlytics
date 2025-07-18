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
        Long merchantId = dto.getMerchantId();
        String merchantName = dto.getMerchantName() != null ? dto.getMerchantName().trim() : "";
        if (merchantId != null) {
            Optional<Merchant> opt = findByIdAndUser(merchantId, user);
            if (opt.isPresent()) {
                return opt.get();
            } else {
                bindingResult.rejectValue("merchantId", "error.transactionDTO", "Merchant selezionato non valido.");
                return null;
            }
        }

        if (!merchantName.isEmpty()) {
            Optional<Merchant> optByName = findByNameAndUser(merchantName, user);
            if (optByName.isPresent()) {
                return optByName.get();
            } else {
                Merchant newMer = new Merchant();
                newMer.setName(merchantName);
                newMer.setUser(user);
                return save(newMer);
            }
        }
        return null;
    }

}
