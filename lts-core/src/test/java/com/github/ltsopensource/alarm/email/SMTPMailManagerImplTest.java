package com.github.ltsopensource.alarm.email;

import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com) on 2/17/16.
 */
public class SMTPMailManagerImplTest {

    @Test
    public void testSend() throws Exception {
        String host = "smtp.163.com";
        // 授权码从这里获取 http://service.mail.qq.com/cgi-bin/help?subtype=1&&id=28&&no=1001256
        // lts12345
        MailManager mailManager = new SMTPMailManagerImpl(host, "chenwenshun@163.com", "MX2004", "chenwenshun@163.com", true);
        mailManager.send("13500752@qq.com", "测试", "fdsafhakdsjfladslfj呵呵呵");
    }
}