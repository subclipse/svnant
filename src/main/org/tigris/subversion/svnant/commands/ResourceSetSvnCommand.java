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

import org.tigris.subversion.svnclientadapter.utils.SVNStatusUtils;

import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;

import org.tigris.subversion.svnant.SvnAntUtilities;

import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.FileSet;

import org.apache.tools.ant.DirectoryScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import java.io.File;

/**
 * This base implementation of an SvnCommand is supposed to provide basic functionalities for
 * resource related operations.
 *
 * @author Daniel Kasmeroglu (Daniel.Kasmeroglu@kasisoft.net)
 */
public class ResourceSetSvnCommand extends SvnCommand {

    private static final String MSG_FAILED_TO_CALCULATE_UNMANAGED = "Failed to calculate unmanaged entries.";
    private File            dir;
    private File            file;
    private boolean         recurse;
    private boolean         incfiledirs;
    private List<FileSet>   filesets;
    private List<DirSet>    dirsets;
    private SVNStatusKind   unmanageddirs;
    private boolean         scanunmanaged;

    /**
     * Initialises this base command with the supplied defaults.
     * 
     * @param defrecurse   <code>true</code> <=> The default value for the recurse flag.
     * @param filedirs     <code>true</code> <=> Include directories implied by FileSet.
     */
    ResourceSetSvnCommand( boolean defrecurse, boolean filedirs ) {
        this( defrecurse, filedirs, false, null );
    }
    
    /**
     * Initialises this base command with the supplied defaults.
     * 
     * @param defrecurse   <code>true</code> <=> The default value for the recurse flag.
     * @param filedirs     <code>true</code> <=> Include directories implied by FileSet.
     * @param scan         <code>true</code> <=> Scan unmanaged resources.
     * @param unmanaged    Specifies the way unmanaged resources will be handled. If 
     *                     <code>null</code> a directory is considered to be unmanaged when it's 
     *                     not part of the repository. If not <code>null</code> a resource is 
     *                     considered unmanaged when it's part of the repository and has the 
     *                     supplied status. 
     */
    ResourceSetSvnCommand( boolean defrecurse, boolean filedirs, boolean scan, SVNStatusKind unmanaged ) {
        recurse         = defrecurse;
        incfiledirs     = filedirs;
        unmanageddirs   = unmanaged;
        scanunmanaged   = scan;
        dir             = null;
        file            = null;
        filesets        = new ArrayList<FileSet>();
        dirsets         = new ArrayList<DirSet>();
    }
    
    /**
     * Returns a list of directories from the DirSets.
     * 
     * @param unmanaged   A receiver for unmanaged directories. Not <code>null</code>.
     * 
     * @return   A list of directories from the DirSets. Not <code>null</code>.
     */
    private File[] collectDirectories( List<File> unmanaged ) {
        List<File> result = new ArrayList<File>();
        if( dir != null ) {
            result.add( dir );
        }
        for( DirSet dirset : dirsets ) {
            collect( result, unmanaged, dirset );
        }
        return result.toArray( new File[ result.size() ] );
    }
    
    /**
     * Returns a list of files from the FileSets (potentially including directories depending
     * on the parameterisation of the constructor).
     * 
     * @param unmanaged   A receiver for unmanaged directories. Not <code>null</code>.
     * 
     * @return   A list of files from the FileSets. Not <code>null</code>.
     */
    private File[] collectFiles( List<File> unmanaged ) {
        List<File> result = new ArrayList<File>();
        if( file != null ) {
            result.add( file );
        }
        for( FileSet fileset : filesets ) {
            collect( result, unmanaged, fileset );
        }
        return result.toArray( new File[ result.size() ] );
    }
    
    /**
     * Collects all files from the supplied FileSet.
     * 
     * @param receiver    The Collection to receive the files (and implied directories). Not <code>null</code>.
     * @param unmanaged   The Collection to receive unmanaged directories. Not <code>null</code>.
     * @param fileset     The FileSet which has to be traversed. Not <code>null</code>.
     */
    private void collect( List<File> receiver, List<File> unmanaged, FileSet fileset ) {
        
        DirectoryScanner scanner = fileset.getDirectoryScanner( getProject() );
        File             dir     = fileset.getDir( getProject() );
        
        
        if( incfiledirs ) {
            // we're including directories which are implied by the FileSet
            String[] dirs   = scanner.getIncludedDirectories();
            for( String includeddir : dirs ) {
                File directory = new File( dir, includeddir );
                collectUnmanaged( unmanaged, dir, directory, false );
                receiver.add( directory );
            }
        }
        
        String[]         files   = scanner.getIncludedFiles();
        for( String includedfile : files ) {
            File file = new File( dir, includedfile );
            collectUnmanaged( unmanaged, dir, file.getParentFile(), false );
            receiver.add( file );
        }
    }

    /**
     * Collects all directories from the supplied DirSet.
     * 
     * @param receiver    The Collection to receive the directories (and implied directories). Not <code>null</code>.
     * @param unmanaged   The Collection to receive unmanaged directories. Not <code>null</code>.
     * @param dirset      The DirSet which has to be traversed. Not <code>null</code>.
     */
    private void collect( List<File> receiver, List<File> unmanaged, DirSet dirset ) {
        DirectoryScanner scanner = dirset.getDirectoryScanner( getProject() );
        File             dir     = dirset.getDir( getProject() );
        String[]         dirs    = scanner.getIncludedDirectories();
        for( String includeddir : dirs ) {
            File directory = new File( dir, includeddir );
            collectUnmanaged( unmanaged, dir, directory, true );
            receiver.add( directory );
        }
    }

    /**
     * Collect all unmanaged directories.
     * 
     * @param receiver     The Collection which will be extended. Not <code>null</code>.
     * @param basedir      The base directory of the FileSet/DirSet. Not <code>null</code>.
     * @param dir          The current directory to be handled. Not <code>null</code>.
     * @param skipfirst    <code>true</code> <=> This directory is explicitly selected, so it
     *                     shall not be considered an unmanaged version.
     */
    private void collectUnmanaged( List<File> receiver, File basedir, File dir, boolean skipfirst ) {
        if( ! scanunmanaged ) {
            // if unwanted by the concrete implementation we don't collect unmmanaged entries
            // which is a speed improvement due to the reduction of svn client actions
            return;
        }
        try {
            // iterate through the parental hierarchy until we find a managed source or the base dir
            Stack<File> parents = new Stack<File>();
            ISVNStatus  status  = getClient().getSingleStatus( dir );
            while( (dir != null) && (! dir.equals( basedir )) ) {
                if( unmanageddirs == null ) {
                    // no specific status has been set, so we're leaving if we've found
                    // a managed resource
                    if( SVNStatusUtils.isManaged( status ) ) {
                        break;
                    }
                } else {
                    if( status.getTextStatus() != unmanageddirs ) {
                        // un expected status which indicates, to stop here
                        break;
                    }
                }
                parents.push( dir );
                dir     = dir.getParentFile();
                status  = getClient().getSingleStatus( dir );
            }
            
            // store all implied resources
            int count = skipfirst ? 1 : 0;
            while( parents.size() > count ) {
                File unmanaged = parents.pop();
                if( ! receiver.contains( unmanaged ) ) { 
                    receiver.add( unmanaged );
                }
            }
            
        } catch( SVNClientException ex ) {
            throw ex( ex, MSG_FAILED_TO_CALCULATE_UNMANAGED );
        }
        
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() {
        SvnAntUtilities.attrsNotSet( "file, dir, fileset, dirset", file, dir, filesets, dirsets );
        if( file != null ) {
            SvnAntUtilities.attrIsFile( "file", file );
        }
        if( dir != null ) {
            SvnAntUtilities.attrIsDirectory( "dir", dir );
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @note [04-Jun-2011:KASI]   The call of any handle-method is not guaranteed. Each implementation
     *                            is allowed to fail immediately.
     */
    public final void execute() {
        
        // just for the start
        handleBegin();
        
        // collect all data
        List<File> unmanaged   = new ArrayList<File>();
        File[]     directories = collectDirectories ( unmanaged );
        File[]     files       = collectFiles       ( unmanaged );

        // process unmanaged directories first
        for( File dir : unmanaged ) {
            handleUnmanaged( dir );
        }
        
        // process selected directories
        for( File dir : directories ) {
            handleDir( dir, recurse );
        }
        
        // process selected directories (implied by the filesets)
        for( File file : files ) {
            if( file.isDirectory() ) {
                // this directory is included as part of the fileset, so recursion
                // is disabled here
                handleDir( file, false );
            }
        }
        
        // process files
        for( File file : files ) {
            if( ! file.isDirectory() ) {
                handleFile( file );
            }
        }
        
        // just the end
        handleEnd();
        
    }
    
    /**
     * Will be invoked directly at the beginning.
     */
    protected void handleBegin() {
    }
    
    /**
     * Will be invoked with the unmanaged directory.
     * 
     * @param dir   The unmanaged directory. Not <code>null</code>.
     */
    protected void handleUnmanaged( File dir ) {
    }
    
    /**
     * Will be invoked with the selected directory.
     * 
     * @param dir       The selected directory. Not <code>null</code>.
     * @param recurse   <code>true</code> <=> Enable recursion for this operation if supported.
     */
    protected void handleDir( File dir, boolean recurse ) {
    }

    /**
     * Will be invoked with the selected file.
     * 
     * @param file   The selected file. Not <code>null</code>.
     */
    protected void handleFile( File file ) {
    }
    
    /**
     * Will be invoked when all other handle methods have been finished without an error.
     */
    protected void handleEnd() {
    }

    /**
     * Set the file to handle.
     * 
     * @param file
     */
    public void setFile( File file ) {
        this.file = file;
    }

    /**
     * set the directory to handle
     * @param dir
     */
    public void setDir( File dir ) {
        this.dir = dir;
    }

    /**
     * Changes whether directories will be treated recursively or not.
     *  
     * @param recurse   <code>true</code> <=> Handle directories recursively.
     */
    protected void setRecurse( boolean recurse ) {
        this.recurse = recurse;
    }

    /**
     * Adds a set of files to the list.
     * 
     * @param set   A set of files that have to be added. Not <code>null</code>.
     */
    public void addFileset( FileSet set ) {
        filesets.add( set );
    }

    /**
     * Adds a FileSet in general to this command.
     * 
     * @param set   The FileSet (svnFileSet) which will be added. Not <code>null</code>.
     */
    public void add( FileSet set ) {
        filesets.add( set );
    }
    
    /**
     * Adds a set of directories to the list.
     * 
     * @param set   A set of directories that have to be added. Not <code>null</code>.
     */
    public void addDirset( DirSet set ) {
        dirsets.add( set );
    }

}
