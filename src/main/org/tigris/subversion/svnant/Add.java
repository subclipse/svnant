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

import java.io.File;
import java.util.Stack;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.tigris.subversion.javahl.ClientException;
import org.tigris.subversion.svnclientadapter.SVNClientAdapter;

/**
 * svn Add. Add a file, a directory or a set of files to repository
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class Add extends SvnCommand {
    /** file to add to the repository */
    private File file = null;

    /** filesets to add to the repository */
    private Vector filesets = new Vector();

    /** do not fail when file or directory to add is not found */
    private boolean failonerror = false;

    /** directory to add to the repository */
    private File dir = null;

    /** add recursively ? (only for dir attribute) */
    private boolean recurse = true;

    private SVNClientAdapter svnClient;

    public void execute(SVNClientAdapter svnClient) throws BuildException {
        this.svnClient = svnClient;
        validateAttributes();

		log("Svn : Putting files and directories under revision control :");

        // deal with the single file
        if (file != null) {
            svnAddFile(file);
        }

        // deal with a directory
        if (dir != null) {
            svnAddDir(dir, recurse);
        }

        // deal with filesets
        if (filesets.size() > 0) {
            for (int i = 0; i < filesets.size(); i++) {
                FileSet fs = (FileSet) filesets.elementAt(i);
                svnAddFileSet(fs);
            }
        }

    }

    /**
     * Ensure we have a consistent and legal set of attributes
     */
    protected void validateAttributes() throws BuildException {

        if ((file == null) && (dir == null) && (filesets.size() == 0))
            throw new BuildException("file, url or fileset must be set");
    }

    /**
     * add a file to the repository
     * @param svnClient
     * @param file
     * @throws BuildException
     */
    private void svnAddFile(File file) throws BuildException {
        if (file.exists()) {
            if (file.isDirectory()) {
                log(
                    "Directory "
                        + file.getAbsolutePath()
                        + " cannot be added using the file attribute.  "
                        + "Use dir instead.");
            } else {
                try {
                    svnClient.addFile(file);
                } catch (Exception e) {
                    throw new BuildException(
                        "Can't add file "
                            + file.getAbsolutePath()
                            + " to repository",
                        e);
                }
            }
        } else {
            String message =
                "Warning: Could not find file "
                    + file.getAbsolutePath()
                    + " to add to the repository.";
            if (!failonerror) {
                log(message);
            } else {
                throw new BuildException(message);
            }
        }
    }

    /**
     * add a directory to the repository
     * @param svnClient
     * @param dir
     * @param recursive
     * @throws BuildException
     */
    private void svnAddDir(File dir, boolean recursive) throws BuildException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                log(
                    "File "
                        + dir.getAbsolutePath()
                        + " cannot be added using the dir attribute.  "
                        + "Use file instead.");
            } else {

                try {
                    svnClient.addDirectory(dir, recursive);
                } catch (Exception e) {
                    throw new BuildException(
                        "Can't add directory "
                            + dir.getAbsolutePath()
                            + " to repository",
                        e);
                }
            }
        } else {
            String message =
                "Warning: Could not find directory "
                    + dir.getAbsolutePath()
                    + " to add to the repository.";
            if (!failonerror) {
                log(message);
            } else {
                throw new BuildException(message);
            }
        }

    }

    /**
     * add the file (or directory) to the repository, including any necessary parent directories
     * @param svnClient
     * @param file
     * @param baseDir
     * @throws BuildException
     */
    private void svnAddFileWithDirs(File file, File baseDir)
        throws BuildException {

        Stack dirs = new Stack();
        File currentDir = file.getParentFile();
        
        try {
			// don't add the file if already added ...
			if (svnClient.getSingleStatus(file).isManaged())
			    return;
			
			// determine directories to add to repository			
			while ((currentDir != null)
			    && (!svnClient.getSingleStatus(currentDir).isManaged())
			    && (!currentDir.equals(baseDir))) {
			    dirs.push(currentDir);
			    currentDir = currentDir.getParentFile();
			}
		} catch (ClientException e) {
			throw new BuildException("Cannot get status of file or directory");
		}

        // add them to the repository
        while (dirs.size() > 0) {
            currentDir = (File) dirs.pop();
            try {
                svnClient.addFile(currentDir);
            } catch (Exception e) {
                throw new BuildException(
                    "Cannot add directory "
                        + currentDir.getAbsolutePath()
                        + " to repository",
                    e);
            }
        }

        // now add the file ...
        try {
            svnClient.addFile(file);
        } catch (Exception e) {
            throw new BuildException(
                "Can't add file " + file.getAbsolutePath() + " to repository",
                e);

        }
    }

    /**
     * add a fileset (both dirs and files) to the repository
     * @param svnClient
     * @param fs
     * @throws BuildException
     */
    private void svnAddFileSet(FileSet fs) throws BuildException {
        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
        File baseDir = fs.getDir(getProject()); // base dir
        String[] files = ds.getIncludedFiles();
        String[] dirs = ds.getIncludedDirectories();

        // first : we add directories to the repository
        for (int i = 0; i < dirs.length; i++) {
            File dir = new File(baseDir, dirs[i]);
            svnAddFileWithDirs(dir, baseDir);
        }

        // then we add files
        for (int i = 0; i < files.length; i++) {
            File file = new File(baseDir, files[i]);
            svnAddFileWithDirs(file, baseDir);
        }
    }

	/**
	 * set file to add to repository
	 * @param file
	 */
    public void setFile(File file) {
        this.file = file;
    }

	/**
	 * set the directory to add to the repository
	 * @param dir
	 */
    public void setDir(File dir) {
        this.dir = dir;
    }

	/**
	 * if set, directory will be added recursively (see setDir)
	 * @param recursive
	 */
    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

    /**
     * Adds a set of files to add
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }

}
