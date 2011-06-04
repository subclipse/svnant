/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package org.tigris.subversion.svnant.commands;

import org.tigris.subversion.svnclientadapter.ISVNLogMessage;
import org.tigris.subversion.svnclientadapter.ISVNLogMessageChangePath;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNRevision;
import org.tigris.subversion.svnclientadapter.SVNUrl;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import java.text.SimpleDateFormat;

/**
 * svn log. 
 * 
 * @author Martin Letenay
 */
public class Log extends SvnCommand {

    private static final String MSG_FAILED_TO_LOAD = "Can't get the content of the specified file";

    private static final String MSG_CANT_GET_LOG_MESSAGES = "Can't get the log messages for the path or url";

    /** destination file. */
    private File        destFile      = null;

    private SVNUrl      url           = null;

    private File        path          = null;

    /** the stop-on-copy flag */
    private boolean     stopOnCopy    = true;

    /** the --xml flag */
    private boolean     asXml         = true;

    /** the --limit */
    private long        limit         = 0;

    /** start revision */
    private SVNRevision startRevision = SVNRevision.HEAD;

    /** stop revision */
    private SVNRevision stopRevision  = new SVNRevision.Number( 1 );

    /** the --verbose flag */
    private boolean     changedpathes       = false;

    /**
     * {@inheritDoc}
     */
    public void execute() {
        if( destFile == null ) {
            destFile = new File( getProject().getBaseDir(), url.getLastPathSegment() );
        }
        ISVNLogMessage[] logMessages = null;
        try {
            if( path != null ) {
                logMessages = getClient().getLogMessages( path, startRevision, stopRevision, stopOnCopy, changedpathes, limit );
            } else {
                logMessages = getClient().getLogMessages( url, startRevision, startRevision, stopRevision, stopOnCopy, changedpathes, limit );
            }
            writeLogMessages( logMessages );
        } catch( SVNClientException ex ) {
            throw ex( ex, MSG_CANT_GET_LOG_MESSAGES );
        }
    }

    private void writeLogMessages( ISVNLogMessage[] logMessages ) {
        if( asXml ) {
            OutputStream outstream = null;
            try {
                outstream = new BufferedOutputStream( new FileOutputStream( destFile ) );
                // Use an actual XML Document to generate the log file to
                // produce valid XML output.
                Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
                doc.setXmlStandalone( true );
                Element log = doc.createElement( "log" );
                doc.appendChild( log );
                for( ISVNLogMessage logMessage : logMessages ) {
                    writeLogEntryAsXml( logMessage, doc, log );
                }

                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.setOutputProperty( OutputKeys.INDENT, "yes" );
                transformer.setOutputProperty( OutputKeys.ENCODING, "UTF-8" );
                transformer.transform( new DOMSource( doc ), new StreamResult( outstream ) );
            } catch( TransformerConfigurationException ex ) {
                throw ex( ex, MSG_FAILED_TO_LOAD );
            } catch( ParserConfigurationException ex ) {
                throw ex( ex, MSG_FAILED_TO_LOAD );
            } catch( TransformerException ex ) {
                throw ex( ex, MSG_FAILED_TO_LOAD );
            } catch( IOException ex ) {
                throw ex( ex, MSG_FAILED_TO_LOAD );
            } finally {
                SvnAntUtilities.close( outstream );
            }
        } else {
            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter( new OutputStreamWriter( new FileOutputStream( destFile ) ) );
                for( int i = 0; i < logMessages.length; i++ ) {
                    writeLogEntryAsPlaintext( logMessages[i], writer );
                }
            } catch( IOException ex ) {
                throw ex( ex, MSG_FAILED_TO_LOAD );
            } finally {
                SvnAntUtilities.close( writer );
            }
        }

    }

    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrsNotSet( "url, path", url, path );
        SvnAntUtilities.attrNotNull( "startRevision", startRevision );
        SvnAntUtilities.attrNotNull( "stopRevision", stopRevision );
    }

    /**
     * Sets the URL; required.
     * @param url The url to set
     */
    public void setUrl( SVNUrl url ) {
        this.url = url;
    }

    /**
     * set the path of the new directory
     * @param path
     */
    public void setPath( File path ) {
        this.path = path;
    }

    /**
     * @param destFile the destFile to set
     */
    public void setDestFile( File destFile ) {
        this.destFile = destFile;
    }

    /**
     * @param startRevision the startRevision to set
     */
    public void setStartRevision( String startRevision ) {
        this.startRevision = getRevisionFrom( startRevision );
    }

    /**
     * @param stopRevision the stopRevision to set
     */
    public void setStopRevision( String stopRevision ) {
        this.stopRevision = getRevisionFrom( stopRevision );
    }

    /**
     * @param stopOnCopy the stopOnCopy to set
     */
    public void setStopOnCopy( boolean stopOnCopy ) {
        this.stopOnCopy = stopOnCopy;
    }

    /**
     * @param asXml the asXml to set
     */
    public void setAsXml( boolean asXml ) {
        this.asXml = asXml;
    }

    /**
     * @param limit the limit to set
     */
    public void setLimit( int limit ) {
        this.limit = limit;
    }

    /**
     * @param changedpathes whether to be verbose or not
     */
    public void setChangedpathes( boolean changedpathes ) {
        this.changedpathes = changedpathes;
    }

    private void writeLogEntryAsPlaintext( ISVNLogMessage logMessage, BufferedWriter writer ) throws IOException {
        //Non-verbose
        //    ------------------------------------------------------------------------
        //    r2233 | markphip | 2006-05-22 22:16:34 +0200 (po, 22 V 2006) | 3 lines
        //
        //    Improve error reporting in Lock/Unlock messages.  It now outputs the erro
        //    message that is created by the client adapter, which includes more info s
        //    as the user that holds the lock on the file.
        //    ------------------------------------------------------------------------
        //    r2221 | markphip | 2006-05-19 17:29:46 +0200 (pi, 19 V 2006) | 1 line
        //
        //    JavaSVN 1.0.5
        //    ------------------------------------------------------------------------

        //Verbose
        //    ------------------------------------------------------------------------
        //    r2233 | markphip | 2006-05-22 22:16:34 +0200 (po, 22 V 2006) | 3 lines
        //    Changed paths:
        //       M /trunk/subclipse/core/lib/svnClientAdapter.jar
        //       M /trunk/svnClientAdapter/src/main/org/tigris/subversion/svnclientadapter/jav
        //    ahl/JhlNotificationHandler.java
        //
        //    Improve error reporting in Lock/Unlock messages.  It now outputs the error
        //    message that is created by the client adapter, which includes more info such
        //    as the user that holds the lock on the file.
        //    ------------------------------------------------------------------------
        //    r2221 | markphip | 2006-05-19 17:29:46 +0200 (pi, 19 V 2006) | 1 line
        //    Changed paths:
        //       M /branches/1.0.x/subclipse/core/lib/javasvn.jar
        //       M /branches/1.0.x/svnClientAdapter/lib/javasvn.jar
        //       M /trunk/subclipse/core/lib/javasvn.jar
        //       M /trunk/svnClientAdapter/lib/javasvn.jar
        //       M /trunk/www/subclipse/changes.html
        //
        //    JavaSVN 1.0.5
        //    ------------------------------------------------------------------------

        writer.write( "------------------------------------------------------------------------" );
        writer.newLine();
        writer.write( 'r' );
        writer.write( logMessage.getRevision().toString() );
        writer.write( " | " );
        writer.write( logMessage.getAuthor() );
        writer.write( " | " );
        writer.write( logMessage.getDate().toString() );
        //    writer.write(" | ");
        //    writer.write();
        //    writer.write(" lines");
        //    if (.length > 1) {
        //      writer.write('s');
        //    }
        if( changedpathes ) {
            writer.write( "Changed paths:" );
            writer.newLine();
            for( ISVNLogMessageChangePath changepathlogmessage : logMessage.getChangedPaths() ) {
                writer.write( "   " + changepathlogmessage.getAction() + " " );
                writer.write( changepathlogmessage.getPath() );
                writer.newLine();
            }
        }

        writer.newLine();
        writer.newLine();
        writer.write( logMessage.getMessage() );
        writer.newLine();
    }

    private void writeLogEntryAsXml( ISVNLogMessage logMessage, Document doc, Element log ) {
        //Non-verbose
        //    <?xml version="1.0" encoding="utf-8"?>
        //    <log>
        //    <logentry
        //       revision="2233">
        //    <author>markphip</author>
        //    <date>2006-05-22T20:16:34.198898Z</date>
        //    <msg>Improve error reporting in Lock/Unlock messages.  It now outputs the error
        //    message that is created by the client adapter, which includes more info such
        //    as the user that holds the lock on the file.</msg>
        //    </logentry>
        //    <logentry
        //       revision="2221">
        //    <author>markphip</author>
        //    <date>2006-05-19T15:29:46.078330Z</date>
        //    <msg>JavaSVN 1.0.5</msg>
        //    </logentry>
        //    </log>

        //Verbose
        //    <?xml version="1.0" encoding="utf-8"?>
        //    <log>
        //    <logentry
        //       revision="2233">
        //    <author>markphip</author>
        //    <date>2006-05-22T20:16:34.198898Z</date>
        //    <paths>
        //    <path
        //       action="M">/trunk/svnClientAdapter/src/main/org/tigris/subversion/svnclientad
        //    apter/javahl/JhlNotificationHandler.java</path>
        //    <path
        //       action="M">/trunk/subclipse/core/lib/svnClientAdapter.jar</path>
        //    </paths>
        //    <msg>Improve error reporting in Lock/Unlock messages.  It now outputs the error
        //    message that is created by the client adapter, which includes more info such
        //    as the user that holds the lock on the file.</msg>
        //    </logentry>
        //    <logentry
        //       revision="2221">
        //    <author>markphip</author>
        //    <date>2006-05-19T15:29:46.078330Z</date>
        //    <paths>
        //    <path
        //       action="M">/trunk/svnClientAdapter/lib/javasvn.jar</path>
        //    <path
        //       action="M">/branches/1.0.x/subclipse/core/lib/javasvn.jar</path>
        //    <path
        //       action="M">/branches/1.0.x/svnClientAdapter/lib/javasvn.jar</path>
        //    <path
        //       action="M">/trunk/www/subclipse/changes.html</path>
        //    <path
        //       action="M">/trunk/subclipse/core/lib/javasvn.jar</path>
        //    </paths>
        //    <msg>JavaSVN 1.0.5</msg>
        //    </logentry>
        //    </log>

        Element entry = doc.createElement( "logentry" );
        entry.setAttribute( "revision", logMessage.getRevision().toString() );
        log.appendChild( entry );

        Element author = doc.createElement( "author" );
        entry.appendChild( author );
        Text authorText = doc.createTextNode( logMessage.getAuthor() );
        author.appendChild( authorText );

        Element date = doc.createElement( "date" );
        entry.appendChild( date );
        Text dateText = doc.createTextNode( new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" ).format( logMessage
                        .getDate() ) );
        date.appendChild( dateText );

        if( changedpathes ) {
            Element paths = doc.createElement( "paths" );
            entry.appendChild( paths );
            for( ISVNLogMessageChangePath changedPath : logMessage.getChangedPaths() ) {
                Element path = doc.createElement( "path" );
                paths.appendChild( path );
                path.setAttribute( "action", String.valueOf( changedPath.getAction() ) );
                Text pathText = doc.createTextNode( changedPath.getPath() );
                path.appendChild( pathText );
            }
        }

        Element msg = doc.createElement( "msg" );
        entry.appendChild( msg );
        Text msgText = doc.createTextNode( logMessage.getMessage() );
        msg.appendChild( msgText );
    }

}
