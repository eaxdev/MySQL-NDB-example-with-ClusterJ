package ru.example.clusterj.autoconfigure;

import com.mysql.clusterj.ClusterJHelper;
import com.mysql.clusterj.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * @author Roman Skidan
 */
@Configuration
@EnableConfigurationProperties(ClusterJProperties.class)
@ConditionalOnProperty(prefix = "clusterj", name = {"connectString", "dataBaseName"})
public class ClusterJAutoConfiguration {

    private final ClusterJProperties clusterJConfigProperties;

    @Autowired
    public ClusterJAutoConfiguration(ClusterJProperties clusterJConfigProperties) {
        this.clusterJConfigProperties = clusterJConfigProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public SessionFactory sessionFactory() {
        Properties clusterJProperties = new Properties();
        clusterJProperties.setProperty("com.mysql.clusterj.connectstring", clusterJConfigProperties.getConnectString());
        clusterJProperties.setProperty("com.mysql.clusterj.database", clusterJConfigProperties.getDataBaseName());
        clusterJProperties.putAll(clusterJConfigProperties.getProperties());
        return ClusterJHelper.getSessionFactory(clusterJProperties);
    }
}
