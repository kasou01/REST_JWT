package com.RESTJWT.demo;

import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class DemoApplicationTests {

	@Test
	void contextLoads() {
		BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
		String hash = bCryptPasswordEncoder.encode("password");
		System.out.println(hash);
		Assert.isTrue(bCryptPasswordEncoder.matches("password", hash));
	}

}
