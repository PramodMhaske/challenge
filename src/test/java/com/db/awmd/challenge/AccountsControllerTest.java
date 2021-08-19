package com.db.awmd.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.service.AccountsService;
import java.math.BigDecimal;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @Before
  public void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  public void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  public void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  public void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  
  @Test
  public void transferAmount() throws Exception {
	  
	  String fromAccountId = "Id-111";
	  String toAccountId = "Id-222";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1050}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":950}")).andExpect(status().isCreated());
	  
	  
	  BigDecimal amount = new BigDecimal("50");
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON).param("fromAccountId", fromAccountId)
			  .param("toAccountId", toAccountId).param("amount", amount.toString()))
		      .andExpect(status().isOk());
  }
  
  @Test
  public void transferAmountMoreThanBalance() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  BigDecimal amount = new BigDecimal("20000");
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount.toString())).andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferAmountFromEmptyAccount() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  fromAccountId = "";
	  BigDecimal amount = new BigDecimal("20000");
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount.toString())).andExpect(status().isNotFound());
  }
  
  @Test
  public void transferAmountFromNullAccount() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  fromAccountId = null;
	  BigDecimal amount = new BigDecimal("20000");
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount.toString())).andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferAmountToEmptyAccount() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  toAccountId = "";
	  BigDecimal amount = new BigDecimal("20000");
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount.toString())).andExpect(status().isNotFound());
  }
  
  @Test
  public void transferAmountToNullAccount() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  toAccountId = null;
	  BigDecimal amount = new BigDecimal("20000");
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount.toString())).andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferNullAmount() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  String amount = null;
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount)).andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferEmptyAmount() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  String amount = "";
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount)).andExpect(status().isInternalServerError());
  }
  
  @Test
  public void transferNegativeAmount() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  
	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" + fromAccountId + "\",\"balance\":1000}")).andExpect(status().isCreated());

	  this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
	            .content("{\"accountId\":\"" +toAccountId+ "\",\"balance\":1000}")).andExpect(status().isCreated());
	  
	  String amount = "-2";
	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount)).andExpect(status().isBadRequest());
  }
  
  @Test
  public void transferAmountAccountNotExist() throws Exception {
	  
	  String fromAccountId = "Id-333";
	  String toAccountId = "Id-444";
	  BigDecimal amount = new BigDecimal("20000");

	  this.mockMvc.perform(post("/v1/accounts/transfer").contentType(MediaType.APPLICATION_JSON)
		      .param("fromAccountId",fromAccountId).param("toAccountId", toAccountId).param("amount", amount.toString())).andExpect(status().isNotFound());
  }
}
