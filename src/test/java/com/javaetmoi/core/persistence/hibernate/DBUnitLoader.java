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
import java.sql.SQLException;
import java.util.List;

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
import static java.util.stream.Collectors.toUnmodifiableList;
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
     * Generate a default location based on the name of the given class.
     * If the class is named {@code com.example.MyTest},
     * DBUnitLoad loads your DBUnit dataset from {@code com/example/MyTest-dataset.xml}.
     *
     * @param testClass
     *         Test class.
     */
    public void loadDatabase(Class<?> testClass) {
        var dataSetLocation = testClass.getName().replace(".", "/") + "-dataset.xml";
        loadDatabase(dataSetLocation);
    }

    public void loadDatabase(String... dataSetLocations) {
        if (isEmpty(dataSetLocations)) {
            throw new IllegalArgumentException("Dataset locations are mandatory");
        }

        var dataSets = stream(dataSetLocations)
                .map(this::buildDataSet)
                .collect(toUnmodifiableList());

        var connection = connection();
        executeAll(connection, DELETE_ALL, dataSets);
        executeAll(connection, INSERT, dataSets);
    }

    private IDataSet buildDataSet(String dataSetLocation) {
        var url = getClass().getClassLoader().getResource(dataSetLocation);
        if (url == null) {
            throw new IllegalArgumentException("No dataset file located at " + dataSetLocation);
        }

        try {
            var flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
            flatXmlDataSetBuilder.setColumnSensing(true);
            return flatXmlDataSetBuilder.build(url.openStream());
        } catch (DataSetException | IOException e) {
            throw new RuntimeException("Error while reading dataset " + dataSetLocation, e);
        }
    }

    private IDatabaseConnection connection() {
        try {
            var connection = new DatabaseDataSourceConnection(dataSource);
            var config = connection.getConfig();
            config.setProperty(PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException("Error while getting JDBC data source", e);
        }
    }

    public void executeAll(IDatabaseConnection connection, DatabaseOperation operation, List<IDataSet> dataSets) {
        try (var c = dataSource.getConnection(); var statement = c.createStatement()) {
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
            throw new RuntimeException("Inserting DBUnit dataset", e);
        }
    }
}
