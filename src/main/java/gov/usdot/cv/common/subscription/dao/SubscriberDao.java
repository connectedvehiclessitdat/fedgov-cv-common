package gov.usdot.cv.common.subscription.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import org.bson.Document;

import com.deleidos.rtws.commons.dao.exception.DataRetrievalException;
import com.deleidos.rtws.commons.dao.jdbc.DataAccessSession;
import com.deleidos.rtws.commons.dao.jdbc.DataAccessUtil;
import com.deleidos.rtws.commons.dao.jdbc.DefaultStatementHandler;
import com.deleidos.rtws.commons.dao.jdbc.RecordBuilder;
import com.deleidos.rtws.commons.dao.type.sql.SqlTypeHandler;

import gov.usdot.cv.common.model.Subscriber;

public class SubscriberDao {
	private static final String DATABASE_TYPE 			= "APPLICATION";
	private static final String TABLE_NAME 				= "SUBSCRIBER";
	private static final String SUBSCRIBER_ID_COL_NAME 	= "ID";
	private static final String CERTIFICATE_COL_NAME 	= "CERTIFICATE";
	private static final String TARGET_HOST_COL_NAME 	= "TARGET_HOST";
	private static final String TARGET_PORT_COL_NAME 	= "TARGET_PORT";
	
	private String tableName;
	private String selectSql; 

	/** Utility instance used to manage connections. */
	private DataAccessSession session;
	
	public static class Builder {
		private DataSource dataSource;
		private String tableName;
		
		public Builder setDataSource(DataSource dataSource) {
			this.dataSource = dataSource;
			return this;
		}
		
		public Builder setTableName(String tableName) {
			this.tableName = tableName;
			return this;
		}
		
		public SubscriberDao build() {
			if (this.dataSource == null) {
				throw new NullPointerException("Data source is null.");
			}
			
			if (this.tableName == null) {
				this.tableName = TABLE_NAME;
			}
			
			SubscriberDao dao = new SubscriberDao();
			dao.tableName = this.tableName;
			dao.session = DataAccessUtil.session(this.dataSource);
			dao.selectSql = String.format("SELECT %s, %s, %s, %s FROM %s.%s;", 
				SUBSCRIBER_ID_COL_NAME,
				CERTIFICATE_COL_NAME,
				TARGET_HOST_COL_NAME,
				TARGET_PORT_COL_NAME,
				DATABASE_TYPE,
				dao.tableName);
			return dao;
		}
	}
	
	private SubscriberDao() {
		// Perform DAO initialization work here.
	}
	
	/**
	 * Returns all the records from the SUBSCRIBER table.
	 */
	@SuppressWarnings("deprecation")
	public Collection<Subscriber> findAll() {
		return this.session.executeMultiRowQuery(selectSql, null, new SubscriberBuilder());
	}
	
	/**
	 * Given a subscriber id, locate the record in the SUBSCRIBER table and return it.
	 */
	@SuppressWarnings("deprecation")
	public Subscriber findById(int subscriberId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(DATABASE_TYPE).append(".").append(this.tableName);
		sql.append(" WHERE ").append(SUBSCRIBER_ID_COL_NAME).append("=").append(subscriberId).append(";");
		
		return this.session.executeSingleRowQuery(sql.toString(), null, new SubscriberBuilder());
	}
	
	/**
	 * Inserts the subscriber id and public certificate 
	 * into the SUBSCRIBER table.
	 */
	@SuppressWarnings("deprecation")
	public void insert(
			int subscriberId, 
			Subscriber subscriber) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(DATABASE_TYPE).append(".").append(this.tableName);
		sql.append(" (");
		sql.append(SUBSCRIBER_ID_COL_NAME).append(", ");
		sql.append(CERTIFICATE_COL_NAME).append(", ");
		sql.append(TARGET_HOST_COL_NAME).append(", ");
		sql.append(TARGET_PORT_COL_NAME).append(") ");
		sql.append(" VALUES (?,?,?,?)");
		
		DefaultStatementHandler handler = new DefaultStatementHandler(
			new Object [] { subscriberId, subscriber.getCertificate(), subscriber.getDestHost(), subscriber.getDestPort() });
		
		this.session.executeStatement(sql.toString(), handler);
	}
	
	@SuppressWarnings("deprecation")
	public void update(
			int subscriberId, 
			Subscriber subscriber) {
		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(DATABASE_TYPE).append(".").append(this.tableName);
		sql.append(" SET ").append(CERTIFICATE_COL_NAME).append(" = ? ");
		sql.append(" , ").append(TARGET_HOST_COL_NAME).append(" = ? ");
		sql.append(" , ").append(TARGET_PORT_COL_NAME).append(" = ? ");
		sql.append("WHERE ").append(SUBSCRIBER_ID_COL_NAME).append(" = ?");

		DefaultStatementHandler handler = new DefaultStatementHandler(
			new Object [] { subscriber.getCertificate(), subscriber.getDestHost(), subscriber.getDestPort(), subscriberId });
		
		this.session.executeStatement(sql.toString(), handler);
	}
	
	/**
	 * Check to see if the subscriber id already exists in
	 * the SUBSCRIBER table. If not than the subscriber
	 * id and public certificate will be added else the
	 * certificate will be updated.
	 */
	public void upsert(
			int subscriberId, 
			Subscriber subscriber) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT count(*) FROM ").append(DATABASE_TYPE).append(".").append(this.tableName);
		sql.append(" WHERE ").append(SUBSCRIBER_ID_COL_NAME).append("=").append(subscriberId).append(";");
		
		@SuppressWarnings("deprecation")
		int count = this.session.executeSingleValueQuery(sql.toString(), null, SqlTypeHandler.INTEGER);
		if (count == 0) {
			insert(subscriberId, subscriber);
		} else {
			update(subscriberId, subscriber);
		}
	}
	
	/**
	 * Deletes a subscription from the SUBSCRIBER table.
	 */
	@SuppressWarnings("deprecation")
	public void delete(int subscriberId) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(DATABASE_TYPE).append('.').append(this.tableName);
		sql.append(" WHERE ").append(SUBSCRIBER_ID_COL_NAME).append(" = ").append(subscriberId).append(';');
		
		this.session.executeStatement(sql.toString(), null);
	}
	
	/**
	 * Inner class used to populate beans with data from a result set.
	 */
	protected static final class SubscriberBuilder implements RecordBuilder<Subscriber> {
		public Subscriber buildRecord(ResultSet result) {
			try {
				Subscriber.Builder builder = new Subscriber.Builder();
				builder.setSubscriberId(result.getInt(SUBSCRIBER_ID_COL_NAME));
				builder.setCertificate(result.getBytes(CERTIFICATE_COL_NAME));
				builder.setDestHost(result.getString(TARGET_HOST_COL_NAME));
				builder.setDestPort(result.getInt(TARGET_PORT_COL_NAME));
				return builder.build();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DataRetrievalException(e);
			}
		}

		@Override
		public Subscriber buildRecord(Document result) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}