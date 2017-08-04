package com.roo.utils;

import com.roo.config.RooConst;
import com.roo.model.param.ForgetParam;
import com.roo.model.param.RegisterParam;
import io.github.biezhi.ome.OhMyEmail;
import jetbrick.template.JetEngine;
import jetbrick.template.JetTemplate;
import lombok.extern.slf4j.Slf4j;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 邮箱配置
 *
 * @author biezhi
 * @date 2017/8/4
 */
@Slf4j
public class EmailUtils {

    private static final JetEngine       engine   = JetEngine.create();
    private static final ExecutorService executor = Executors.newFixedThreadPool(3);

    public static void init() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.ssl.enable", RooConst.settings.getOrDefault("mail.smtp.ssl.enable", "false"));
            props.put("mail.transport.protocol", "smtp");
            props.put("mail.smtp.timeout", RooConst.settings.getOrDefault("mail.smtp.timeout", "10000"));
            props.put("mail.smtp.host", RooConst.settings.get("mail.smtp.host"));
            props.put("mail.smtp.port", RooConst.settings.getOrDefault("mail.smtp.host", "25"));

            OhMyEmail.config(props,
                    RooConst.settings.get("mail.username"),
                    RooConst.settings.get("mail.password"));
        } catch (Exception e) {
            throw new RuntimeException("Email init error.");
        }
    }

    /**
     * 异步发送注册邮件
     *
     * @param registerParam
     */
    public static void sendRegister(RegisterParam registerParam) {
        executor.submit(() -> {
            JetTemplate         template = engine.getTemplate("/templates/emails/register.html");
            Map<String, Object> context  = new HashMap<>();
            context.put("username", registerParam.getUsername());
            context.put("appname", RooConst.settings.getOrDefault("mail.form", "Roo 社区"));
            context.put("link", registerParam.getLink());
            context.put("copyright", "Roo 社区 © 2017");

            StringWriter writer = new StringWriter();
            template.render(context, writer);
            String output = writer.toString();

            try {
                String subject = "欢迎您加入" + RooConst.settings.getOrDefault("mail.form", "Roo 社区");
                OhMyEmail.subject(subject)
                        .from(RooConst.settings.getOrDefault("mail.form", "Roo社区"))
                        .to(registerParam.getEmail())
                        .html(output)
                        .send();
            } catch (Exception e) {
                log.error("Email send error", e);
            }
        });
    }

    /**
     * 异步发送密码重置邮件
     */
    public static void sendForget(ForgetParam forgetParam) {
        executor.submit(() -> {
            JetTemplate         template = engine.getTemplate("/templates/emails/forget.html");
            Map<String, Object> context  = new HashMap<>();
            context.put("appname", RooConst.settings.getOrDefault("mail.form", "Roo 社区"));
            context.put("link", forgetParam.getLink());
            context.put("copyright", "Roo 社区 © 2017");

            StringWriter writer = new StringWriter();
            template.render(context, writer);
            String output = writer.toString();

            try {
                String subject = "密码重置 - " + RooConst.settings.getOrDefault("mail.form", "Roo 社区");
                OhMyEmail.subject(subject)
                        .from(RooConst.settings.getOrDefault("mail.form", "Roo社区"))
                        .to(forgetParam.getEmail())
                        .html(output)
                        .send();
            } catch (Exception e) {
                log.error("Email send error", e);
            }
        });
    }

}