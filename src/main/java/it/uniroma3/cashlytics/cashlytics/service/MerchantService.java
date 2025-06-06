package it.uniroma3.cashlytics.cashlytics.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.uniroma3.cashlytics.cashlytics.Model.Merchant;
import it.uniroma3.cashlytics.cashlytics.Model.User;
import it.uniroma3.cashlytics.cashlytics.Repository.MerchantRepository;

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
}
