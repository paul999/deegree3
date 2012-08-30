//$HeadURL$
/*----------------------------------------------------------------------------
 This file is part of deegree, http://deegree.org/
 Copyright (C) 2001-2009 by:
 - Department of Geography, University of Bonn -
 and
 - lat/lon GmbH -

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
package org.deegree.services.csw.exporthandling;

import static org.deegree.protocol.csw.CSWConstants.CSW_202_DISCOVERY_SCHEMA;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_NS;
import static org.deegree.protocol.csw.CSWConstants.CSW_202_RECORD;
import static org.deegree.protocol.csw.CSWConstants.CSW_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.DC_LOCAL_PART;
import static org.deegree.protocol.csw.CSWConstants.DC_NS;
import static org.deegree.protocol.csw.CSWConstants.DC_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.GMD_LOCAL_PART;
import static org.deegree.protocol.csw.CSWConstants.GMD_NS;
import static org.deegree.protocol.csw.CSWConstants.GMD_PREFIX;
import static org.deegree.protocol.csw.CSWConstants.VERSION_202;
import static org.deegree.services.i18n.Messages.get;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.deegree.commons.tom.ows.Version;
import org.deegree.commons.utils.kvp.InvalidParameterValueException;
import org.deegree.commons.xml.CommonNamespaces;
import org.deegree.commons.xml.XMLAdapter;
import org.deegree.commons.xml.stax.SchemaLocationXMLStreamWriter;
import org.deegree.metadata.persistence.MetadataStore;
import org.deegree.metadata.persistence.MetadataStoreException;
import org.deegree.protocol.csw.CSWConstants;
import org.deegree.protocol.csw.CSWConstants.OutputSchema;
import org.deegree.services.controller.exception.ControllerException;
import org.deegree.services.controller.ows.OWSException;
import org.deegree.services.controller.utils.HttpResponseBuffer;
import org.deegree.services.csw.CSWController;
import org.deegree.services.csw.CSWService;
import org.deegree.services.csw.describerecord.DescribeRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines the export functionality for a {@link DescribeRecord} request.
 * <p>
 * NOTE:<br>
 * Due to the architecture of this CSW implementation there should be a typeName available which recordStore is
 * requested. But in the describeRecord operation there exists the possibility to get all the recordStores without any
 * typeName available. So at the moment there is a HACK for this UseCase.
 * 
 * @see CSWController
 * 
 * @author <a href="mailto:thomas@lat-lon.de">Steffen Thomas</a>
 * @author last edited by: $Author: thomas $
 * 
 * @version $Revision: $, $Date: $
 */
public class DescribeRecordHandler {

    private static final Logger LOG = LoggerFactory.getLogger( DescribeRecordHandler.class );

    private final CSWService service;

    /**
     * Creates a new {@link DescribeRecordHandler} instance that uses the given service to lookup the
     * {@link MetadataStore}s.
     * 
     * @param service
     */
    public DescribeRecordHandler( CSWService service ) {
        this.service = service;

    }

    /**
     * Preprocessing for the export of a {@link DescribeRecord} request to determine which recordstore is requested.
     * 
     * @param descRec
     *            the parsed describeRecord request
     * @param response
     *            for the servlet request to the client
     * @throws XMLStreamException
     * @throws IOException
     * @throws OWSException
     */
    public void doDescribeRecord( DescribeRecord descRec, HttpResponseBuffer response, boolean isSoap )
                            throws XMLStreamException, IOException, OWSException {

        QName[] typeNames = descRec.getTypeNames();

        Version version = descRec.getVersion();
        response.setContentType( descRec.getOutputFormat() );

        XMLStreamWriter xmlWriter = getXMLResponseWriter( response, null );

        try {
            export( xmlWriter, typeNames, version, isSoap );
        } catch ( MetadataStoreException e ) {
            LOG.debug( e.getMessage() );
            throw new OWSException( e.getMessage(), ControllerException.NO_APPLICABLE_CODE );
        }
        xmlWriter.flush();

    }

    /**
     * 
     * Exports the correct recognized request and determines to which version export it should delegate it.
     * 
     * @param writer
     *            to write the XML to
     * @param record
     *            the recordStore that is requested
     * @throws XMLStreamException
     * @throws MetadataStoreException
     */
    private static void export( XMLStreamWriter writer, QName[] typeNames, Version version, boolean isSoap )
                            throws XMLStreamException, MetadataStoreException {

        if ( VERSION_202.equals( version ) ) {
            export202( writer, typeNames, isSoap );
        } else {
            throw new IllegalArgumentException( "Version '" + version + "' is not supported." );
        }

    }

    /**
     * Exporthandling for the CSW version 2.0.2. <br>
     * It is a container for zero or more SchemaComponent elements.
     * 
     * @param writer
     * @param record
     * @throws XMLStreamException
     * @throws MetadataStoreException
     */
    private static void export202( XMLStreamWriter writer, QName[] typeNames, boolean isSoap )
                            throws XMLStreamException, MetadataStoreException {

        writer.setDefaultNamespace( CSW_202_NS );
        writer.setPrefix( CSW_PREFIX, CSW_202_NS );
        writer.writeStartElement( CSW_202_NS, "DescribeRecordResponse" );
        writer.writeAttribute( "xsi", CommonNamespaces.XSINS, "schemaLocation", CSW_202_NS + " "
                                                                                + CSW_202_DISCOVERY_SCHEMA );

        URLConnection urlConn = null;
        try {
            if ( typeNames.length == 0 ) {
                urlConn = new URL( CSWConstants.CSW_202_RECORD ).openConnection();
                exportSchemaFile( writer, new QName( DC_NS, DC_LOCAL_PART, DC_PREFIX ), urlConn );
                exportISO( writer );
            }
            for ( QName typeName : typeNames ) {

                /*
                 * if typeName is csw:Record
                 */
                if ( OutputSchema.determineByTypeName( typeName ) == OutputSchema.DC ) {

                    urlConn = new URL( CSW_202_RECORD ).openConnection();
                    exportSchemaFile( writer, typeName, urlConn );

                }

                /*
                 * if typeName is gmd:MD_Metadata
                 */
                else if ( OutputSchema.determineByTypeName( typeName ) == OutputSchema.ISO_19115 ) {
                    exportISO( writer );
                }
                /*
                 * if the typeName is no registered in this recordprofile
                 */
                else {
                    String errorMessage = "The typeName " + typeName + "is not supported. ";
                    LOG.debug( errorMessage );
                    throw new InvalidParameterValueException( errorMessage );
                }

            }
        } catch ( IOException e ) {

            LOG.debug( "error: " + e.getMessage(), e );
            throw new MetadataStoreException( e.getMessage() );
        } catch ( Exception e ) {

            LOG.debug( "error: " + e.getMessage(), e );
        }

        writer.writeEndElement();// DescribeRecordResponse
        writer.writeEndDocument();
    }

    private static void exportISO( XMLStreamWriter writer )
                            throws XMLStreamException, UnsupportedEncodingException {
        InputStream in_data = DescribeRecordHandler.class.getResourceAsStream( "iso_data.xml" );
        InputStream in_service = DescribeRecordHandler.class.getResourceAsStream( "iso_service.xml" );
        InputStreamReader isr = null;

        if ( in_data != null ) {
            isr = new InputStreamReader( in_data, "UTF-8" );
            exportSchemaComponent( writer, new QName( GMD_NS, GMD_LOCAL_PART, GMD_PREFIX ), isr );
        } else {
            String msg = get( "CSW_NO_FILE", "iso_data.xml" );
            LOG.debug( msg );
        }

        if ( in_service != null ) {
            isr = new InputStreamReader( in_service, "UTF-8" );
            exportSchemaComponent( writer, new QName( CSWConstants.SRV_NS, CSWConstants.SRV_LOCAL_PART,
                                                      CSWConstants.SRV_PREFIX ), isr );
        } else {
            String msg = get( "CSW_NO_FILE", "iso_service.xml" );
            LOG.debug( msg );
        }

    }

    private static void exportSchemaFile( XMLStreamWriter writer, QName typeName, URLConnection urlConn )
                            throws IOException, XMLStreamException {
        // urlConn.setDoInput( true );
        BufferedInputStream bais = new BufferedInputStream( urlConn.getInputStream() );

        // Charset charset = encoding == null ? Charset.defaultCharset() : Charset.forName( encoding );
        InputStreamReader isr = new InputStreamReader( bais, "UTF-8" );

        exportSchemaComponent( writer, typeName, isr );
    }

    /**
     * SchemaCompontent which encapsulates the requested xml schema.
     * 
     * @param writer
     * @param record
     * @param typeName
     *            that corresponds to the requested {@link MetadataStore}
     * @throws XMLStreamException
     */
    private static void exportSchemaComponent( XMLStreamWriter writer, QName typeName, InputStreamReader isr )
                            throws XMLStreamException {

        writer.writeStartElement( CSW_202_NS, "SchemaComponent" );

        // required, by default XMLSCHEMA
        writer.writeAttribute( "schemaLanguage", "XMLSCHEMA" );
        // required
        writer.writeAttribute( "targetNamespace", typeName.getNamespaceURI() );

        /*
         * optional parentSchema. This is handled in the recordStore in the describeRecord operation because it is a
         * record profile specific value.
         */
        // writer.writeAttribute( "parentSchema", "" );

        XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader( isr );
        xmlReader.nextTag();
        XMLAdapter.writeElement( writer, xmlReader );

        xmlReader.close();

        writer.writeEndElement();// SchemaComponent

    }

    /**
     * Returns an <code>XMLStreamWriter</code> for writing an XML response document.
     * 
     * @param writer
     *            writer to write the XML to, must not be null
     * @param schemaLocation
     *            allows to specify a value for the 'xsi:schemaLocation' attribute in the root element, must not be null
     * 
     * @return {@link XMLStreamWriter}
     * @throws XMLStreamException
     * @throws IOException
     */
    static XMLStreamWriter getXMLResponseWriter( HttpResponseBuffer writer, String schemaLocation )
                            throws XMLStreamException, IOException {

        if ( schemaLocation == null ) {
            return writer.getXMLWriter();
        }
        return new SchemaLocationXMLStreamWriter( writer.getXMLWriter(), schemaLocation );
    }

}