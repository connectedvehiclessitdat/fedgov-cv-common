package gov.usdot.cv.common.database.mongodb.dao;

import gov.usdot.cv.common.database.mongodb.criteria.DateRange;
import gov.usdot.cv.common.database.mongodb.geospatial.Geometry;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

public abstract class AbstractQuerySitDataDao extends AbstractMongoDbDao {
	private static final String GEO_WITHIN_OPERATOR 	= "$geoWithin";
	private static final String GEO_INTERSECTS_OPERATOR = "$geoIntersects";
	private static final String GEOMETRY_OPERATOR 		= "$geometry";
	private static final String EXISTS_OPERATOR			= "$exists";
	private static final String AND_OPERATOR			= "$and";
	private static final String GTE_OPERATOR			= "$gte";
	private static final String LTE_OPERATOR			= "$lte";
	
	protected AbstractQuerySitDataDao(Mongo mongo, String dbname) {
		super(mongo, dbname);
	}
	
	/**
	 * Builds the date range db object.
	 * 
	 * { <field name> : { $gte : <start-time>, $lte : <end-time> } }
	 */
	protected DBObject buildDateRangeExpression(DateRange dateRange) {
		if (dateRange == null) return null;
		if (dateRange.getFieldName() == null) return null;
		if (! dateRange.hasStartTime() && ! dateRange.hasEndTime()) return null;
		BasicDBObject expr = new BasicDBObject();
		if (dateRange.hasStartTime()) 	expr.put(GTE_OPERATOR, dateRange.getStartTime());
		if (dateRange.hasEndTime()) 	expr.put(LTE_OPERATOR, dateRange.getEndTime());
		return new BasicDBObject(dateRange.getFieldName(), expr);
	}
	
	/**
	 * Builds the orderby db object.
	 * 
	 * { <field name> : [1|-1] }
	 */
	protected DBObject buildOrderByExpression(String fieldName, boolean asc) {
		int value = (asc) ? 1 : -1;
		return new BasicDBObject(fieldName, value);
	}
	
	/**
	 * Builds a $geoWithin query expression.
	 * 
	 * { <geospatial field> :
     *     { $geoWithin :
     *         { $geometry :
     *             { type : "<GeoJSON object type>" ,
     *               coordinates : [ [ [ <lng1>, <lat1> ] , [ <lng2>, <lat2> ] ... ] ]
     * } } } }
	 */
	protected DBObject buildGeoWithinExpression(String fieldName, Geometry geometry) {
		return 
			new BasicDBObject(fieldName, 
				new BasicDBObject(GEO_WITHIN_OPERATOR, 
					new BasicDBObject(GEOMETRY_OPERATOR, 
						geometry.toJSONObject())));
	}
	
	/**
	 * Builds a $geoIntersects query expression.
	 * 
	 * { <geospatial field> :
     *     { $geoIntersects :
     *         { $geometry :
     *             { type : "<GeoJSON object type>" ,
     *               coordinates : [ <coordinates> ]
     * } } } }
	 */
	protected DBObject buildGeoIntersectsExpression(String fieldName, Geometry geometry) {
		return 
			new BasicDBObject(fieldName, 
				new BasicDBObject(GEO_INTERSECTS_OPERATOR, 
					new BasicDBObject(GEOMETRY_OPERATOR, 
						geometry.toJSONObject())));
	}
	
	/**
	 * Builds a $exists query expression.
	 * 
	 * { <field name> : { $exists : <true|false> } }
	 */
	protected DBObject buildExistsExpression(String fieldName, boolean value) {
		return 
			new BasicDBObject(fieldName, 
				new BasicDBObject(EXISTS_OPERATOR, value));
	}
	
	/**
	 * Builds a $and query expression.
	 * 
	 * { $and : [ {<expr1>}, {<expr2>}, ... ] }
	 */
	protected DBObject and(DBObject... exps) {
		BasicDBList list = new BasicDBList();
		for (DBObject exp : exps) {
			if (exp != null) list.add(exp);
		}
		return new BasicDBObject(AND_OPERATOR, list);
	}
}