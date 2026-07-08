package com.devsu.hackerearth.backend.account;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.devsu.hackerearth.backend.account.controller.AccountController;
import com.devsu.hackerearth.backend.account.exception.GenericException;
import com.devsu.hackerearth.backend.account.model.Account;
import com.devsu.hackerearth.backend.account.model.dto.AccountDto;
import com.devsu.hackerearth.backend.account.model.dto.TransactionDto;
import com.devsu.hackerearth.backend.account.repository.AccountRepository;
import com.devsu.hackerearth.backend.account.service.AccountService;
import com.devsu.hackerearth.backend.account.service.TransactionService;

@SpringBootTest
public class sampleTest {

	private AccountService accountService = mock(AccountService.class);
	private AccountController accountController = new AccountController(accountService);

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private TransactionService transactionService;

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private void loginAs(Long clientId) {
		SecurityContextHolder.getContext()
				.setAuthentication(new UsernamePasswordAuthenticationToken(clientId, null, Collections.emptyList()));
	}

	@Test
	void createAccountTest() {
		// Arrange
		AccountDto newAccount = new AccountDto(1L, "number", "savings", 0.0, true, 1L);
		AccountDto createdAccount = new AccountDto(1L, "number", "savings", 0.0, true, 1L);
		when(accountService.create(newAccount)).thenReturn(createdAccount);

		// Act
		ResponseEntity<AccountDto> response = accountController.create(newAccount);

		// Assert
		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		assertEquals(createdAccount, response.getBody());
	}

	@Test
	void transactionFlowUpdatesBalanceAndRejectsInsufficientFunds() {
		// Arrange: persist a real account through the actual repository/H2 database (integration test)
		Account account = new Account();
		account.setNumber("478758");
		account.setType("Ahorros");
		account.setInitialAmount(1000.0);
		account.setActive(true);
		account.setClientId(1L);
		Account savedAccount = accountRepository.save(account);

		// Act: a deposit is allowed for any authenticated client, even one that is not the owner
		loginAs(2L);
		TransactionDto deposit = new TransactionDto(null, new Date(), "Deposito", 200.0, 0.0, savedAccount.getId());
		TransactionDto createdDeposit = transactionService.create(deposit);
		assertEquals(1200.0, createdDeposit.getBalance().doubleValue());

		// Act + Assert: a withdrawal attempted by someone other than the account owner must be rejected
		TransactionDto withdrawalByOther = new TransactionDto(null, new Date(), "Retiro", -100.0, 0.0,
				savedAccount.getId());
		GenericException forbidden = assertThrows(GenericException.class, () -> transactionService.create(withdrawalByOther));
		assertEquals(HttpStatus.FORBIDDEN, forbidden.getStatus());

		// Act: the account owner withdraws within the available balance
		loginAs(1L);
		TransactionDto debit = new TransactionDto(null, new Date(), "Retiro", -575.0, 0.0, savedAccount.getId());
		TransactionDto createdDebit = transactionService.create(debit);

		// Assert: the balance was persisted and recalculated correctly (1200 - 575 = 625)
		assertEquals(625.0, createdDebit.getBalance().doubleValue());

		// Act + Assert: a withdrawal beyond the available balance must be rejected
		TransactionDto overdraft = new TransactionDto(null, new Date(), "Retiro", -1000.0, 0.0, savedAccount.getId());
		GenericException exception = assertThrows(GenericException.class, () -> transactionService.create(overdraft));
		assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, exception.getStatus());
		assertEquals("Saldo no disponible", exception.getMessage());

		// Act: deleting the withdrawal creates a reversal entry instead of removing the original record
		transactionService.deleteById(createdDebit.getId());
		TransactionDto lastTransaction = transactionService.getLastByAccountId(savedAccount.getId());

		// Assert: the reversal restored the balance to what it was before the withdrawal (625 + 575 = 1200)
		assertEquals(1200.0, lastTransaction.getBalance().doubleValue());
		assertEquals("Reversión: Retiro", lastTransaction.getType());
	}
}
