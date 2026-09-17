package com.tcxw;

import com.tcxw.utils.JwtUtil;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class TongchuangXianwuApplicationTests {

	@Test
	void shouldGetSameUserIdFromGeneratedToken(){
				JwtUtil jwtUtil = new JwtUtil("this-is-a-test- secret-key-at-least-32-chars");
				Long expectedUserId = 1001L;

				String token = jwtUtil.generateToken(expectedUserId,"alice","USER");
				Long actualUserId = jwtUtil.getUserId(token);
				assertEquals(expectedUserId,actualUserId);
	}

}
