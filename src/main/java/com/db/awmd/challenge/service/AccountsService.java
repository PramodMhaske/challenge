package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.AccountNotFoundException;
import com.db.awmd.challenge.exception.InsufficientBalanceException;
import com.db.awmd.challenge.repository.AccountsRepository;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountsService {

  @Getter
  private final AccountsRepository accountsRepository;
  
  @Autowired
  private NotificationService notificationService;

  @Autowired
  public AccountsService(AccountsRepository accountsRepository) {
    this.accountsRepository = accountsRepository;
  }

  public void createAccount(Account account) {
    this.accountsRepository.createAccount(account);
  }

  public Account getAccount(String accountId) {
    return this.accountsRepository.getAccount(accountId);
  }
  
  // We can use @Transactional if there is any DB transaction
  synchronized  public void transferAmount(String fromAccountId, String toAccountId, BigDecimal amount) {
	  // TODO Auto-generated method stub
	  if(fromAccountId!= null && !fromAccountId.isEmpty() && getAccount(fromAccountId)!= null) {
		  
		  if(toAccountId!= null && !toAccountId.isEmpty() &&  getAccount(toAccountId)!= null) {
			 
			  Account accountFrom = getAccount(fromAccountId);
			  boolean flag = debitFromAccount(accountFrom, amount);
			  
			  if(flag) {
				
				  Account accountTo = getAccount(toAccountId);
				  boolean creditFlag = creditToAccount(accountTo, amount);
				  if(creditFlag) {
					  //Code for Send notification details
					  sendNotification(accountFrom,accountTo,amount);
				  }
				  
			  } else {
				  throw new InsufficientBalanceException(
						  "Insufficient balance !!!");
			  }

		  } else {
			  throw new AccountNotFoundException(
					  "Account Id " + toAccountId + " is not found!");
		  }
	  } else {
		  throw new AccountNotFoundException(
				  "Account Id " + fromAccountId + " is not found!");
	  }
  }
  
  public boolean debitFromAccount(Account account, BigDecimal amount) {
	  if((amount.doubleValue() > 0) && amount.doubleValue() < account.getBalance().doubleValue()) {
		  account.setBalance(account.getBalance().subtract(amount));
		  return true;
	  } else {
		  return false;
	  }
  }

  public boolean creditToAccount(Account account, BigDecimal amount) {
	  account.setBalance(account.getBalance().add(amount));
	  return true;
  }
  
  public void sendNotification(Account fromAccount, Account toAccount, BigDecimal amount) {
	  try {
		  String accountFromNotification = amount +  " has been transfered to " + toAccount.getAccountId() + " & your Current Balance is : " + fromAccount.getBalance();
		  String accountToNotification = amount +  " has been transfered from " + fromAccount.getAccountId() + " & your Current Balance is : " + toAccount.getBalance();
		  notificationService.notifyAboutTransfer(fromAccount, accountFromNotification);
		  notificationService.notifyAboutTransfer(toAccount, accountToNotification);
	  } catch(RuntimeException e) {
		  e.printStackTrace();
	  }
  }
}
