package support.ext;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.datatype.core.IntType;

@DataTypeInfo(name = "int", aliases = { "integer", "java.sql.Types.INTEGER", "java.lang.Integer", "serial" },
              minParameters = 0, maxParameters = 1, priority = LiquibaseDataType.PRIORITY_DEFAULT + 1)
public class IntAsBigDecimalType extends IntType
{
	@Override
	public DatabaseDataType toDatabaseDataType( Database database ) {
		if ( database instanceof HsqlDatabase ) {
			return new DatabaseDataType( "NUMERIC" );
		}
		else {
			return super.toDatabaseDataType( database );
		}
	}
}
