package liquibase.ext;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;

@DataTypeInfo(name = "varchar", aliases = {"java.sql.Types.VARCHAR", "java.lang.String", "varchar2"}, minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT + 1)
public class VarcharType extends liquibase.datatype.core.VarcharType {
    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {
        if (database instanceof HsqlDatabase) {
            Object[] parameters = getParameters();
            if (parameters.length == 0 || !(parameters[0] instanceof String)) {
                throw new RuntimeException("Varchar is expected to have a size parameter.");
            }

            String sizeParameter = (String) parameters[0];
            sizeParameter = sizeParameter.replaceAll("(?i)char", "");

            return new DatabaseDataType("VARCHAR", sizeParameter);
        } else {
            return super.toDatabaseDataType(database);
        }
    }
}
