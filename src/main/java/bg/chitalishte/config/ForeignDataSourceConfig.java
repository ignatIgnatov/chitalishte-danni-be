package bg.chitalishte.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class ForeignDataSourceConfig {

    @Bean(name = "foreignDataSource")
    public DataSource foreignDataSource() {
        return DataSourceBuilder.create()
                .driverClassName("org.postgresql.Driver")
                .url("jdbc:postgresql://13.62.139.235:5432/chitalishte_karta_db")
                .username("chitalishte_user")
                .password("chitalishte_password")
                .build();
    }

    @Bean(name = "foreignJdbcTemplate")
    public JdbcTemplate foreignJdbcTemplate(@Qualifier("foreignDataSource") DataSource ds) {
        return new JdbcTemplate(ds);
    }
}


