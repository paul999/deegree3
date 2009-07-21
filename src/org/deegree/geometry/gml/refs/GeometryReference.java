//$HeadURL: svn+ssh://mschneider@svn.wald.intevation.org/deegree/base/trunk/resources/eclipse/files_template.xml $
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 Department of Geography, University of Bonn
 and
 lat/lon GmbH

 This library is free software; you can redistribute it and/or modify it under
 the terms of the GNU Lesser General Public License as published by the Free
 Software Foundation; either version 2.1 of the License, or (at your option)
 any later version.
 This library is distributed in the hope that it will be useful, but WITHOUT
 ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 details.
 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation, Inc.,
 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

 Contact information:

 lat/lon GmbH
 Aennchenstr. 19, 53177 Bonn
 Germany
 http://lat-lon.de/

 Department of Geography, University of Bonn
 Prof. Dr. Klaus Greve
 Postfach 1147, 53001 Bonn
 Germany
 http://www.geographie.uni-bonn.de/deegree/

 e-mail: info@deegree.org
 ----------------------------------------------------------------------------*/

package org.deegree.geometry.gml.refs;

import org.deegree.commons.types.gml.StandardObjectProperties;
import org.deegree.crs.CRS;
import org.deegree.geometry.Envelope;
import org.deegree.geometry.Geometry;
import org.deegree.geometry.precision.PrecisionModel;
import org.deegree.geometry.uom.ValueWithUnit;

/**
 * Represents a reference to the GML representation of a geometry, which is usually expressed using an
 * <code>xlink:href</code> attribute in GML (may be document-local or remote). </p>
 * 
 * @author <a href="mailto:schneider@lat-lon.de">Markus Schneider</a>
 * @author last edited by: $Author: schneider $
 * 
 * @version $Revision: $, $Date: $
 */
public class GeometryReference implements Geometry {

    protected String href;

    private final String gid;

    protected Geometry geometry;

    public GeometryReference( String href ) {
        this.href = href;
        int pos = href.lastIndexOf( '#' );
        if ( pos < 0 ) {
            // TODO support remote references as well
            String msg = "Reference string (='" + href + "') does not contain a '#' character.";
            throw new IllegalArgumentException( msg );
        }
        gid = href.substring( pos + 1 );
    }

    public void resolve( Geometry geometry ) {
        if ( this.geometry != null ) {
            String msg = "Internal error: Geometry reference (" + href + ") has already been resolved.";
            throw new RuntimeException( msg );
        }
        this.geometry = geometry;
    }

    public boolean contains( Geometry geometry ) {
        return geometry.contains( geometry );
    }

    public Geometry difference( Geometry geometry ) {
        return geometry.difference( geometry );
    }

    public double distance( Geometry geometry ) {
        return geometry.distance( geometry );
    }

    public boolean equals( Geometry geometry ) {
        return geometry.equals( geometry );
    }

    public Geometry getBuffer( ValueWithUnit distance ) {
        return geometry.getBuffer( distance );
    }

    public Geometry getConvexHull() {
        return geometry.getConvexHull();
    }

    public int getCoordinateDimension() {
        return geometry.getCoordinateDimension();
    }

    public CRS getCoordinateSystem() {
        return geometry.getCoordinateSystem();
    }

    public Envelope getEnvelope() {
        return geometry.getEnvelope();
    }

    public GeometryType getGeometryType() {
        return geometry.getGeometryType();
    }

    public String getId() {
        return gid;
    }

    public PrecisionModel getPrecision() {
        return geometry.getPrecision();
    }

    public Geometry intersection( Geometry geometry ) {
        return geometry.intersection( geometry );
    }

    public boolean intersects( Geometry geometry ) {
        return geometry.intersects( geometry );
    }

    public boolean isBeyond( Geometry geometry, ValueWithUnit distance ) {
        return geometry.isBeyond( geometry, distance );
    }

    public boolean isWithin( Geometry geometry ) {
        return geometry.isWithin( geometry );
    }

    public boolean isWithinDistance( Geometry geometry, ValueWithUnit distance ) {
        return geometry.isWithinDistance( geometry, distance );
    }

    public Geometry union( Geometry geometry ) {
        return geometry.union( geometry );
    }

    @Override
    public com.vividsolutions.jts.geom.Geometry getJTSGeometry() {
        return geometry.getJTSGeometry();
    }

    @Override
    public StandardObjectProperties getStandardGMLProperties() {
        return geometry.getStandardGMLProperties();
    }

    @Override
    public void setStandardGMLProperties( StandardObjectProperties standardProps ) {
        geometry.setStandardGMLProperties( standardProps );
    }
}
