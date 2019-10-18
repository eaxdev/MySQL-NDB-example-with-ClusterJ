package ru.example.clusterj.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import java.util.Properties;

/**
 * @author Roman Skidan
 */
@Getter
@Setter
@Validated
@ConfigurationProperties("clusterj")
class ClusterJProperties {

    @NotBlank
    private String connectString;

    @NotBlank
    private String dataBaseName;

    private Properties properties = new Properties();

}
