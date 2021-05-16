package com.herokuapp.projectideas.email;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

@org.springframework.context.annotation.Configuration
public class EmailTemplateLoader {

    @Bean
    public FreeMarkerConfigurer freemarkerClassLoaderConfig() {
        Configuration configuration = new Configuration(
            Configuration.VERSION_2_3_31
        );
        TemplateLoader templateLoader = new ClassTemplateLoader(
            this.getClass(),
            "/ftl-templates"
        );
        configuration.setTemplateLoader(templateLoader);
        FreeMarkerConfigurer freeMarkerConfigurer = new FreeMarkerConfigurer();
        freeMarkerConfigurer.setConfiguration(configuration);
        return freeMarkerConfigurer;
    }

    @Bean
    public Template unreadMessagesTemplate(
        FreeMarkerConfigurer freemarkerClassLoaderConfig
    ) {
        try {
            return freemarkerClassLoaderConfig
                .getConfiguration()
                .getTemplate("unreadMessages.ftl");
        } catch (Exception ignored) {
            return null;
        }
    }
}
