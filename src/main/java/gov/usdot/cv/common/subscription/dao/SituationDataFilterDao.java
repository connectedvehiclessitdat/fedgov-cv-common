package gov.usdot.cv.common.subscription.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.bson.Document;

import com.deleidos.rtws.commons.dao.exception.DataRetrievalException;
import com.deleidos.rtws.commons.dao.jdbc.DataAccessSession;
import com.deleidos.rtws.commons.dao.jdbc.DataAccessUtil;
import com.deleidos.rtws.commons.dao.jdbc.DefaultStatementHandler;
import com.deleidos.rtws.commons.dao.jdbc.RecordBuilder;

import gov.usdot.cv.common.model.BoundingBox;
import gov.usdot.cv.common.model.Filter;

public class SituationDataFilterDao {
	private static final String DATABASE_TYPE 			= "APPLICATION";
	private static final String TABLE_NAME 				= "SITUATION_DATA_FILTER";
	private static final String SUBSCRIBER_ID_COL_NAME 	= "ID";
	private static final String END_TIME_COL_NAME 		= "END_TIME";
	private static final String TYPE_COL_NAME 			= "TYPE";
	private static final String TYPE_VALUE_COL_NAME 	= "TYPE_VALUE";
	private static final String REQUEST_ID_COL_NAME 	= "REQUEST_ID";
	private static final String NW_LAT_COL_NAME 		= "NW_LAT";
	private static final String NW_LON_COL_NAME 		= "NW_LON";
	private static final String SE_LAT_COL_NAME 		= "SE_LAT";
	private static final String SE_LON_COL_NAME 		= "SE_LON";
	
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
		
		public SituationDataFilterDao build() {
			if (this.dataSource == null) {
				throw new NullPointerException("Data source is null.");
			}
			
			if (this.tableName == null) {
				this.tableName = TABLE_NAME;
			}
			
			SituationDataFilterDao dao = new SituationDataFilterDao();
			dao.tableName = this.tableName;
			dao.session = DataAccessUtil.session(this.dataSource);
			dao.selectSql = String.format("SELECT %s, %s, %s, %s, %s, %s, %s, %s, %s FROM %s.%s;", 
				SUBSCRIBER_ID_COL_NAME,
				END_TIME_COL_NAME,
				TYPE_COL_NAME,
				TYPE_VALUE_COL_NAME,
				REQUEST_ID_COL_NAME,
				NW_LAT_COL_NAME,
				NW_LON_COL_NAME,
				SE_LAT_COL_NAME,
				SE_LON_COL_NAME,
				DATABASE_TYPE,
				dao.tableName);
			return dao;
		}
	}

	private SituationDataFilterDao() {
		// Perform DAO initialization work here.
	}
	
	/**
	 * Returns all the records from the SITUATION_DATA_FILTER table.
	 */
	@SuppressWarnings("deprecation")
	public Collection<Filter> findAll() {
		return this.session.executeMultiRowQuery(selectSql, null, new FilterBuilder());
	}
	
	/**
	 * Given a subscriber id, locate the record in the SITUATION_DATA_FILTER table and return it.
	 */
	@SuppressWarnings("deprecation")
	public Filter findById(int subscriberId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(DATABASE_TYPE).append(".").append(this.tableName);
		sql.append(" WHERE ").append(SUBSCRIBER_ID_COL_NAME).append("=").append(subscriberId);
		
		return this.session.executeSingleRowQuery(sql.toString(), null, new FilterBuilder());
	}
	
	/**
	 * Inserts a subscription into the SITUATION_DATA_FILTER table.
	 */
	@SuppressWarnings("deprecation")
	public void insert(int subscriberId, Filter filter) {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(DATABASE_TYPE).append('.').append(this.tableName);
		sql.append(" (");
		sql.append(SUBSCRIBER_ID_COL_NAME).append(", ");
		sql.append(END_TIME_COL_NAME).append(", ");
		sql.append(TYPE_COL_NAME).append(", ");
		sql.append(TYPE_VALUE_COL_NAME).append(", ");
		sql.append(REQUEST_ID_COL_NAME).append(", ");
		sql.append(NW_LAT_COL_NAME).append(", ");
		sql.append(NW_LON_COL_NAME).append(", ");
		sql.append(SE_LAT_COL_NAME).append(", ");
		sql.append(SE_LON_COL_NAME).append(") ");
		sql.append(" VALUES (?,?,?,?,?,?,?,?,?);");
		
		DefaultStatementHandler handler = null;
		if (filter.getBoundingBox() == null) {
			handler = new DefaultStatementHandler(new Object [] { 
				subscriberId, 
				filter.getEndTime().getTime(), 
				filter.getType(),
				filter.getTypeValue(),
				filter.getRequestId(),
				null, null, null, null
			});
		} else {
			handler = new DefaultStatementHandler(new Object [] { 
				subscriberId, 
				filter.getEndTime().getTime(), 
				filter.getType(),
				filter.getTypeValue(),
				filter.getRequestId(),
				filter.getBoundingBox().getNWLat(), 
				filter.getBoundingBox().getNWLon(), 
				filter.getBoundingBox().getSELat(), 
				filter.getBoundingBox().getSELon()
			});
		}
		
		this.session.executeStatement(sql.toString(), handler);
	}
	
	/**
	 * Deletes a subscription from the SITUATION_DATA_FILTER table.
	 */
	@SuppressWarnings("deprecation")
	public void delete(int subscriberId, int requestId) {
		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(DATABASE_TYPE).append('.').append(this.tableName);
		sql.append(" WHERE ").append(SUBSCRIBER_ID_COL_NAME).append("=").append(subscriberId);
		sql.append(" AND ").append(REQUEST_ID_COL_NAME).append("=").append(requestId).append(";");
		
		this.session.executeStatement(sql.toString(), null);
	}
	
	/**
	 * Inner class used to populate beans with data from a result set.
	 */
	protected static final class FilterBuilder implements RecordBuilder<Filter> {
		public Filter buildRecord(ResultSet result) {
			try {
				BoundingBox.Builder boxBuilder = new BoundingBox.Builder();
				
				String value = result.getString(NW_LAT_COL_NAME);
				if (value != null) boxBuilder.setNWLat(Double.parseDouble(value));
				value = result.getString(NW_LON_COL_NAME);
				if (value != null) boxBuilder.setNWLon(result.getDouble(NW_LON_COL_NAME));
				value = result.getString(SE_LAT_COL_NAME);
				if (value != null) boxBuilder.setSELat(result.getDouble(SE_LAT_COL_NAME));
				value = result.getString(SE_LON_COL_NAME);
				if (value != null) boxBuilder.setSELon(result.getDouble(SE_LON_COL_NAME));
				
				Calendar end = Calendar.getInstance(TimeZone.getTimeZone(Filter.UTC_TIMEZONE));
				end.setTimeInMillis(result.getTimestamp(END_TIME_COL_NAME).getTime());
				
				Filter.Builder filterBuilder = new Filter.Builder();
				filterBuilder.setSubscriberId(result.getInt(SUBSCRIBER_ID_COL_NAME));
				filterBuilder.setEndTime(end);
				filterBuilder.setType(result.getString(TYPE_COL_NAME));
				filterBuilder.setTypeValue(result.getInt(TYPE_VALUE_COL_NAME));
				filterBuilder.setRequestId(result.getInt(REQUEST_ID_COL_NAME));
				filterBuilder.setBoundingBox(boxBuilder.build());
				return filterBuilder.build();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new DataRetrievalException(e);
			}
		}

		@Override
		public Filter buildRecord(Document result) {
			// TODO Auto-generated method stub
			return null;
		}
	}
}