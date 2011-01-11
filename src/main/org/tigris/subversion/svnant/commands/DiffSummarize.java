package org.tigris.subversion.svnant.commands;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.tigris.subversion.svnant.SvnAntException;
import org.tigris.subversion.svnant.SvnAntValidationException;
import org.tigris.subversion.svnclientadapter.SVNClientException;
import org.tigris.subversion.svnclientadapter.SVNDiffSummary;

/**
 * Diff summary (as in "svn diff --summarize"). The first version only displays
 * the diff summary as the CLI client does. The objective is to make the
 * resources available for manipulation by other tasks.
 * 
 * @author Michael Ludwig
 */
public class DiffSummarize extends Diff {
  
    private int depth = 1000; // default depth of directory tree descent
    private boolean ignoreAncestry = true;
    private boolean logToFile = false;
    private String encoding = "UTF-8";

    public void setEncoding(String e) {
        encoding = e;
    }

    public void setOutFile(File f) {
        super.setOutFile(f);
        this.logToFile = true;
    }

    /*
     * ISVNClientAdapter#diffSummarize : parameter ignoreAncestry = false
     * svn CLI : --notice-ancestry
     * svnant : ancestry="true"
     */
    public void setAncestry(boolean b) {
        this.ignoreAncestry = ! b;
    }

    public void setDepth(int d) {
        this.depth = d;
    }

    /**
     * {@inheritDoc}
     */
    public void execute() throws SvnAntException {
        BufferedWriter out = null;
        if (logToFile) {
            File f = getOutFile();
            try {
                log("output to file: " + f);
                out = new BufferedWriter(new OutputStreamWriter(( new FileOutputStream(f) ), encoding));
            } catch ( Exception e ) {
                throw new BuildException(e);
            }
        }
        try {
            
            // summarize only supported on repo, so on URLs
            logAction(true);
          
            SVNDiffSummary[] summary = svnClient.diffSummarize(
                getOldUrl(), getOldTargetRevision(),
                getNewUrl(), getNewTargetRevision(),
                depth, ignoreAncestry);
            
            StringBuilder sb = new StringBuilder();
            for (SVNDiffSummary s : summary) {
                sb.setLength(0);
                char first = Character.toUpperCase(s.getDiffKind().toString().charAt(0));
                if ((first != 'A') && (first != 'M') && (first != 'D')) {
                    log(String.format("the diff kind '%s' is currently not known", Character.valueOf(first)), Project.MSG_WARN);
                }
                sb.append(s.propsChanged() ? "M" : " ");
                // log(String.format("%s %s %s\n", status, propSt, s.getPath()));
                sb.append(" ");
                sb.append(s.getPath());
                if (logToFile) {
                    sb.append("\n");
                    out.write(sb.toString());
                } else {
                    log(sb.toString());
                }
            }
        } catch (SVNClientException ex) {
            throw new SvnAntException(ex);
        } catch (IOException ex) {
            throw new BuildException(ex);
        } finally {
            if (out != null) {
              try {
                  out.close();
              } catch (IOException ex) {
                  throw new BuildException(ex);
              }
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    protected void validateAttributes() throws SvnAntValidationException {
        super.validateAttributes();
        if (encoding.trim().length() == 0) {
            throw new SvnAntValidationException("the parameter 'encoding' is supposed to provide a value");
        }
    }
    
}
