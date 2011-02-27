package com.j256.ormlite.db;

import java.util.List;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.FieldType;

/**
 * HyberSQL database type information used to create the tables, etc..
 * 
 * @author graywatson
 */
public class HsqldbDatabaseType extends BaseDatabaseType implements DatabaseType {

	private final static String DATABASE_URL_PORTION = "hsqldb";
	private final static String DRIVER_CLASS_NAME = "org.hsqldb.jdbcDriver";
	private final static String DATABASE_NAME = "HSQLdb";

	public boolean isDatabaseUrlThisType(String url, String dbTypePart) {
		return DATABASE_URL_PORTION.equals(dbTypePart);
	}

	@Override
	protected String getDriverClassName() {
		return DRIVER_CLASS_NAME;
	}

	@Override
	public String getDatabaseName() {
		return DATABASE_NAME;
	}

	@Override
	protected void appendLongStringType(StringBuilder sb) {
		sb.append("LONGVARCHAR");
	}

	@Override
	protected void appendBooleanType(StringBuilder sb) {
		sb.append("BIT");
	}

	@Override
	protected void appendByteArrayType(StringBuilder sb) {
		sb.append("BINARY");
	}

	@Override
	protected void configureGeneratedIdSequence(StringBuilder sb, FieldType fieldType, List<String> statementsBefore,
			List<String> additionalArgs, List<String> queriesAfter) {
		// needs to match dropColumnArg()
		StringBuilder seqSb = new StringBuilder();
		seqSb.append("CREATE SEQUENCE ");
		appendEscapedEntityName(seqSb, fieldType.getGeneratedIdSequence());
		if (fieldType.getDataType() == DataType.LONG) {
			seqSb.append(" AS BIGINT");
		} else {
			// integer is the default
		}
		// with hsqldb (as opposed to all else) the sequences start at 0, grumble
		seqSb.append(" START WITH 1");
		statementsBefore.add(seqSb.toString());
		sb.append("GENERATED BY DEFAULT AS IDENTITY ");
		configureId(sb, fieldType, statementsBefore, additionalArgs, queriesAfter);
	}

	@Override
	public void appendEscapedEntityName(StringBuilder sb, String word) {
		sb.append('\"').append(word).append('\"');
	}

	@Override
	public void dropColumnArg(FieldType fieldType, List<String> statementsBefore, List<String> statementsAfter) {
		if (fieldType.isGeneratedIdSequence()) {
			StringBuilder sb = new StringBuilder();
			sb.append("DROP SEQUENCE ");
			appendEscapedEntityName(sb, fieldType.getGeneratedIdSequence());
			statementsAfter.add(sb.toString());
		}
	}

	@Override
	public boolean isIdSequenceNeeded() {
		return true;
	}

	@Override
	public boolean isVarcharFieldWidthSupported() {
		return false;
	}

	@Override
	public boolean isLimitAfterSelect() {
		return true;
	}

	@Override
	public void appendLimitValue(StringBuilder sb, int limit, Integer offset) {
		// the 0 is the offset, could also use TOP X
		sb.append("LIMIT ");
		if (offset == null) {
			sb.append("0 ");
		} else {
			sb.append(offset).append(' ');
		}
		sb.append(limit).append(' ');
	}

	@Override
	public boolean isOffsetLimitArgument() {
		return true;
	}

	@Override
	public void appendOffsetValue(StringBuilder sb, int offset) {
		throw new IllegalStateException("Offset is part of the LIMIT in database type " + getClass());
	}

	@Override
	public void appendSelectNextValFromSequence(StringBuilder sb, String sequenceName) {
		sb.append("CALL NEXT VALUE FOR ");
		appendEscapedEntityName(sb, sequenceName);
	}

	@Override
	public boolean isEntityNamesMustBeUpCase() {
		return true;
	}

	@Override
	public boolean isNestedSavePointsSupported() {
		return false;
	}

	@Override
	protected void appendUnique(StringBuilder sb, FieldType fieldType, List<String> statementsAfter) {
		StringBuilder alterSb = new StringBuilder();
		alterSb.append("ALTER TABLE ");
		appendEscapedEntityName(alterSb, fieldType.getTableName());
		alterSb.append(" ADD UNIQUE (");
		appendEscapedEntityName(alterSb, fieldType.getDbColumnName());
		alterSb.append(");");
		statementsAfter.add(alterSb.toString());
	}

	@Override
	public String getPingStatement() {
		return "SELECT COUNT(*) FROM INFORMATION_SCHEMA.SYSTEM_TABLES";
	}
}
