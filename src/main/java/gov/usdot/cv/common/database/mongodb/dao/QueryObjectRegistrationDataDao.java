package gov.usdot.cv.common.database.mongodb.dao;

import gov.usdot.cv.common.database.mongodb.MongoClientBuilder;
import gov.usdot.cv.common.database.mongodb.geospatial.Geometry;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;

/**
 * Data access object to perform queries on the Object Registration
 * data collection.
 */
public class QueryObjectRegistrationDataDao extends AbstractQuerySitDataDao {	
	private final Logger logger = Logger.getLogger(getClass());
	
	public static QueryObjectRegistrationDataDao newInstance(
			String mongoServerHost, 
			int mongoServerPort, 
			MongoOptions options,
			String dbname) throws UnknownHostException {
		MongoClientBuilder builder = new MongoClientBuilder();
		builder.setHost(mongoServerHost).setPort(mongoServerPort).setMongoOptions(options);
		return new QueryObjectRegistrationDataDao(builder.build(), dbname);
	}
	
	private QueryObjectRegistrationDataDao(Mongo mongo, String dbname) {
		super(mongo, dbname);
	}
	
	/**
	 * Returns a list of Object Registration data documents. The query will look for all documents 
	 * with a polygon that is within the given geometry. If the result set limit has not
	 * been reached it will look for all documents with a polygon that intersects with the given
	 * geometry. The results will be combined and returned back.
	 */
	public Collection<DBObject> findAll(
			String collectionName,
			int serviceId,
			String geoSpatialFieldName,
			Geometry geometry,
			String orderByFieldName,
			int limit) {
		List<DBObject> within = findWithin(
				collectionName, 
				serviceId,
				geoSpatialFieldName,
				geometry, 
				orderByFieldName,
				limit);
		
		if (within.size() == limit) return within;
		
		int remaining = limit - within.size();
		
		// The same data may be returned using the intersects query so
		// we double the limit size for the intersects query to ensure
		// we populate the result set with the specified limit size.
		
		if (within.size() > 0) remaining *= 2;
		
		List<DBObject> intersects = findIntersects(
				collectionName, 
				serviceId,
				geoSpatialFieldName,
				geometry,
				orderByFieldName,
				remaining);
		
		if (intersects.size() == 0) return within;
		
		Set<DBObject> result = new HashSet<DBObject>();
		result.addAll(within);
		for (DBObject dbObj : intersects) {
			result.add(dbObj);
			if (result.size() >= limit)
				break;
		}
		return result;
	}
	
	/**
	 * Returns a list of situation data documents. The query will look for all documents 
	 * with a polygon that is within the given geometry. It will cap the result with the
	 * supplied limit value.
	 */
	public List<DBObject> findWithin(
		String collectionName,
		int serviceId,
		String geoSpatialFieldName,
		Geometry geometry,
		String orderByFieldName,
		int limit) {
		DBCollection collection = get(collectionName);
		
		DBObject idFilter = new BasicDBObject("serviceId", serviceId);
		DBObject orderBy = buildOrderByExpression(orderByFieldName, false);
		DBObject within = buildGeoWithinExpression(geoSpatialFieldName, geometry);
		DBObject query = and(idFilter, within);
		
		logger.debug(String.format("Executing query: %s", query.toString()));
		
		DBCursor cursor = collection.find(query).sort(orderBy).limit(limit);
		
		List<DBObject> result = new ArrayList<DBObject>();
		
		try {
			while (cursor.hasNext()) {
				result.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		
		return result;
	}
	
	/**
	 * Returns a list of situation data documents. The query will look for all documents 
	 * with a polygon that intersects with the given geometry. It will cap the result with the
	 * supplied limit value.
	 */
	public List<DBObject> findIntersects(
		String collectionName,
		int serviceId,
		String geoSpatialFieldName,
		Geometry geometry, 
		String orderByFieldName,
		int limit) {
		DBCollection collection = get(collectionName);
		
		DBObject idFilter = new BasicDBObject("serviceId", serviceId);
		DBObject orderBy = buildOrderByExpression(orderByFieldName, false);
		DBObject intersects = buildGeoIntersectsExpression(geoSpatialFieldName, geometry);
		DBObject query = and(idFilter, intersects);

		logger.debug(String.format("Executing query: %s", query.toString()));
		
		DBCursor cursor = collection.find(query).sort(orderBy).limit(limit);
		
		List<DBObject> result = new ArrayList<DBObject>();
		
		try {
			while (cursor.hasNext()) {
				result.add(cursor.next());
			}
		} finally {
			cursor.close();
		}
		
		return result;
	}
}