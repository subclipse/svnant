package org.tigris.subversion.svnant.conditions;


import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;
import org.tigris.subversion.svnant.SvnAntException;
import org.tigris.subversion.svnant.SvnFacade;

/**
 * This is an abstract class that implements functions common to all subclasses.
 * It handles the generic 'javahl' and 'svnkit' properties.
 * 
 * Conditions are from a special type in ANT where they can be used to set values
 * of properties. See taks Condition in ANT documentation.
 * 
 * @author Jean-Pierre Fiset <a href="mailto:jp@fiset.ca">jp@fiset.ca</a>
 *
 */
public abstract class SvnCondition extends ConditionBase implements Condition {

    /* (non-Javadoc)
     * @see org.tigris.subversion.svnant.ISvnAntProjectComponent#getProjectComponent()
     */
    public ProjectComponent getProjectComponent() {
        return this;
    }

    /**
     * @see org.tigris.subversion.svnant.ISvnAntProjectComponent#getJavahl()
     */
    public boolean getJavahl() {
        return SvnFacade.getJavahl( this );
    }

    /**
     * @see org.tigris.subversion.svnant.ISvnAntProjectComponent#getSvnKit()
     */
    public boolean getSvnKit() {
        return SvnFacade.getSvnKit( this );
    }

    /**
     * Sets the referred configuration to be used for the svn task.
     *
     * @param refid   The id of the configuration to be used for the svn task.
     */
    public void setRefid(String refid) {
        SvnFacade.setRefid( this, refid );
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        SvnFacade.setUsername( this, username );
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        SvnFacade.setPassword( this, password );
    }

    /**
     * set javahl to false to use command line interface
     * @param javahl
     */
    public void setJavahl(boolean javahl) {
        SvnFacade.setJavahl( this, javahl );
    }

    /**
     * set svnkit to false to use command line interface
     * @param svnkit
     */
    public void setSvnkit(boolean svnkit) {
        SvnFacade.setSvnKit( this, svnkit );
    }

    /**
     * {@inheritDoc}
     */
    public boolean eval() throws BuildException {
        try {
            preconditions();
            return internalEval();
        } catch (SvnAntException e) {
            throw new BuildException(e.getMessage(), e);
        }
    }
    
    /**
     * Causes a BuildException to be thrown if a precondition isn't met.
     * 
     * @throws BuildException   A precondition isn't fulfilled.
     */
    protected void preconditions() throws BuildException {
    }
    
    /**
     * Method called internally by eval(). Must be implemented by all subclasses.
     * @return True if the condition is met. False, otherwsie.
     * @throws SvnAntException exception that should be used in case of problems.
     */
    protected abstract boolean internalEval() throws SvnAntException;
    
}
