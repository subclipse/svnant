package org.tigris.subversion.svnant.conditions;


import org.tigris.subversion.svnant.SvnFacade;

import org.apache.tools.ant.types.Reference;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.ProjectComponent;
import org.apache.tools.ant.taskdefs.condition.Condition;
import org.apache.tools.ant.taskdefs.condition.ConditionBase;

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

    /**
     * @see SvnFacade#setRefid(ProjectComponent, org.apache.tools.ant.types.Reference)
     */
    public void setRefid( Reference refid ) {
        SvnFacade.setRefid( this, refid );
    }

    /**
     * @see SvnFacade#setUsername(ProjectComponent, String)
     */
    public void setUsername( String username ) {
        SvnFacade.setUsername( this, username );
    }

    /**
     * @see SvnFacade#setPassword(ProjectComponent, String)
     */
    public void setPassword( String password ) {
        SvnFacade.setPassword( this, password );
    }

    /**
     * @see SvnFacade#setJavahl(ProjectComponent, boolean)
     */
    public void setJavahl( boolean javahl ) {
        SvnFacade.setJavahl( this, javahl );
    }

    /**
     * @see SvnFacade#setSvnKit(ProjectComponent, boolean)
     */
    public void setSvnkit( boolean svnkit ) {
        SvnFacade.setSvnKit( this, svnkit );
    }

    /**
     * {@inheritDoc}
     */
    public boolean eval() {
        preconditions();
        return internalEval();
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
     */
    protected abstract boolean internalEval();
    
}
