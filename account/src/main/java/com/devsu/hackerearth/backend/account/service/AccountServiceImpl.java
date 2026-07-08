package com.devsu.hackerearth.backend.account.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.devsu.hackerearth.backend.account.communication.rest.ClientRestClient;
import com.devsu.hackerearth.backend.account.exception.GenericException;
import com.devsu.hackerearth.backend.account.model.Account;
import com.devsu.hackerearth.backend.account.model.dto.AccountDto;
import com.devsu.hackerearth.backend.account.model.dto.PartialAccountDto;
import com.devsu.hackerearth.backend.account.repository.AccountRepository;
import com.devsu.hackerearth.backend.account.security.SecurityUtils;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final ClientRestClient clientRestClient;

    public AccountServiceImpl(AccountRepository accountRepository, ClientRestClient clientRestClient) {
        this.accountRepository = accountRepository;
        this.clientRestClient = clientRestClient;
    }

    @Override
    public List<AccountDto> getAll() {
        Long currentClientId = SecurityUtils.getCurrentClientId();
        return this.accountRepository.findByClientId(currentClientId).stream()
                .map(this::mapAccountToDto)
                .collect(Collectors.toList());
    }

    @Override
    public AccountDto getById(Long id) {
        Account account = findAccountOrThrow(id);
        this.requireAccountOwnership(account);
        return this.mapAccountToDto(account);
    }

    @Override
    @Transactional
    public AccountDto create(AccountDto accountDto) {
        Long currentClientId = SecurityUtils.getCurrentClientId();

        accountDto.setClientId(currentClientId);

        String authorizationHeader = SecurityUtils.getCurrentAuthorizationHeader();
        this.clientRestClient.getClientByIdBlocking(currentClientId, authorizationHeader);

        Account newAccount = new Account();
        this.mapDtoToAccount(newAccount, accountDto);

        Account savedAccount = this.accountRepository.save(newAccount);

        return this.mapAccountToDto(savedAccount);
    }

    @Override
	@Transactional
	public AccountDto update(AccountDto accountDto) {
		Account existingAccount = findAccountOrThrow(accountDto.getId());
		this.requireAccountOwnership(existingAccount);
		this.mapDtoToAccount(existingAccount, accountDto);

		Account updatedAccount = accountRepository.save(existingAccount);
		return mapAccountToDto(updatedAccount);
	}

	@Override
	@Transactional
	public AccountDto partialUpdate(Long id, PartialAccountDto partialAccountDto) {
		Account existingAccount = findAccountOrThrow(id);
		this.requireAccountOwnership(existingAccount);

		if (partialAccountDto.getType() != null) {
			existingAccount.setType(partialAccountDto.getType());
		}
		if (partialAccountDto.getInitialAmount() != null) {
			existingAccount.setInitialAmount(partialAccountDto.getInitialAmount());
		}
		if (partialAccountDto.getActive() != null) {
			existingAccount.setActive(partialAccountDto.getActive());
		}

		Account updatedAccount = this.accountRepository.save(existingAccount);
		return this.mapAccountToDto(updatedAccount);
	}

	@Override
	@Transactional
	public void deleteById(Long id) {
		Account account = findAccountOrThrow(id);
		this.requireAccountOwnership(account);
		this.accountRepository.deleteById(id);
	}

    private void mapDtoToAccount(Account account, AccountDto accountDto) {
        account.setNumber(accountDto.getNumber());
        account.setType(accountDto.getType());
        account.setInitialAmount(accountDto.getInitialAmount());
        account.setActive(accountDto.isActive());
        account.setClientId(accountDto.getClientId());
    }

    private AccountDto mapAccountToDto(Account account) {
        return new AccountDto(account.getId(), account.getNumber(), account.getType(), account.getInitialAmount(),
                account.isActive(), account.getClientId());
    }

    private Account findAccountOrThrow(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND, "No se encontró la cuenta con id " + id));
    }

    private void requireAccountOwnership(Account account) {
		Long currentClientId = SecurityUtils.getCurrentClientId();
		if (currentClientId == null || !currentClientId.equals(account.getClientId())) {
			throw new GenericException(HttpStatus.NOT_FOUND, "No se encontró la cuenta con id " + account.getId());
		}
	}

}
