package com.devsu.hackerearth.backend.account.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devsu.hackerearth.backend.account.model.dto.AccountDto;
import com.devsu.hackerearth.backend.account.model.dto.BasicResponse;
import com.devsu.hackerearth.backend.account.model.dto.PartialAccountDto;
import com.devsu.hackerearth.backend.account.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

	private final AccountService accountService;

	public AccountController(AccountService accountService) {
		this.accountService = accountService;
	}

	@GetMapping
	public BasicResponse<List<AccountDto>> getAll() {
		List<AccountDto> accounts = this.accountService.getAll();
		return BasicResponse.of(HttpStatus.OK, "Cuentas obtenidas correctamente", accounts);
	}

	@GetMapping("/{id}")
	public BasicResponse<AccountDto> get(@PathVariable Long id) {
		AccountDto account = this.accountService.getById(id);
		return BasicResponse.of(HttpStatus.OK, "Cuenta obtenida correctamente", account);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public BasicResponse<AccountDto> create(@Valid @RequestBody AccountDto accountDto, HttpServletRequest request) {
		String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		AccountDto created = this.accountService.create(accountDto, authorizationHeader);
		return BasicResponse.of(HttpStatus.CREATED, "Cuenta creada correctamente", created);
	}

	@PutMapping("/{id}")
	public BasicResponse<AccountDto> update(@PathVariable Long id, @Valid @RequestBody AccountDto accountDto) {
		accountDto.setId(id);
		AccountDto updated = this.accountService.update(accountDto);
		return BasicResponse.of(HttpStatus.OK, "Cuenta actualizada correctamente", updated);
	}

	@PatchMapping("/{id}")
	public BasicResponse<AccountDto> partialUpdate(@PathVariable Long id,
			@RequestBody PartialAccountDto partialAccountDto) {
		AccountDto updated = this.accountService.partialUpdate(id, partialAccountDto);
		return BasicResponse.of(HttpStatus.OK, "Cuenta actualizada correctamente", updated);
	}

	@DeleteMapping("/{id}")
	public BasicResponse<Void> delete(@PathVariable Long id) {
		this.accountService.deleteById(id);
		return BasicResponse.of(HttpStatus.OK, "Cuenta eliminada correctamente", null);
	}
}
