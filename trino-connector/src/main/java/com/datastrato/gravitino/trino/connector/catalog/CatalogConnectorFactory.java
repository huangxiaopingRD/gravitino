/*
 * Copyright 2023 Datastrato Pvt Ltd.
 * This software is licensed under the Apache License version 2.
 */
package com.datastrato.gravitino.trino.connector.catalog;

import static com.datastrato.gravitino.trino.connector.GravitinoErrorCode.GRAVITINO_UNSUPPORTED_CATALOG_PROVIDER;

import com.datastrato.gravitino.trino.connector.catalog.hive.HiveConnectorAdapter;
import com.datastrato.gravitino.trino.connector.catalog.iceberg.IcebergConnectorAdapter;
import com.datastrato.gravitino.trino.connector.catalog.jdbc.mysql.MySQLConnectorAdapter;
import com.datastrato.gravitino.trino.connector.catalog.jdbc.postgresql.PostgreSQLConnectorAdapter;
import com.datastrato.gravitino.trino.connector.catalog.memory.MemoryConnectorAdapter;
import com.datastrato.gravitino.trino.connector.metadata.GravitinoCatalog;
import io.trino.spi.TrinoException;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** This class use to create CatalogConnectorContext instance by given catalog. */
public class CatalogConnectorFactory {
  private static final Logger LOG = LoggerFactory.getLogger(CatalogConnectorFactory.class);

  private final HashMap<String, CatalogConnectorContext.Builder> catalogBuilders = new HashMap<>();

  public CatalogConnectorFactory() {
    catalogBuilders.put("hive", new CatalogConnectorContext.Builder(new HiveConnectorAdapter()));
    catalogBuilders.put(
        "memory", new CatalogConnectorContext.Builder(new MemoryConnectorAdapter()));
    catalogBuilders.put(
        "lakehouse-iceberg", new CatalogConnectorContext.Builder(new IcebergConnectorAdapter()));
    catalogBuilders.put(
        "jdbc-mysql", new CatalogConnectorContext.Builder(new MySQLConnectorAdapter()));
    catalogBuilders.put(
        "jdbc-postgresql", new CatalogConnectorContext.Builder(new PostgreSQLConnectorAdapter()));
  }

  public CatalogConnectorContext.Builder createCatalogConnectorContextBuilder(
      GravitinoCatalog catalog) {
    String catalogProvider = catalog.getProvider();
    CatalogConnectorContext.Builder builder = catalogBuilders.get(catalogProvider);
    if (builder == null) {
      String message = String.format("Unsupported catalog provider %s.", catalogProvider);
      LOG.error(message);
      throw new TrinoException(GRAVITINO_UNSUPPORTED_CATALOG_PROVIDER, message);
    }

    // Avoid using the same builder object to prevent catalog creation errors.
    return builder.clone(catalog);
  }
}
