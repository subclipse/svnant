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
import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
import org.tigris.subversion.svnclientadapter.ISVNStatus;
import org.tigris.subversion.svnclientadapter.SVNStatusKind;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNStatusUtils;

/**
 * svn Add. Add a file, a directory or a set of files to repository
 * @author Cédric Chabanois 
 *         <a href="mailto:cchabanois@ifrance.com">cchabanois@ifrance.com</a>
 *
 */
public class Commit extends SvnCommand {
    final private int SVN_ERR_WC_NOT_DIRECTORY = 155007;

    /** message for commit */
    private String message = null;

    /** file to commit */
    private File file = null;

    /** directory to commit */
    private File dir = null;

    /** add recursively ? (only for dir attribute) */
    private boolean recurse = true;

    /** filesets to commit */
    private Vector filesets = new Vector();

    private ISVNClientAdapter svnClient;

    public void execute(ISVNClientAdapter svnClient) throws BuildException {
		this.svnClient = svnClient;
        validateAttributes();
		
		log("Svn : Sending changes from your working copy to the repository :");
		
        // deal with the single file
        if (file != null) {
            svnCommitFile(file);
        }

        // deal with a directory
        if (dir != null) {
            svnCommitDir(dir, recurse);
        }

        // deal with filesets		
        if (filesets.size() > 0) {
            for (int i = 0; i < filesets.size(); i++) {
                FileSet fs = (FileSet) filesets.elementAt(i);
                svnCommitFileSet(fs);
            }
        }
    }

    /**
     * Ensure we have a consistent and legal set of attributes
     */
    protected void validateAttributes() throws BuildException {

        if ((file == null) && (dir == null) && (filesets.size() == 0))
            throw new BuildException("file, url or fileset must be set");
        if (file != null)
            if ((dir != null) || (filesets.size() != 0))
                throw new BuildException("dir and fileset must not be set when file attribute is present");

        if (dir != null)
            if ((file != null) || (filesets.size() != 0))
                throw new BuildException("file and fileset must not be set when dir attribute is present");

        if (message == null)
            throw new BuildException("Message must be set");
    }

    /**
     * commit a file to the repository
     * @param svnClient
     * @param file
     * @throws BuildException
     */
    private void svnCommitFile(File file) throws BuildException {
        if (file.exists()) {
            if (file.isDirectory()) {
                log(
                    "Directory "
                        + file.getAbsolutePath()
                        + " cannot be commited using the file attribute.  "
                        + "Use dir instead.");
            } else {
                try {
                    svnClient.commit(new File[] { file }, message, false);
                } catch (Exception e) {
                    throw new BuildException(
                        "Can't commit file " + file.getAbsolutePath(),
                        e);
                }
            }
        } else {
            String message =
                "Warning: Could not find file "
                    + file.getAbsolutePath()
                    + " to commit to the repository.";
            throw new BuildException(message);
        }
    }

    /**
     * commit a directory to the repository
     * @param svnClient
     * @param dir
     * @param recursive
     * @throws BuildException
     */
    private void svnCommitDir(File dir, boolean recursive)
        throws BuildException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                log(
                    "File "
                        + dir.getAbsolutePath()
                        + " cannot be commited using the dir attribute.  "
                        + "Use file instead.");
            } else {
                try {
                    svnClient.commit(new File[] { dir }, message, recursive);
                } catch (Exception e) {
                    throw new BuildException(
                        "Can't commit directory " + dir.getAbsolutePath(),
                        e);
                }
            }
        } else {
            String message =
                "Warning: Could not find directory "
                    + dir.getAbsolutePath()
                    + " to add to the repository.";
            throw new BuildException(message);

        }

    }

    /**
     * commit the file (or directory) to the repository, including any necessary parent directories
     * @param svnClient
     * @param file
     * @param baseDir
     * @throws BuildException
     */
    private void svnPrepareCommitFileWithDirs(
        Vector filesToCommit,
        File file,
        File baseDir)
        throws BuildException {

        if (filesToCommit.contains(file))
            return; // we already know that we will commit it

        try {
			// file has not been "added", we cannot commit it
			if (!SVNStatusUtils.isManaged(svnClient.getSingleStatus(file)))
			    return;
		} catch (SVNClientException e1) {
            throw new BuildException("Cannot get status of file :"+file.toString(),e1);
		}

        // determine directories to commit
        // we will commit directories that are necessary to commit in order to
        // commit specified files (ie parent directories that have been added) 

		Stack dirs;
		File currentDir = null;
		try {
			dirs = new Stack();
			currentDir = file.getParentFile();
			ISVNStatus status = svnClient.getSingleStatus(currentDir);
			while ((currentDir != null)
			    && (status.getTextStatus() == SVNStatusKind.ADDED)
			    && (!currentDir.equals(baseDir))) {
			    dirs.push(currentDir);
			    currentDir = currentDir.getParentFile();
			    status = svnClient.getSingleStatus(currentDir);
			}
		} catch (SVNClientException e) {
            throw new BuildException("Cannot get status of directory :"+currentDir.toString(),e);
		}

        // add them to the vector
        while (dirs.size() > 0) {
            currentDir = (File) dirs.pop();
            if (!filesToCommit.contains(currentDir))
                filesToCommit.add(currentDir);
        }

        // now add the file ...
        filesToCommit.add(file);
    }

    /**
     * add a fileset (both dirs and files) to the repository
     * @param svnClient
     * @param fs
     * @throws BuildException
     */
    private void svnCommitFileSet(FileSet fs) throws BuildException {
        DirectoryScanner ds = fs.getDirectoryScanner(getProject());
        File baseDir = fs.getDir(getProject()); // base dir
        String[] includedFiles = ds.getIncludedFiles();
        String[] includedDirs = ds.getIncludedDirectories();
        Vector filesToCommit = new Vector();

        // first : we add directories to the repository
        for (int i = 0; i < includedDirs.length; i++) {
            File dir = new File(baseDir, includedDirs[i]);
            svnPrepareCommitFileWithDirs(filesToCommit, dir, baseDir);
        }

        // then we add files
        for (int i = 0; i < includedFiles.length; i++) {
            File file = new File(baseDir, includedFiles[i]);
            svnPrepareCommitFileWithDirs(filesToCommit, file, baseDir);
        }
        File[] files = new File[filesToCommit.size()];
        for (int i = 0; i < filesToCommit.size(); i++)
            files[i] = ((File) filesToCommit.get(i));

        // finally we commit files
        try {
            svnClient.commit(files, message, false);
        } catch (Exception e) {
            throw new BuildException("Can't commit fileset : ", e);
        }

    }

	/**
	 * set file to commit
	 * @param file
	 */
    public void setFile(File file) {
        this.file = file;
    }

	/**
	 * set directory to commit
	 * @param dir
	 */
    public void setDir(File dir) {
        this.dir = dir;
    }

	/**
	 * if set, directory will be commited recursively (see setDir)
	 * @param recursive
	 */
    public void setRecurse(boolean recurse) {
        this.recurse = recurse;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Adds a set of files to add
     */
    public void addFileset(FileSet set) {
        filesets.addElement(set);
    }

}
