/**
 * Copyright 2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.javaetmoi.core.persistence.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.h2.jdbcx.JdbcDataSource;

import static java.util.Arrays.stream;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;
import static org.dbunit.database.DatabaseConfig.PROPERTY_DATATYPE_FACTORY;
import static org.dbunit.operation.DatabaseOperation.DELETE_ALL;
import static org.dbunit.operation.DatabaseOperation.INSERT;

/**
 * Allows to easily insert and cleanup test data into a database.
 * 
 * @author Antoine Rey
 * @author Markus Heiden
 */
public class DBUnitLoader {

    private final DataSource dataSource;

    DBUnitLoader(String databaseUrl) {
        var jdbcDataSource = new JdbcDataSource();
        jdbcDataSource.setURL(databaseUrl);
        this.dataSource = jdbcDataSource;
    }

    /**
     * Load the database from a data set for the given class.
     * If the class is named {@code com.example.MyTest}, DBUnitLoad loads your DBUnit data set
     * from the file {@code MyTest-dataset.xml} in the package {@code com.example}.
     *
     * @param testClass
     *         Test class.
     */
    public void loadDatabase(Class<?> testClass) {
        loadDatabase(testClass, testClass.getSimpleName() + "-dataset.xml");
    }

    /**
     * Load the database from the given data sets.
     *
     * @param dataSetLocations
     *         Data set locations.
     *         Absolute: {@code "com/example/MyTest-dataset.xml"}.
     */
    public void loadDatabase(String... dataSetLocations) {
        loadDatabase(null, dataSetLocations);
    }

    /**
     * Load the database from the given data sets.
     *
     * @param testClass
     *         Test class used as anchor.
     * @param dataSetLocations
     *         Data set locations.
     *         If the test class is not {@code null}, relative to the test class: {@code "MyTest-dataset.xml"}.
     *         If the test class is {@code null}, absolute: {@code "com/example/MyTest-dataset.xml"}.
     */
    public void loadDatabase(Class<?> testClass, String... dataSetLocations) {
        if (isEmpty(dataSetLocations)) {
            throw new IllegalArgumentException("Data set locations are mandatory");
        }

        var dataSets = stream(dataSetLocations)
                .map(dataSetLocation -> buildDataSet(testClass, dataSetLocation))
                .toArray(IDataSet[]::new);

        var connection = connection();
        executeAll(connection, DELETE_ALL, dataSets);
        executeAll(connection, INSERT, dataSets);
    }

    private IDataSet buildDataSet(Class<?> testClass, String dataSetLocation) {
        try (var dataSet = getResourceAsStream(testClass, dataSetLocation)) {
            if (dataSet == null) {
                throw new IllegalArgumentException("No data set file located at " + dataSetLocation);
            }

            return new FlatXmlDataSetBuilder()
                    .setColumnSensing(true)
                    .build(dataSet);
        } catch (DataSetException | IOException e) {
            throw new RuntimeException("Error while reading data set " + dataSetLocation, e);
        }
    }

    private InputStream getResourceAsStream(Class<?> testClass, String dataSetLocation) {
        return testClass != null ?
                testClass.getResourceAsStream(dataSetLocation) :
                getClass().getClassLoader().getResourceAsStream(dataSetLocation);
    }

    private IDatabaseConnection connection() {
        try {
            var connection = new DatabaseDataSourceConnection(dataSource);
            var config = connection.getConfig();
            config.setProperty(PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Error while getting the JDBC data source", e);
        }
    }

    public void executeAll(IDatabaseConnection connection, DatabaseOperation operation, IDataSet... dataSets) {
        try (var jdbcConnection = dataSource.getConnection(); var statement = jdbcConnection.createStatement()) {
            // language=H2
            statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
            for (var dataSet : dataSets) {
                operation.execute(connection, dataSet);
            }
            // language=H2
            statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
        } catch (NoSuchTableException e) {
            // Ignore missing tables.
            // Delete all: A not existing table needs not be dropped.
            // Insert: When creating a table it should not exist.
        } catch (DatabaseUnitException e) {
            throw new RuntimeException("DBUnit error", e);
        } catch (SQLException e) {
            throw new RuntimeException("Data set error", e);
        }
    }
}
