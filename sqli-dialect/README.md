# IoTDB dialect

`IoTDBDialect` adds Apache IoTDB support to sqli's dialect extension point. It targets IoTDB's SQL table model and reports the dialect key `iotdb`.

Register it alongside the application's default dialect when using `DynamicDialect`:

```java
DynamicDialect dialect = new DynamicDialect();
dialect.setDefaultDialect(new MySqlDialect());
dialect.addDialect(new IoTDBDialect());
```

The dialect uses IoTDB-compatible `INSERT`, `UPDATE`, `DELETE`, `LIMIT`/`OFFSET`, and JDBC temporal values. Define the table and its schema in IoTDB before using repositories; sqli generates data statements and does not provision IoTDB tables. Map the entity's event-time property to the table model's `time` column and provide the other columns in the IoTDB table schema.

`createOrReplace()` and `createOrReplaceBatch()` are unsupported because IoTDB has no MySQL `REPLACE` equivalent. Use `create()` for inserts or an explicit update operation. Tree-model paths can be queried with native SQL, but the repository's general entity CRUD contract targets the table model.
