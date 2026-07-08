package com.devsu.hackerearth.backend.account.service;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.devsu.hackerearth.backend.account.communication.rest.ClientRestClient;
import com.devsu.hackerearth.backend.account.communication.rest.dto.ClientResponseDto;
import com.devsu.hackerearth.backend.account.exception.GenericException;
import com.devsu.hackerearth.backend.account.model.Account;
import com.devsu.hackerearth.backend.account.model.Transaction;
import com.devsu.hackerearth.backend.account.model.dto.BankStatementDto;
import com.devsu.hackerearth.backend.account.model.dto.TransactionDto;
import com.devsu.hackerearth.backend.account.repository.AccountRepository;
import com.devsu.hackerearth.backend.account.repository.TransactionRepository;
import com.devsu.hackerearth.backend.account.security.SecurityUtils;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final ClientRestClient clientRestClient;

    public TransactionServiceImpl(TransactionRepository transactionRepository, AccountRepository accountRepository,
            ClientRestClient clientRestClient) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.clientRestClient = clientRestClient;
    }

    @Override
    public List<TransactionDto> getAll() {
        Long currentClientId = SecurityUtils.getCurrentClientId();
        List<Long> ownAccountIds = accountRepository.findByClientId(currentClientId).stream()
                .map(Account::getId)
                .collect(Collectors.toList());

        return transactionRepository.findByAccountIdIn(ownAccountIds).stream()
                .map(this::mapTransactionToDto)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDto getById(Long id) {
        Transaction transaction = findTransactionOrThrow(id);
        Account account = findAccountOrThrow(transaction.getAccountId());

        Long currentClientId = SecurityUtils.getCurrentClientId();
        if (currentClientId == null || !currentClientId.equals(account.getClientId())) {
            throw new GenericException(HttpStatus.NOT_FOUND, "No se encontró la transacción con id " + id);
        }

        return mapTransactionToDto(transaction);
    }

    @Override
    @Transactional
    public TransactionDto create(TransactionDto transactionDto) {
        Account account = findAccountOrThrow(transactionDto.getAccountId());

        // Cualquier cliente autenticado puede depositar; solo el titular puede retirar.
        if (transactionDto.getAmount() < 0) {
            requireAccountOwnership(account);
        }

        Transaction transaction = applyTransaction(account, transactionDto.getType(), transactionDto.getAmount(),
                transactionDto.getDate());
        return mapTransactionToDto(transaction);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Transaction original = findTransactionOrThrow(id);
        Account account = findAccountOrThrow(original.getAccountId());

        requireAccountOwnership(account);

        // No se borra el registro original: se crea un asiento de reversión con el
        // monto opuesto.
        applyTransaction(account, "Reversión: " + original.getType(), -original.getAmount(), new Date());
    }

    @Override
    public List<BankStatementDto> getAllByAccountClientIdAndDateBetween(Long clientId, Date dateTransactionStart,
            Date dateTransactionEnd) {
        Long currentClientId = SecurityUtils.getCurrentClientId();
        if (currentClientId == null || !currentClientId.equals(clientId)) {
            throw new GenericException(HttpStatus.FORBIDDEN,
                    "Solo puedes consultar el estado de cuenta de tu propio cliente");
        }

        String authorizationHeader = SecurityUtils.getCurrentAuthorizationHeader();
        ClientResponseDto client = clientRestClient.getClientByIdBlocking(clientId, authorizationHeader);

        List<Account> accounts = accountRepository.findByClientId(clientId);
        Date endOfDay = endOfDay(dateTransactionEnd);

        return accounts.stream()
                .flatMap(account -> transactionRepository
                        .findByAccountIdAndDateBetween(account.getId(), dateTransactionStart, endOfDay)
                        .stream()
                        .map(transaction -> mapToBankStatementDto(client, account, transaction)))
                .sorted(Comparator.comparing(BankStatementDto::getDate))
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDto getLastByAccountId(Long accountId) {
        return transactionRepository.findFirstByAccountIdOrderByDateDesc(accountId)
                .map(this::mapTransactionToDto)
                .orElse(null);
    }

    private Transaction applyTransaction(Account account, String type, double amount, Date date) {
        double currentBalance = transactionRepository.findFirstByAccountIdOrderByDateDesc(account.getId())
                .map(Transaction::getBalance)
                .orElse(account.getInitialAmount());

        double newBalance = currentBalance + amount;
        if (newBalance < 0) {
            throw new GenericException(HttpStatus.UNPROCESSABLE_ENTITY, "Saldo no disponible");
        }

        Transaction transaction = new Transaction();
        transaction.setDate(date != null ? date : new Date());
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setBalance(newBalance);
        transaction.setAccountId(account.getId());

        return transactionRepository.save(transaction);
    }

    private void requireAccountOwnership(Account account) {
        Long currentClientId = SecurityUtils.getCurrentClientId();
        if (currentClientId == null || !currentClientId.equals(account.getClientId())) {
            throw new GenericException(HttpStatus.FORBIDDEN,
                    "Esta operación solo puede realizarla el titular de la cuenta");
        }
    }

    private Date endOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();
    }

    private Account findAccountOrThrow(Long accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new GenericException(HttpStatus.NOT_FOUND,
                        "No se encontró la cuenta con id " + accountId));
    }

    private Transaction findTransactionOrThrow(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(
                        () -> new GenericException(HttpStatus.NOT_FOUND, "No se encontró la transacción con id " + id));
    }

    private TransactionDto mapTransactionToDto(Transaction transaction) {
        return new TransactionDto(transaction.getId(), transaction.getDate(), transaction.getType(),
                transaction.getAmount(), transaction.getBalance(), transaction.getAccountId());
    }

    private BankStatementDto mapToBankStatementDto(ClientResponseDto client, Account account,
            Transaction transaction) {
        return new BankStatementDto(transaction.getDate(), client.getName(), account.getNumber(), account.getType(),
                account.getInitialAmount(), account.isActive(), transaction.getType(), transaction.getAmount(),
                transaction.getBalance());
    }

}
