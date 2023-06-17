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
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConfig;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.NoSuchTableException;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.h2.H2DataTypeFactory;
import org.dbunit.operation.DatabaseOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Allows to easily insert and cleanup test data into a database.
 * 
 * @author arey
 * 
 */
public class DBUnitLoader {

    private static final Logger LOG = LoggerFactory.getLogger(DBUnitLoader.class);

    private final DataSource dataSource;

    DBUnitLoader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Generate a default location based on the name of the given class. If the class is named
     * com.example.MyTest, DBUnitLoad loads your DBUnit dataset from
     * "classpath:/com/example/MyTest-dataset.xml".
     * 
     * @param testClass
     */
    public void loadDatabase(Class<?> testClass) {
        loadDatabase(buildDefaultDataSetLocation(testClass));
    }

    private String buildDefaultDataSetLocation(Class<?> testClass) {
        return "classpath:" + testClass.getName().replace(".", "/") + "-dataset.xml";
    }

    public void loadDatabase(String... dataSetLocations) {
        var dataSets = new ArrayList<IDataSet>();

        if ((dataSetLocations == null) || (dataSetLocations.length == 0)) {
            throw new IllegalArgumentException("Dataset location is mandatory");
        }

        for (String dataSetLocation : dataSetLocations) {
            URL url = retrieveDataSetURL(dataSetLocation);
            FlatXmlDataSetBuilder flatXmlDataSetBuilder = new FlatXmlDataSetBuilder();
            flatXmlDataSetBuilder.setColumnSensing(true);
            IDataSet dataSet;
            try {
                dataSet = flatXmlDataSetBuilder.build(url.openStream());
            } catch (DataSetException | IOException e) {
                throw new RuntimeException("Error while reading dataset " + dataSetLocation, e);
            }
            dataSets.add(dataSet);
        }
        IDatabaseConnection dbConn;
        try {
            dbConn = new DatabaseDataSourceConnection(dataSource);
        } catch (SQLException e) {
            throw new RuntimeException("Getting JDBC data source", e);
        }
        DatabaseConfig config = dbConn.getConfig();
        config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY, new H2DataTypeFactory());
        try {
            executeDeleteAll(dataSets, dbConn);
            executeInsert(dataSets, dbConn);
        } catch (DatabaseUnitException e) {
            throw new RuntimeException("DBUnit error", e);
        } catch (SQLException e) {
            throw new RuntimeException("Inserting DBUnit dataset", e);
        }

    }

    private void executeDeleteAll(List<IDataSet> dataSets, IDatabaseConnection dbConn)
            throws DatabaseUnitException, SQLException {
        new AbstractDatabaseOperation() {

            @Override
            protected void execute(IDataSet dataSet, IDatabaseConnection dbConn)
                    throws DatabaseUnitException, SQLException {
                DatabaseOperation.DELETE_ALL.execute(dbConn, dataSet);
            }

        }.execute(dataSets, dbConn);
    }

    private void executeInsert(List<IDataSet> dataSets, IDatabaseConnection dbConn)
            throws DatabaseUnitException, SQLException {
        new AbstractDatabaseOperation() {

            @Override
            protected void execute(IDataSet dataSet, IDatabaseConnection dbConn)
                    throws DatabaseUnitException, SQLException {
                DatabaseOperation.INSERT.execute(dbConn, dataSet);
            }

        }.execute(dataSets, dbConn);
    }

    private URL retrieveDataSetURL(String dataSetLocation) {
        var url = getClass().getClassLoader().getResource(dataSetLocation);
        if (url == null) {
            throw new IllegalArgumentException("No dataSet file located at " + url);
        }
        return url;
    }

    abstract class AbstractDatabaseOperation {

        public void execute(List<IDataSet> dataSets, IDatabaseConnection dbConn)
                throws DatabaseUnitException, SQLException {
            try (var connection = dataSource.getConnection(); var statement = connection.createStatement()) {
                // language=H2
                statement.execute("SET REFERENTIAL_INTEGRITY FALSE");
                for (var dataSet : dataSets) {
                    execute(dataSet, dbConn);
                }
                // language=H2
                statement.execute("SET REFERENTIAL_INTEGRITY TRUE");
            } catch (NoSuchTableException e) {
                LOG.error("Differences between dataset tables and hibernate configuration, check your dataset (typing error), did you override at least one of these beans : mappingResources, annotatedClasses");
                throw e;
            }
        }

        protected abstract void execute(IDataSet dataSet, IDatabaseConnection dbConn)
                throws DatabaseUnitException, SQLException;
    }
}
