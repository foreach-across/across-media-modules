package com.foreach.imageserver.core.data.handlers;


import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringToSetHandler implements TypeHandler<Set<String>> {
    @Override
    public void setParameter(PreparedStatement ps, int i, Set<String> parameter, JdbcType jdbcType) throws SQLException {
        String tags = StringUtils.join(parameter, ",");

        ps.setString(i, tags);
    }

    @Override
    public Set<String> getResult(ResultSet rs, String columnName) throws SQLException {
        return split(rs.getString(columnName));
    }

    @Override
    public Set<String> getResult(ResultSet rs, int columnIndex) throws SQLException {
        return split(rs.getString(columnIndex));
    }

    @Override
    public Set<String> getResult(CallableStatement cs, int columnIndex) throws SQLException {
        return split(cs.getString(columnIndex));
    }

    private Set<String> split(String value) {
        return new HashSet<>(Arrays.asList(StringUtils.split(StringUtils.defaultString(value), ",")));
    }
}
