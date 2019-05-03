package ru.ndb.testcontainers.model;

import com.mysql.clusterj.annotation.Column;
import com.mysql.clusterj.annotation.PersistenceCapable;
import com.mysql.clusterj.annotation.PrimaryKey;

@PersistenceCapable(table = "user")
public interface User {

    @PrimaryKey
    int getId();

    void setId(int id);

    @Column(name = "firstName")
    String getFirstName();

    void setFirstName(String firstName);

    @Column(name = "lastName")
    String getLastName();

    void setLastName(String lastName);
}