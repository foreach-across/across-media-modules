package liquibase.ext;

import liquibase.database.Database;
import liquibase.database.core.OracleDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.IntType;

@DataTypeInfo(name = "realOracleInteger", minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT + 1)
public class RealOracleInteger extends IntType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof OracleDatabase) {
            return new DatabaseDataType("INTEGER");
        } else {
            return super.toDatabaseDataType(database);
        }
    }
}
