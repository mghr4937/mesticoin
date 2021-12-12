package com.example.mestichain;

import com.example.mestichain.domain.Transaction;
import com.example.mestichain.utils.SignatureUtils;
import com.example.mestichain.utils.constants.Path;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URL;
import java.security.KeyPair;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
class MestichainApplicationTests {

	@Autowired
	private MockMvc mvc;

	public static String asJsonString(final Object obj) {
		try {
			final ObjectMapper mapper = new ObjectMapper();
			return mapper.writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Test
	public void addTransaction() throws Exception {
		KeyPair senderKey = SignatureUtils.generateKeyPair();
		log.info("Sender Key: {}", senderKey);
		KeyPair recipientKey = SignatureUtils.generateKeyPair();
		log.info("Recipient Key: {}", recipientKey);

		Transaction transaction = new Transaction(senderKey.getPublic().getEncoded(), recipientKey.getPublic().getEncoded(), 100);

		transaction.setSignature(SignatureUtils.sign(transaction.getContent(), senderKey.getPrivate().getEncoded()));
		log.info("Transaction created: {}", transaction);

		mvc.perform(MockMvcRequestBuilders.post(Path.TRANSACTION).content(asJsonString(transaction))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isAccepted());
	}

	@Test
	public void getTransactionPool() throws Exception {
		KeyPair recipientKey = SignatureUtils.generateKeyPair();

		String senderPublicKey = "MIIBtzCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoDgYQAAoGAY/Vx+BoONG2RSNIuM8ZrrVJ/Y0fbePVbyBAaLVIzIfilYk1WCGvz/ijOzk9Gp6DesHbXnf9MZptm36a3amu3JEBFyaBP4/j2gTs+N47UbvB/45hnjTdN0yI2H6iubxn8wgq2MartRYFRhF2D6Dcaw0DqNkRqyHDB2c+gVTTehSI=";
		String senderPrivateKey = "MIIBSwIBADCCASwGByqGSM44BAEwggEfAoGBAP1/U4EddRIpUt9KnC7s5Of2EbdSPO9EAMMeP4C2USZpRV1AIlH7WT2NWPq/xfW6MPbLm1Vs14E7gB00b/JmYLdrmVClpJ+f6AR7ECLCT7up1/63xhv4O1fnxqimFQ8E+4P208UewwI1VBNaFpEy9nXzrith1yrv8iIDGZ3RSAHHAhUAl2BQjxUjC8yykrmCouuEC/BYHPUCgYEA9+GghdabPd7LvKtcNrhXuXmUr7v6OuqC+VdMCz0HgmdRWVeOutRZT+ZxBxCBgLRJFnEj6EwoFhO3zwkyjMim4TwWeotUfI0o4KOuHiuzpnWRbqN/C/ohNWLx+2J6ASQ7zKTxvqhRkImog9/hWuWfBpKLZl6Ae1UlZAFMO/7PSSoEFgIUH7WhGYQ8hnRF4vTspE3CpjZRKqs=";

		Transaction transaction = new Transaction(Base64.decodeBase64(senderPublicKey), recipientKey.getPublic().getEncoded(), 5);
		transaction.setSignature(SignatureUtils.sign(transaction.getContent(), Base64.decodeBase64(senderPrivateKey)));

		mvc.perform(MockMvcRequestBuilders.post(Path.TRANSACTION).content(asJsonString(transaction)).contentType(MediaType.APPLICATION_JSON));
		mvc.perform(MockMvcRequestBuilders.get( Path.TRANSACTION).contentType(MediaType.APPLICATION_JSON));

	}

	@Test
	public void getPublicIp() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(Path.NODE + Path.NODE_IP).contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	public void addNode() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(Path.NODE).content(asJsonString(new URL("http", "localhost", 8090, "")))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}

	@Test
	public void getParentNodes() throws Exception {
		mvc.perform(MockMvcRequestBuilders.get(Path.NODE)
				.contentType(MediaType.APPLICATION_JSON));
	}

	@Test
	public void bajaNodo() throws Exception {
		mvc.perform(MockMvcRequestBuilders.post(Path.NODE).content(asJsonString(new URL("http", "localhost", 8090, "")))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

		mvc.perform(MockMvcRequestBuilders.delete( Path.NODE).content(asJsonString(new URL("http", "localhost", 8090, "")))
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
	}


}
