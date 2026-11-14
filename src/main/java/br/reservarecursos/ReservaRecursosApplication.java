package br.reservarecursos;

import br.reservarecursos.ldap.Atualizador;
import br.reservarecursos.ldap.PersonRepo;
import jakarta.servlet.DispatcherType;
import org.apache.wicket.protocol.http.WicketFilter;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.orm.jpa.hibernate.HibernateTransactionManager;
import org.springframework.orm.jpa.hibernate.LocalSessionFactoryBean;
import org.springframework.orm.jpa.support.OpenEntityManagerInViewFilter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Ponto de entrada Spring Boot. Substitui o web.xml + applicationContext.xml:
 * os filtros viram FilterRegistrationBean e os beans de infraestrutura viram
 * métodos @Bean. O WicketApplication continua sendo um @Component descoberto
 * pelo component scan, e o SpringWebApplicationFactory o resolve pelo contexto.
 */
@SpringBootApplication
@EnableTransactionManagement
public class ReservaRecursosApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservaRecursosApplication.class, args);
    }

    // ---------- infraestrutura (antes no applicationContext.xml) ----------

    @Bean
    public DataSource dataSource(@Value("${jdbc.driver}") String driver,
                                 @Value("${jdbc.url}") String url,
                                 @Value("${jdbc.username}") String user,
                                 @Value("${jdbc.password}") String password) {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName(driver);
        ds.setUrl(url);
        ds.setUsername(user);
        ds.setPassword(password);
        return ds;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource ds,
                                                  @Value("${hibernate.hbm2ddl.auto}") String hbm2ddl,
                                                  @Value("${hibernate.dialect}") String dialect,
                                                  @Value("${hibernate.show_sql}") String showSql) {
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(ds);
        factory.setPackagesToScan("br.reservarecursos.entities");
        Properties props = new Properties();
        props.setProperty("hibernate.hbm2ddl.auto", hbm2ddl);
        props.setProperty("hibernate.dialect", dialect);
        props.setProperty("hibernate.show_sql", showSql);
        props.setProperty("hibernate.current_session_context_class",
                "org.springframework.orm.jpa.hibernate.SpringSessionContext");
        factory.setHibernateProperties(props);
        return factory;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public JavaMailSenderImpl mailSender(@Value("${email.host}") String host,
                                         @Value("${email.port}") int port,
                                         @Value("${email.username}") String user,
                                         @Value("${email.password}") String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(user);
        sender.setPassword(password);
        Properties props = new Properties();
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.starttls.enable", "true");
        props.setProperty("mail.smtp.ssl.trust", host);
        sender.setJavaMailProperties(props);
        return sender;
    }

    @Bean("dbTeste")
    public Boolean dbTeste(@Value("${db.teste}") boolean value) {
        return value;
    }

    @Bean("restLigar")
    public Boolean restLigar(@Value("${rest.ligar}") boolean value) {
        return value;
    }

    @Bean("ldapLigado")
    public Boolean ldapLigado(@Value("${ldap.ligado}") boolean value) {
        return value;
    }

    @Bean("ldapAtualizacaoAutomatica")
    public Boolean ldapAtualizacaoAutomatica(@Value("${ldap.atualizacao_automatica}") boolean value) {
        return value;
    }

    @Bean
    public PersonRepo personRepo() {
        return new PersonRepo();
    }

    @Bean
    public Atualizador atualizador() {
        return new Atualizador();
    }

    // ---------- filtros (antes no web.xml) ----------

    @Bean
    public FilterRegistrationBean<OpenEntityManagerInViewFilter> hibernateFilter() {
        FilterRegistrationBean<OpenEntityManagerInViewFilter> bean =
                new FilterRegistrationBean<>(new OpenEntityManagerInViewFilter());
        bean.setName("hibernateFilter");
        bean.addUrlPatterns("/*");
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    @Bean
    public FilterRegistrationBean<WicketFilter> wicketFilter() {
        FilterRegistrationBean<WicketFilter> bean = new FilterRegistrationBean<>(new WicketFilter());
        bean.setName("wicketFilter");
        bean.addInitParameter("applicationFactoryClassName",
                "org.apache.wicket.spring.SpringWebApplicationFactory");
        bean.addInitParameter("applicationClassName", "br.reservarecursos.WicketApplication");
        bean.addInitParameter("filterMappingUrlPattern", "/*");
        bean.addUrlPatterns("/*");
        bean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        return bean;
    }

    // ---------- assets estáticos (antes servidos pelo DefaultServlet do Jetty) ----------

    @Bean
    public WebMvcConfigurer staticResources() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/css/**").addResourceLocations("classpath:/css/");
                registry.addResourceHandler("/imgs/**").addResourceLocations("classpath:/imgs/");
                registry.addResourceHandler("/bootstrap-3.3.5-dist/**")
                        .addResourceLocations("classpath:/bootstrap-3.3.5-dist/");
                registry.addResourceHandler("/bootstrap-daterangepicker-master/**")
                        .addResourceLocations("classpath:/bootstrap-daterangepicker-master/");
            }
        };
    }
}
