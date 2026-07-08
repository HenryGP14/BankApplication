package com.devsu.hackerearth.backend.account.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devsu.hackerearth.backend.account.model.dto.BankStatementDto;
import com.devsu.hackerearth.backend.account.model.dto.BasicResponse;
import com.devsu.hackerearth.backend.account.model.dto.TransactionDto;
import com.devsu.hackerearth.backend.account.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

	private final TransactionService transactionService;

	public TransactionController(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	@GetMapping
	public BasicResponse<List<TransactionDto>> getAll() {
		List<TransactionDto> transactions = this.transactionService.getAll();
		return BasicResponse.of(HttpStatus.OK, "Transacciones obtenidas correctamente", transactions);
	}

	@GetMapping("/{id}")
	public BasicResponse<TransactionDto> get(@PathVariable Long id) {
		TransactionDto transaction = this.transactionService.getById(id);
		transaction.setBalance(null);
		return BasicResponse.of(HttpStatus.OK, "Transacción obtenida correctamente", transaction);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BasicResponse<TransactionDto> create(@RequestBody @Valid TransactionDto transactionDto) {
		TransactionDto created = this.transactionService.create(transactionDto);
		created.setBalance(null);
		return BasicResponse.of(HttpStatus.CREATED, "Transacción registrada correctamente", created);
	}

	@DeleteMapping("/{id}")
	public BasicResponse<Void> delete(@PathVariable Long id) {
		this.transactionService.deleteById(id);
		return BasicResponse.of(HttpStatus.OK, "Transacción revertida correctamente", null);
	}

	@GetMapping("/clients/{clientId}/report")
	public BasicResponse<List<BankStatementDto>> report(@PathVariable Long clientId,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTransactionStart,
			@RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date dateTransactionEnd,
			HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		List<BankStatementDto> report = this.transactionService.getAllByAccountClientIdAndDateBetween(clientId,
				dateTransactionStart, dateTransactionEnd, authorizationHeader);
		return BasicResponse.of(HttpStatus.OK, "Reporte generado correctamente", report);
	}
}
