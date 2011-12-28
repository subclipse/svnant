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
package org.tigris.subversion.svnant;

import org.tigris.subversion.svnant.commands.Add;
import org.tigris.subversion.svnant.commands.Cat;
import org.tigris.subversion.svnant.commands.Checkout;
import org.tigris.subversion.svnant.commands.Cleanup;
import org.tigris.subversion.svnant.commands.Commit;
import org.tigris.subversion.svnant.commands.Copy;
import org.tigris.subversion.svnant.commands.CreateRepository;
import org.tigris.subversion.svnant.commands.Delete;
import org.tigris.subversion.svnant.commands.Diff;
import org.tigris.subversion.svnant.commands.DiffSummarize;
import org.tigris.subversion.svnant.commands.Export;
import org.tigris.subversion.svnant.commands.Feedback;
import org.tigris.subversion.svnant.commands.Ignore;
import org.tigris.subversion.svnant.commands.Import;
import org.tigris.subversion.svnant.commands.Info;
import org.tigris.subversion.svnant.commands.Keywordsadd;
import org.tigris.subversion.svnant.commands.Keywordsremove;
import org.tigris.subversion.svnant.commands.Keywordsset;
import org.tigris.subversion.svnant.commands.Log;
import org.tigris.subversion.svnant.commands.Mkdir;
import org.tigris.subversion.svnant.commands.Move;
import org.tigris.subversion.svnant.commands.Propdel;
import org.tigris.subversion.svnant.commands.Propget;
import org.tigris.subversion.svnant.commands.Propset;
import org.tigris.subversion.svnant.commands.Revert;
import org.tigris.subversion.svnant.commands.SingleInfo;
import org.tigris.subversion.svnant.commands.Status;
import org.tigris.subversion.svnant.commands.SvnCommand;
import org.tigris.subversion.svnant.commands.Switch;
import org.tigris.subversion.svnant.commands.Update;
import org.tigris.subversion.svnant.commands.WcVersion;

import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNNotifyListener;

import org.apache.tools.ant.types.Reference;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.Task;

import java.util.ArrayList;
import java.util.List;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Svn Task
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 * @author Daniel Kasmeroglu 
 *         <a href="mailto:daniel.kasmeroglu@kasisoft.net">Daniel.Kasmeroglu@kasisoft.net</a>
 */
public class SvnTask extends Task {
    
    private List<SvnCommand>         commands        = new ArrayList<SvnCommand>();
    private List<ISVNNotifyListener> notifyListeners = new ArrayList<ISVNNotifyListener>();
    private CharArrayWriter          writer          = new CharArrayWriter();
    private PrintWriter              printer         = new PrintWriter( writer );
    private PrintWriter              logprinter      = null;
    private File                     logfile         = null;

    /**
     * Specifies a location of the log file used to write the output to.
     * 
     * @param newlogfile   The location of the logfile used to write the output to.
     */
    public void setLogFile( File newlogfile ) {
        logfile = newlogfile;
    }
    
    /**
     * {@inheritDoc}
     */
    public ProjectComponent getProjectComponent() {
        return this;
    }

    /**
     * Sets the referred configuration to be used for the svn task.
     *
     * @param refid   The id of the configuration to be used for the svn task.
     */
    public void setRefid( Reference refid ) {
        SvnFacade.setRefid( this, refid );
    }

    /**
     * Adds the <code>checkout</code> command to this task.
     * 
     * @param a   The <code>checkout</code> command. Not <code>null</code>.
     */
    public void addCheckout( Checkout a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>singleinfo</code> command to this task.
     * 
     * @param a   The <code>singleinfo</code> command. Not <code>null</code>.
     */
    public void addSingleinfo( SingleInfo a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>list</code> command to this task.
     * 
     * @param a   The <code>list</code> command. Not <code>null</code>.
     */
    public void addList( org.tigris.subversion.svnant.commands.List a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>add</code> command to this task.
     * 
     * @param a   The <code>add</code> command. Not <code>null</code>.
     */
    public void addAdd( Add a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>cleanup</code> command to this task.
     * 
     * @param a   The <code>cleanup</code> command. Not <code>null</code>.
     */
    public void addCleanup( Cleanup a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>commit</code> command to this task.
     * 
     * @param a   The <code>commit</code> command. Not <code>null</code>.
     */
    public void addCommit( Commit a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>copy</code> command to this task.
     * 
     * @param a   The <code>copy</code> command. Not <code>null</code>.
     */
    public void addCopy( Copy a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>delete</code> command to this task.
     * 
     * @param a   The <code>delete</code> command. Not <code>null</code>.
     */
    public void addDelete( Delete a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>export</code> command to this task.
     * 
     * @param a   The <code>export</code> command. Not <code>null</code>.
     */
    public void addExport( Export a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>info</code> command to this task.
     * 
     * @param a   The <code>info</code> command. Not <code>null</code>.
     */
    public void addInfo( Info a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>import</code> command to this task.
     * 
     * @param a   The <code>import</code> command. Not <code>null</code>.
     */
    public void addImport( Import a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>log</code> command to this task.
     * 
     * @param a   The <code>log</code> command. Not <code>null</code>.
     */
    public void addLog( Log a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>mkdir</code> command to this task.
     * 
     * @param a   The <code>mkdir</code> command. Not <code>null</code>.
     */
    public void addMkdir( Mkdir a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>move</code> command to this task.
     * 
     * @param a   The <code>move</code> command. Not <code>null</code>.
     */
    public void addMove( Move a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>update</code> command to this task.
     * 
     * @param a   The <code>update</code> command. Not <code>null</code>.
     */
    public void addUpdate( Update a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>propset</code> command to this task.
     * 
     * @param a   The <code>propset</code> command. Not <code>null</code>.
     */
    public void addPropset( Propset a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>diff</code> command to this task.
     * 
     * @param a   The <code>diff</code> command. Not <code>null</code>.
     */
    public void addDiff( Diff a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>diffSummarize</code> command to this task.
     * 
     * @param a   The <code>diffSummarize</code> command. Not <code>null</code>.
     */
    public void addDiffSummarize( DiffSummarize a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>keywordsSet</code> command to this task.
     * 
     * @param a   The <code>keywordsSet</code> command. Not <code>null</code>.
     */
    public void addKeywordsSet( Keywordsset a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>keywordsAdd</code> command to this task.
     * 
     * @param a   The <code>keywordsAdd</code> command. Not <code>null</code>.
     */
    public void addKeywordsAdd( Keywordsadd a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>keywordsRemove</code> command to this task.
     * 
     * @param a   The <code>keywordsRemove</code> command. Not <code>null</code>.
     */
    public void addKeywordsRemove( Keywordsremove a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>revert</code> command to this task.
     * 
     * @param a   The <code>revert</code> command. Not <code>null</code>.
     */
    public void addRevert( Revert a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>cat</code> command to this task.
     * 
     * @param a   The <code>cat</code> command. Not <code>null</code>.
     */
    public void addCat( Cat a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>propdel</code> command to this task.
     * 
     * @param a   The <code>propdel</code> command. Not <code>null</code>.
     */
    public void addPropdel( Propdel a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>ignore</code> command to this task.
     * 
     * @param a   The <code>ignore</code> command. Not <code>null</code>.
     */
    public void addIgnore( Ignore a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>createRepository</code> command to this task.
     * 
     * @param a   The <code>createRepository</code> command. Not <code>null</code>.
     */
    public void addCreateRepository( CreateRepository a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>wcVersion</code> command to this task.
     * 
     * @param a   The <code>wcVersion</code> command. Not <code>null</code>.
     */
    public void addWcVersion( WcVersion a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>status</code> command to this task.
     * 
     * @param a   The <code>status</code> command. Not <code>null</code>.
     */
    public void addStatus( Status a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>switch</code> command to this task.
     * 
     * @param a   The <code>switch</code> command. Not <code>null</code>.
     */
    public void addSwitch( Switch a ) {
        addCommand( a );
    }

    /**
     * Adds the <code>propget</code> command to this task.
     * 
     * @param a   The <code>propget</code> command. Not <code>null</code>.
     */
    public void addPropget( Propget a ) {
        addCommand( a );
    }

    /**
     * Adds the supplied command to this task.
     * 
     * @param cmd   The command which has to be added. Not <code>null</code>.
     */
    private void addCommand( SvnCommand cmd ) {
        cmd.setTask( this );
        commands.add( cmd );
    }

    /**
     * Registers the supplied listener.
     * 
     * @param notifyListener   The listener used to be registered to receive notifications. 
     *                         Not <code>null</code>.
     */
    public void addNotifyListener( ISVNNotifyListener notifyListener ) {
        notifyListeners.add( notifyListener );
    }

    /**
     * Modifies the supplied arguments so a Throwable instance is converted into a textual 
     * representation.
     * 
     * @param args   The arguments that are supposed to be altered. Maybe <code>null</code>.
     * 
     * @return   The supplied potentially changed arguments. Maybe <code>null</code>.
     */
    private Object[] alter( Object[] args ) {
        if( args != null ) {
            for( int i = 0; i < args.length; i++ ) {
                if( args[i] instanceof Throwable ) {
                    writer.reset();
                    Throwable t = (Throwable) args[i];
                    t.printStackTrace( printer );
                    args[i]     = String.valueOf( writer.toCharArray() );
                }
            }
        }
        return args;
    }

    /**
     * Dumps some verbose messages.
     * 
     * @param fmt    A formatting String. Not <code>null</code>.
     * @param args   The arguments for the formatting String. Maybe <code>null</code>.
     */
    public void verbose( String fmt, Object ... args ) {
        if( (args == null) || (args.length == 0) ) {
            write( fmt, Project.MSG_VERBOSE );
        } else {
            write( String.format( fmt, alter( args ) ), Project.MSG_VERBOSE );
        }
    }

    /**
     * Dumps some debug messages.
     * 
     * @param fmt    A formatting String. Not <code>null</code>.
     * @param args   The arguments for the formatting String. Maybe <code>null</code>.
     */
    public void debug( String fmt, Object ... args ) {
        if( (args == null) || (args.length == 0) ) {
            write( fmt, Project.MSG_DEBUG );
        } else {
            write( String.format( fmt, alter( args ) ), Project.MSG_DEBUG );
        }
    }
    
    /**
     * Dumps some warning messages.
     * 
     * @param fmt    A formatting String. Not <code>null</code>.
     * @param args   The arguments for the formatting String. Maybe <code>null</code>.
     */
    public void warning( String fmt, Object ... args ) {
        if( (args == null) || (args.length == 0) ) {
            write( fmt, Project.MSG_WARN );
        } else {
            write( String.format( fmt, alter( args ) ), Project.MSG_WARN );
        }
    }

    /**
     * Dumps some info messages.
     * 
     * @param verbose   <code>true</code> <=> Consider this message to be a verbose one.
     * @param fmt       A formatting String. Not <code>null</code>.
     * @param args      The arguments for the formatting String. Maybe <code>null</code>.
     */
    public void info( boolean verbose, String fmt, Object ... args ) {
        if( (args == null) || (args.length == 0) ) {
            write( fmt, verbose ? Project.MSG_VERBOSE : Project.MSG_INFO );
        } else {
            write( String.format( fmt, alter( args ) ), verbose ? Project.MSG_VERBOSE : Project.MSG_INFO );
        }
    }

    /**
     * Dumps some info messages.
     * 
     * @param fmt    A formatting String. Not <code>null</code>.
     * @param args   The arguments for the formatting String. Maybe <code>null</code>.
     */
    public void info( String fmt, Object ... args ) {
        if( (args == null) || (args.length == 0) ) {
            write( fmt, Project.MSG_INFO );
        } else {
            write( String.format( fmt, alter( args ) ), Project.MSG_INFO );
        }
    }

    /**
     * Dumps some error messages.
     * 
     * @param fmt    A formatting String. Not <code>null</code>.
     * @param args   The arguments for the formatting String. Maybe <code>null</code>.
     */
    public void error( String fmt, Object ... args ) {
        if( (args == null) || (args.length == 0) ) {
            write( fmt, Project.MSG_ERR );
        } else {
            write( String.format( fmt, alter( args ) ), Project.MSG_ERR );
        }
    }
    
    /**
     * Writes a message to the log.
     * 
     * @param message   The message which has to be written. Neither <code>null</code> nor empty.
     * @param level     The error level.
     */
    private void write( String message, int level ) {
        if( logprinter != null ) {
            logprinter.println( message );
        } else {
            log( message, level );
        }
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws BuildException {

        try {
            if( logfile != null ) {
                logprinter = new PrintWriter( new FileWriter( logfile ) );
            }
            executeImpl();
        } catch( Exception ex ) {

            if( SvnFacade.getFailonerror( this ) ) {

                if( ex instanceof BuildException ) {
                    throw (BuildException) ex;
                } else {
                    throw new BuildException( ex );
                }

            } else {
                // quit normally but we're dumping the exception so the user will notice it
                error( "the execution failed for some reason. cause: %s", ex );
            }
            
        } finally {
            if( logprinter != null ) {
                logprinter.close();
            }
        }

    }

    /**
     * Implementation of this task.
     */
    private void executeImpl() {

        ISVNClientAdapter svnClient = SvnFacade.getClientAdapter( this );

        for( int i = 0; i < notifyListeners.size(); i++ ) {
            svnClient.addNotifyListener( notifyListeners.get( i ) );
        }

        for( int i = 0; i < commands.size(); i++ ) {
            SvnCommand command  = commands.get( i );
            Feedback   feedback = new Feedback( command );
            svnClient.addNotifyListener( feedback );
            command.executeCommand( svnClient );
            svnClient.removeNotifyListener( feedback );
        }

    }

}
