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

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.SVNRevision;

import org.tigris.subversion.svnant.SvnTask;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;

import java.util.Date;
import java.util.TimeZone;

import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * All ant svn commands inherits from this abstract class
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 */
public abstract class SvnCommand extends ProjectComponent {

    private  SvnTask            task;
    private ISVNClientAdapter   svnClient;

    protected abstract void validateAttributes();

    protected ISVNClientAdapter getClient() {
        return svnClient;
    }
    
    public String getCommandname() {
        return getClass().getSimpleName();
    }
    
    /**
     * Execute the command.
     */
    public abstract void execute();

    /**
     * Execute the receiver (a svn command) using the supplied clientAdapter
     * @param svnClientAdapter
     * @throws BuildException
     */
    public final void executeCommand( ISVNClientAdapter svnClientAdapter ) throws BuildException {
        this.svnClient = svnClientAdapter;
        info( "<%s> started ...", getCommandname() );
        try {
            validateAttributes();
            execute();
            info( "<%s> finished.", getCommandname() );
        } catch( BuildException ex ) {
            error( "<%s> failed.", getCommandname() );
            throw ex;
        } catch( Exception ex ) {
            error( "<%s> failed.", getCommandname() );
            throw new BuildException(ex);
        }
    }

    /**
     * @return the task
     */
    public SvnTask getTask() {
        return task;
    }

    /**
     * @param task the task to set
     */
    public void setTask( SvnTask task ) {
        this.task = task;
    }

    /**
     * Convert the revision string to SVNRevision.
     * In case the date was supplied as revision, use proper dateFormatter
     * @param revision
     * @return SVNRevision constructed from given string or null if unable to do so 
     */
    public SVNRevision getRevisionFrom( String revision ) {
        try {
            return SVNRevision.getRevision( revision, getDateFormatter() );
        } catch( ParseException e ) {
            warning( "Unable to parse revision string" );
            return null;
        }
    }

    /**
     * Answer a given date as string formatted according to current formatter
     * @param aDate
     * @return a String representation of the date
     */
    public String getDateStringFor( Date aDate ) {
        return getDateFormatter().format( aDate );
    }

    private SimpleDateFormat getDateFormatter() {
        final SimpleDateFormat formatter = new SimpleDateFormat( task.getDateFormatter() );
        final TimeZone timezone = task.getDateTimezone();
        if( timezone != null ) {
            formatter.setTimeZone( timezone );
        }
        return formatter;
    }

    /**
     * @see SvnTask#verbose(String, Object...)
     */
    public void verbose( String fmt, Object... args ) {
        task.verbose( fmt, args );
    }

    /**
     * @see SvnTask#debug(String, Object...)
     */
    public void debug( String fmt, Object... args ) {
        task.debug( fmt, args );
    }

    /**
     * @see SvnTask#warning(String, Object...)
     */
    public void warning( String fmt, Object... args ) {
        task.warning( fmt, args );
    }

    /**
     * @see SvnTask#info(boolean, String, Object...)
     */
    public void info( boolean verbose, String fmt, Object... args ) {
        task.info( verbose, fmt, args );
    }

    /**
     * @see SvnTask#info(String, Object...)
     */
    public void info( String fmt, Object... args ) {
        task.info( fmt, args );
    }

    /**
     * @see SvnTask#error(String, Object...)
     */
    public void error( String fmt, Object... args ) {
        task.error( fmt, args );
    }

    /**
     * {@inheritDoc}
     */
    public void log( String message ) {
        info( message );
    }

    /**
     * {@inheritDoc}
     */
    public void log( String message, int level ) {
        switch( level ) {
        case Project.MSG_DEBUG      : debug( message ); break;
        case Project.MSG_ERR        : error( message ); break;
        case Project.MSG_INFO       : info( message ); break;
        case Project.MSG_VERBOSE    : verbose( message ); break;
        case Project.MSG_WARN       : warning( message ); break;
        }
    }

    /**
     * A simple helper used to create an exception.
     * 
     * @param fmt    The formatting string to be used. Neither <code>null</code> nor empty.
     * @param args   Some arguments for the formatting string.
     * 
     * @return   The BuildException indicating the error.
     */
    protected BuildException createEx( String fmt, Object ... args ) {
        return new BuildException( String.format( fmt, args ) );
    }
    
}
