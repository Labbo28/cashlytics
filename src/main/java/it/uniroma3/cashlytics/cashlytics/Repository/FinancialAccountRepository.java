package it.uniroma3.cashlytics.cashlytics.Repository;

import it.uniroma3.cashlytics.cashlytics.Model.FinancialAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface FinancialAccountRepository extends JpaRepository<FinancialAccount, Long> {
    
    /**
     * Trova tutti gli account finanziari di un utente tramite username
     */
    List<FinancialAccount> findByUser_Credentials_Username(String username);
    
    /**
     * Trova tutti gli account finanziari di un utente tramite user ID
     */
    List<FinancialAccount> findByUser_Id(Long userId);
    
    /**
     * Calcola il bilancio totale di un utente
     */
    @Query("SELECT COALESCE(SUM(fa.balance), 0) FROM FinancialAccount fa WHERE fa.user.credentials.username = :username")
    BigDecimal getTotalBalanceByUsername(@Param("username") String username);
    
    /**
     * Trova gli account per tipo
     */
    List<FinancialAccount> findByUser_Credentials_UsernameAndAccountType(String username, it.uniroma3.cashlytics.cashlytics.Model.Enums.AccountType accountType);
    
    /**
     * Conta il numero di account di un utente
     */
    @Query("SELECT COUNT(fa) FROM FinancialAccount fa WHERE fa.user.credentials.username = :username")
    long countByUsername(@Param("username") String username);
}